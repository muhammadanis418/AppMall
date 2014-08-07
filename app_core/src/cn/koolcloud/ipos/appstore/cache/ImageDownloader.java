package cn.koolcloud.ipos.appstore.cache;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.api.HttpService;
import cn.koolcloud.ipos.appstore.cache.BindDataIf.BindHolder;
import cn.koolcloud.ipos.appstore.cache.BindDataIf.Callback;
import cn.koolcloud.ipos.appstore.cache.base.AsyncTask;
import cn.koolcloud.ipos.appstore.utils.MyLog;

/**
 * download images.
 * this class include memory image cache, file object cache, and the files in the sdcard.
 * @author Teddy
 * @Create 2013-10-29
 */
@SuppressLint("HandlerLeak")
public class ImageDownloader {

	private static final String LOG_TAG = ImageDownloader.class.getSimpleName();
	
	/**
	 * the default memory size of LruMemoryCache
	 */
	private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 1024 * 6;
	
	/**
	 * the times of retrying to download images.
	 */
	private static final int IMAGE_RETRY_TIMES = 3;
	
	private ExecutorService executorService;
	ImageFileCache imageFileCache;
	
	private LruMemoryCache<String, Bitmap> mLruMemoryCache;
	
	private static ImageDownloader instance;
	private Context mContext;

	public static ImageDownloader getInstance(Context context) {
		if (instance == null) {
			synchronized(ImageDownloader.class) {
				if (instance == null)
					instance = new ImageDownloader(context);
			}
		}
		return instance;
	}
	
	private ImageDownloader(Context context) {
		mContext = context;
		int cpuNums = Runtime.getRuntime().availableProcessors();
		executorService = Executors.newFixedThreadPool(cpuNums * 10);//thread pool
		imageFileCache = new ImageFileCache(context);
		
		mLruMemoryCache = new LruMemoryCache<String, Bitmap>(DEFAULT_MEM_CACHE_SIZE) {
            /**
             * Measure item size in bytes rather than units which is more practical
             * for a bitmap cache
             */
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return getBitmapSize(bitmap);
            }
        };
	}
	
	private static int getBitmapSize(Bitmap bitmap) {
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
	
	/**
	 * release the memory of mLruMemoryCache
	 */
	public void release(){
		mLruMemoryCache.clearCache();
	}
	
	/**
	 * delete the image files from sd
	 */
	public void deleteImagesOfSd(){
		imageFileCache.deleteAll();
	}
	
	/**
     * Download the specified image from the Internet and binds it to the provided ImageView. The
     * binding is immediate if the image is found in the cache and will be done asynchronously
     * otherwise. A null bitmap will be associated to the ImageView if an error occurs.
     *
     * @param url The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
	@Deprecated
	public void download(BindHolder holder, Callback callback) {
		String url = holder.getUrl();
		if(null == url || "".equals(url)) {
			return;
		}
		// TO DO :  sbh : don't store bitmap object in cache , use local files (compressed cache)
		//Bitmap bitmap = imageMemoryCache.getBitmapFromCache(url);
		Bitmap bitmap = mLruMemoryCache.get(url);
		if(bitmap == null) {
			executorService.submit(new ImageTask(new ImageTaskHandler(callback), url, holder));
		} else {
			MyLog.d(" return image from the cache, " + bitmap+"  from url ="+url);
			holder.setResource(bitmap);
			callback.callback(holder);
		}
	}
	
	/**
     * get one image from three places, imageMemoryCache,imageFileCache,network
     *
     * @param url The URL of the image to download.
     * @return Bitmap return a bitmap
     */
	private Bitmap getBitmap(final String url) {
    	if(null == url || "".equals(url)) {
			return null;
		}
        // get image from mLruMemoryCache
    	Bitmap result = null;
    	result = mLruMemoryCache.get(url);
        if (result == null) {
            // get image from imageFileCache
            result = imageFileCache.getImage(url);
            if (result == null) {
                // get image from network
//                result = getImageHttp(url);
                result = getImageFromHttp(url);
                if (result != null) {
                	if (mLruMemoryCache.get(url) == null) {
                        mLruMemoryCache.put(url, result);
                    }
					imageFileCache.addImgToSDTask(url, result);
                }
            } else {
            	if (mLruMemoryCache.get(url) == null) {
                    mLruMemoryCache.put(url, result);
                }
            }
        }
        return result;
    }
	
	
	/**
	 * download image from url through the Internet
	 * @param fileName
	 * @return Bitmap from http
	 */
    private Bitmap getImageFromHttp(String fileName) {
		//try to get image from file cache
//    	Logger.debug(this, "Load image from network " + fileName);
		Bitmap bitmap = null;
		int times = 0;
		String urlString = ApiService.getDownloadPictureUrl();
		InputStream in = null;
		
		String[] strArray = fileName.split("_");
		
		JSONObject params = ApiService.getDownloadPicJson(strArray[0], mContext);
		
		HttpService httpService = new HttpService();
		
		while (times < IMAGE_RETRY_TIMES) {
			try {
				
				HttpResponse response = httpService.getResponseResult(urlString, params, "post", null);
				if (response != null) {
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode == HttpStatus.SC_OK) {
						in = new BufferedInputStream(response.getEntity().getContent());
						BitmapFactory.Options opt = new BitmapFactory.Options();
						opt.inPreferredConfig = Bitmap.Config.RGB_565;   
						opt.inPurgeable = true;  
						opt.inInputShareable = true;
						
						InputStream is = new FlushedInputStream(in);
						bitmap = BitmapFactory.decodeStream(is, null, opt);
						is.close();
						in.close();
					} else if ((statusCode == HttpStatus.SC_MOVED_PERMANENTLY) ||
				            (statusCode == HttpStatus.SC_MOVED_TEMPORARILY) ||
				            (statusCode == HttpStatus.SC_SEE_OTHER) ||
				            (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {// response redirect
						String newUrl = response.getLastHeader("Location").getValue();
						HttpResponse newResponse = httpService.getResponseResult(newUrl, params, "post", null);
						
						if (newResponse != null) {
							statusCode = newResponse.getStatusLine().getStatusCode();
							if (statusCode == HttpStatus.SC_OK) {
								in = new BufferedInputStream(newResponse.getEntity().getContent());
								BitmapFactory.Options opt = new BitmapFactory.Options();
								opt.inPreferredConfig = Bitmap.Config.RGB_565;   
								opt.inPurgeable = true;  
								opt.inInputShareable = true;
								
								InputStream is = new FlushedInputStream(in);
								bitmap = BitmapFactory.decodeStream(is, null, opt);
								is.close();
								in.close();
							}
						} else {
							
							MyLog.d(LOG_TAG + "_" + "error: " + statusCode);
						}
					} else {
						MyLog.d(LOG_TAG + "_" + "error: "
								+ response.getStatusLine().getStatusCode());
					}
				}
		       
		        return bitmap;
			} catch (Exception e) {
				MyLog.w("getImageHttp=" + fileName + e);
				times ++;
			}
			continue;
		}
		return null;
	} // end of downloadBitmap
    
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    } // end of FlushedInputStream
    
    private class ImageTaskHandler extends Handler {
        BindDataIf.Callback callback;

        @SuppressLint("HandlerLeak")
		public ImageTaskHandler(BindDataIf.Callback callback) {
        	this.callback = callback;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null) {
            	BindHolder holder = (BindHolder) msg.obj;
                callback.callback(holder);
            }
        }
    }

    private class ImageTask implements Callable<String> {
        private String url;
        private Handler handler;
        private BindHolder holder;

        public ImageTask(Handler handler, String url, BindHolder holder) {
            this.url = url;
            this.handler = handler;
            this.holder = holder;
        }

        @Override
        public String call() throws Exception {
            Message msg = new Message();
            if(holder != null) {
            	holder.setResource(getBitmap(url));
                msg.obj = holder;
                if (msg.obj != null) {
                	handler.sendMessage(msg);
                }
            }
            return url;
        }
    }
    
    public void deleteAllImages() {
    	imageFileCache.deleteAll();
    }
    
    /**
     * used for ImageView to download images.
	 * <p>if you use "ViewHolder" and "convertView.getTag()" in listView or gridView, you must use this method'.</p>
	 * <p>for image, if you just use bitmap from this method, you don't need to recycle this bitmap. 
	 * if you create a temporary bitmap, you must recycle it by yourself.</p>
     * @param url
     * @param defaultBitmap
     * @param imageView
     */
    public void download(String url, Bitmap defaultBitmap, ImageView imageView) {
		if (null == url || "".equals(url) || "null".equals(url)) {
			return ;
		}
		Bitmap bitmap = null;
		//bitmap = imageMemoryCache.getBitmapFromSoftCache(url);
		//bitmap = memoryCache.get(url);
		bitmap = mLruMemoryCache.get(url);
		if (bitmap != null) {
//			Logger.debug(this,"get image from memory cache, " + bitmap + "  from url =" + url);
			imageView.setImageBitmap(bitmap);
		} else if (checkImageTask(url, imageView)) {
			final BitmapLoadAndDisplayTask task = new BitmapLoadAndDisplayTask(
					imageView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(
					mContext.getResources(), defaultBitmap, task);
			imageView.setImageDrawable(asyncDrawable);

			task.executeOnExecutor(executorService, url);
		}
	}

	/**
	 * check whether the imageView has it's task.
	 * 
	 * @param data
	 * @param imageView
	 * @return true no task
	 */
	private static boolean checkImageTask(Object data, ImageView imageView) {
		final BitmapLoadAndDisplayTask bitmapWorkerTask = getBitmapTaskFromImageView(imageView);

		if (bitmapWorkerTask != null) {
			final Object bitmapData = bitmapWorkerTask.data;
			if (bitmapData == null || !bitmapData.equals(data)) {
				bitmapWorkerTask.cancel(true);
			} else {
				return false;
			}
		}
		return true;
	}
	
	private static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapLoadAndDisplayTask> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap defaultBitmap, BitmapLoadAndDisplayTask bitmapWorkerTask) {
			super(res, defaultBitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapLoadAndDisplayTask>(
					bitmapWorkerTask);
		}

		public BitmapLoadAndDisplayTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	private class BitmapLoadAndDisplayTask extends
			AsyncTask<Object, Void, Bitmap> {
		private Object data;
		private final WeakReference<ImageView> imageViewReference;

		public BitmapLoadAndDisplayTask(ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected Bitmap doInBackground(Object... params) {
			data = params[0];
			final String dataString = String.valueOf(data);
			Bitmap bitmap = null;

			/*synchronized (mPauseWorkLock) {
				while (mPauseWork && !isCancelled()) {
					try {
						mPauseWorkLock.wait();
					} catch (InterruptedException e) {
					}
				}
			}*/
			
			if (!isCancelled() && getAttachedImageView() != null) {
				bitmap = getBitmap(dataString);
			}

			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()/* || mExitTasksEarly*/) {
				bitmap = null;
			}

			final ImageView imageView = getAttachedImageView();
			if (bitmap != null && imageView != null) {
				imageView.setImageBitmap(bitmap);
			}/* else if (bitmap == null && imageView != null) {
			}*/
		}

		@Override
		protected void onCancelled(Bitmap bitmap) {
			super.onCancelled(bitmap);
		}

		/**
		 * get ImageView that matched with it's thread. avoid repeated images and glint.
		 * 
		 * @return
		 */
		private ImageView getAttachedImageView() {
			final ImageView imageView = imageViewReference.get();
			final BitmapLoadAndDisplayTask bitmapWorkerTask = getBitmapTaskFromImageView(imageView);

			if (this == bitmapWorkerTask) {
				return imageView;
			}

			return null;
		}
	}

	private static BitmapLoadAndDisplayTask getBitmapTaskFromImageView(
			ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}
}
