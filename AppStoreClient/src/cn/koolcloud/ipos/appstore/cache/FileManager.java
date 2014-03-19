package cn.koolcloud.ipos.appstore.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.Logger;
import cn.koolcloud.ipos.appstore.utils.MD5;

/**
 * download xml,json, and some static methods about operating files.
 * 
 * @author Teddy
 * @Create 2013-10-29
 */
public class FileManager {

	public static final String TAG_STRING = "FileManager";

	public static final int DATA_SIZE_IN_MB = 25;
	public static final int SD_SIZE_IN_MB = 20;
	private static final long XML_JSON_TIME_DIFF = 3 * 24 * 60 * 60 * 1000; // the
																			// file
																			// will
																			// be
																			// deleted
																			// after
																			// TIME_DIFF(unit
																			// ms).
	public static final long IMAGE_TIME_DIFF = 3 * 24 * 60 * 60 * 1000; // the
																		// file
																		// will
																		// be
																		// deleted
																		// after
																		// TIME_DIFF(unit
																		// ms).
	public static final float REMOVE_FACTOR = 0.5f;
	public static final int MB = 1024 * 1024;
	public static final int BUFFER_SIZE = 1024 * 4;

	/**
	 * get string from local file, or download network file and return a string
	 * from file.
	 * 
	 * @param
	 * @return String a string from xml or json file.
	 */
	public static String getStringFromFileOrUrl(Context context, String path,
			String url, String fileNameToSave, boolean forceDownload) {
		String resultString = null;
		try {
			if (!forceDownload) {// false, get file from local
				resultString = getStringFromLocal(context, path, fileNameToSave);
			}
			if (resultString == null) {
				resultString = getStringFromUrlFile(context, path, url,
						fileNameToSave);
			}
		} catch (IOException e) {
			resultString = null;
		}
		return resultString;
	}

	/**
	 * get a inputStream from local file, or download network file and return a
	 * inputStream from file.
	 * 
	 * @param
	 * @return InputStream a inputStream from xml or json file.
	 */
	public static InputStream getInputStreamFromFileOrUrl(Context context,
			String path, String url, String fileNameToSave,
			boolean forceDownload) {
		InputStream resultInputStream = null;
		try {
			if (!forceDownload) {// false, get file from local
				resultInputStream = getInputStreamFromLocal(context, path,
						fileNameToSave);
			}
			if (resultInputStream == null) {
				resultInputStream = getFileFromUrl(context, path, url,
						fileNameToSave);
			}
		} catch (IOException e) {
			resultInputStream = null;
		}
		return resultInputStream;
	}

	public static InputStream getInputStreamFromFileOrUrlByPost(
			Context context, String path, String url, String fileNameToSave,
			boolean forceDownload, List<NameValuePair> parameters,
			boolean isNeedMD5) {
		InputStream resultInputStream = null;
		try {
			if (!forceDownload) {// false, get file from local
				resultInputStream = getInputStreamFromLocal(context, path,
						fileNameToSave);
			}
			if (resultInputStream == null) {
				InputStream is = null;
				BufferedInputStream bis = null;
				try {
					if (isNeedMD5) {
						parameters = MD5.appendAppsParameters(parameters);
					}
					BasicHttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParams,
							10 * 1000);
					HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);

					HttpClient httpClinet = new DefaultHttpClient(httpParams);
					HttpPost request = new HttpPost(url);
					request.setEntity(new UrlEncodedFormEntity(parameters,
							HTTP.UTF_8));

					Logger.d("==url==" + url);

					Logger.d("==parameters==" + parameters.toString());

					HttpResponse response = httpClinet.execute(request);
					Logger.d("==StatusCode=="
							+ response.getStatusLine().getStatusCode());
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						HttpEntity entity = response.getEntity();
						// Logger.debug(TAG_STRING, "==result=="+
						// EntityUtils.toString(entity));
						is = entity.getContent();
						if(fileNameToSave == null) {
							return is;
						} else {
							bis = new BufferedInputStream(is, 8192);
							return getInputStream(context, path, fileNameToSave,
									bis);
						}
					}
				} catch (Exception e) {
					Logger.d("Error in  File Download:" + e.getMessage());
					e.printStackTrace();
					return null;
				}
			}
		} catch (IOException e) {
			resultInputStream = null;
		}
		return resultInputStream;
	}
	
	public static InputStream getInputStreamFromFileOrUrlByPost(
			Context context, String path, String url, String fileNameToSave,
			boolean forceDownload, List<NameValuePair> parameters,
			boolean isNeedMD5, boolean formatIsJson) {
		InputStream resultInputStream = null;
		try {
			if (!forceDownload) {// false, get file from local
				resultInputStream = getInputStreamFromLocal(context, path,
						fileNameToSave);
			}
			if (resultInputStream == null) {
				InputStream is = null;
				BufferedInputStream bis = null;
				try {
					if (isNeedMD5) {
						if(formatIsJson) {
							parameters = MD5.appendAppsParametersJSON(parameters);
						} else {
							parameters = MD5.appendAppsParameters(parameters);
						}
					}
					BasicHttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParams,
							10 * 1000);
					HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);

					HttpClient httpClinet = new DefaultHttpClient(httpParams);
					HttpPost request = new HttpPost(url);
					request.setEntity(new UrlEncodedFormEntity(parameters,
							HTTP.UTF_8));

					Logger.d("==url==" + url);

					Logger.d("==parameters==" + parameters.toString());

					HttpResponse response = httpClinet.execute(request);
					Logger.d("==StatusCode=="
							+ response.getStatusLine().getStatusCode());
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						HttpEntity entity = response.getEntity();
						// Logger.debug(TAG_STRING, "==result=="+
						// EntityUtils.toString(entity));
						is = entity.getContent();
						if(fileNameToSave == null) {
							return is;
						} else {
							bis = new BufferedInputStream(is, 8192);
							return getInputStream(context, path, fileNameToSave,
									bis);
						}
					}
				} catch (Exception e) {
					Logger.d("Error in  File Download:" + e.getMessage());
					e.printStackTrace();
					return null;
				}
			}
		} catch (IOException e) {
			resultInputStream = null;
		}
		return resultInputStream;
	}
	
	
	public static final String multipart_form_data = "multipart/form-data";  
	public static final String twoHyphens = "--";  
    public static final String boundary = "----WebKitFormBoundarySnxMMaE8drOymB2v";    // data separator
    public static final String lineEnd = System.getProperty("line.separator");  // The value is "\r\n" in Windows.  
    
	/**
	 * @param context
	 * @param path
	 * @param url
	 * @param fileNameToSave
	 * @param forceDownload
	 * @param parameters
	 * @param formatIsJson
	 * @return
	 */
	public static String getInputStreamByPostMultipart2(String actionUrl, Set<Map.Entry<Object,Object>> params) {
		HttpURLConnection conn = null;  
        DataOutputStream output = null;  
        BufferedReader input = null;  
        try {  
            URL url = new URL(actionUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(30000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Type", multipart_form_data + "; boundary=" + boundary);
            conn.addRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
              
            conn.connect();
            output = new DataOutputStream(conn.getOutputStream());
            addFormField(params, output);
              
            output.writeBytes(twoHyphens + boundary + twoHyphens +lineEnd);
            output.flush();
            
            int code = conn.getResponseCode();
            if(code != 200) {
            	Logger.d("Error in  File Download, getResponseCode=" + code);
                return null;
            }
              
            input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String oneLine;
            while((oneLine = input.readLine()) != null) {
                response.append(oneLine + lineEnd);
            }
            return response.toString();
        } catch (IOException e) {
        	Logger.w("Error in post Multipart=" + e);
            return null;
        } finally {
            try {
                if(output != null) {
                    output.close();
                }
                if(input != null) {
                    input.close();  
                }
            } catch (IOException e) {
            	Logger.w("Error in finally=" + e);
            }
              
            if(conn != null) {
                conn.disconnect();
            }
        }
	}
	
	/**
	 * @param context
	 * @param path
	 * @param url
	 * @param fileNameToSave
	 * @param forceDownload
	 * @param parameters
	 * @param formatIsJson
	 * @return
	 */
	/*
	public static String getInputStreamByPostMultipart(String actionUrl, Set<Map.Entry<Object,Object>> params) {
		Logger.debug(TAG_STRING, "post Multipart,actionUrl=" + actionUrl + ", params="+params.toString());
		try {
			BasicHttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams,
					10 * 1000);
			HttpConnectionParams.setSoTimeout(httpParams, 20 * 1000);
	
			HttpClient httpClinet = new DefaultHttpClient(httpParams);
			HttpPost request = new HttpPost(actionUrl);
	*/
	
	//		request.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
	/*		
			MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);  
			for (Map.Entry<Object,Object> entry : params) {
				FormBodyPart formBodyPart = new FormBodyPart((String)entry.getKey(), new StringBody((String)entry.getValue()));
				multipartEntity.addPart(formBodyPart);
			}
	        
	        request.setEntity(multipartEntity);
        
	        HttpResponse response = httpClinet.execute(request);
	        int status = response.getStatusLine().getStatusCode();
	        Logger.debug(TAG_STRING, "post Multipart,status=" + status);
	        if(status == 200) {
	        	HttpEntity r_entity = response.getEntity();
	        	String responseString = EntityUtils.toString(r_entity);
	        	Logger.debug(TAG_STRING, "post Multipart,responseString=" +responseString);
	        	return responseString;
	        }
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return null;
	}
	*/
	private static void addFormField(Set<Map.Entry<Object,Object>> params, DataOutputStream output) {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<Object, Object> param : params) {
            sb.append(twoHyphens + boundary + lineEnd);
            sb.append("Content-Disposition: form-data; name=\"" + param.getKey() + "\"" + lineEnd);
            sb.append(lineEnd);
            sb.append(param.getValue() + lineEnd);
        }
        try {
            output.writeBytes(sb.toString());
        } catch (IOException e) {
        	Logger.w("addFormField Error=" + e);
        }
    }

	private static InputStream getInputStreamFromLocal(Context context,
			String path, String fileNameToSave) throws IOException {
		File file = new File(Env.getDataDirectoryPath(path), fileNameToSave);
		if (!file.exists()) {
			return null;
		}
		updateFileTime(file);
		return new FileInputStream(file);
	}

	private static String getStringFromLocal(Context context, String path,
			String fileNameToSave) throws IOException {
		File file = new File(Env.getDataDirectoryPath(path), fileNameToSave);
		if (!file.exists()) {
			return null;
		}
		updateFileTime(file);
		FileInputStream fis = new FileInputStream(file);
		return convertStreamToString(fis);
	}

	public static InputStream getFileFromUrl(Context context, String path,
			String url, String fileNameToSave) {
		Logger.d("download url = " + url);
		InputStream bis = download(url);
		if (bis == null) {
			return null;
		}
		return getInputStream(context, path, fileNameToSave, bis);
	}

	private static InputStream getInputStream(Context context, String path,
			String fileNameToSave, InputStream bis) {
		if (bis == null) {
			return null;
		}
		if (fileNameToSave == null) {// don't save file
			return bis;
		}

		if (canWriteData()) {
			reduceCache(Env.getAppDataDirectory(), DATA_SIZE_IN_MB,
					freeSpaceOnData(Environment.getDataDirectory().getPath()),
					XML_JSON_TIME_DIFF);
		} else {
			return bis;
		}

		File file = new File(Env.getDataDirectoryPath(path), fileNameToSave);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e1) {
			return bis;
		}
		BufferedOutputStream bos = new BufferedOutputStream(fos);

		try {
			writeFile(bis, bos);
		} catch (IOException e1) {
			file.delete();
		} finally {
			try {
				bis.close();
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	private static String getStringFromUrlFile(Context context, String path,
			String url, String fileNameToSave) {
		InputStream bis = getFileFromUrl(context, path, url, fileNameToSave);
		if (bis == null) {
			return null;
		}
		return convertStreamToString(bis);
	}

	public static InputStream download(String url) {
		InputStream is = null;
		try {
			URLConnection conn = new URL(url).openConnection();
			conn.connect();
			conn.setConnectTimeout(10000);
			is = conn.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
			is = null;
		}
		return is;
	}

	public static boolean canWriteData() {
		long folderSizeInMb = getFileSize(Env.getAppDataDirectory()) / MB;
		return folderSizeInMb < DATA_SIZE_IN_MB;
	}

	public static boolean canWriteSD() {
		long folderSizeInMb = 0;
		if (exsitSdcard()) {
			folderSizeInMb = getFileSize(Env.getExternalStorageDirectory())
					/ MB;
			return folderSizeInMb < SD_SIZE_IN_MB;
		} else {
			return false;
		}
	}

	private static void writeFile(InputStream bis, OutputStream bos)
			throws IOException {
		int r = 0;
		byte[] buffer = new byte[2048];
		while ((r = bis.read(buffer)) != -1) {
			bos.write(buffer, 0, r);
		}

		bos.flush();
	}

	public static String convertStreamToString(InputStream is) {
		if (is == null) {
			return null;
		}
		try {
			StringBuffer out = new StringBuffer();
			byte[] b = new byte[4096];
			for (int n; (n = is.read(b)) != -1;) {
				out.append(new String(b, 0, n));
			}
			return out.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * count the size of the directory, when the size is larger than CACHE_SIZE
	 * , or the rest room of sdcard is smaller the FREE_SD_SPACE_TO_CACHE,
	 * delete the 40% files which were not lately used.
	 * 
	 * @param dirPath
	 * @param filename
	 */
	public static void reduceCache(File dir, int maxMB,
			int freeSpaceOnDataOrSd, long time_diff) {
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		int dirSize = 0;
		for (int i = 0; i < files.length; i++) {
			if (!removeExpiredCache(files[i], time_diff))
				dirSize += files[i].length();
		}
		if (dirSize > maxMB * MB || maxMB > freeSpaceOnDataOrSd) {
			int removeFactor = (int) ((REMOVE_FACTOR * files.length));
			Arrays.sort(files, new FileLastModifComparator());
			for (int i = 0; i < removeFactor; i++) {
				files[i].delete();
			}
		}
	}

	/**
	 * get folder size
	 * 
	 * @param file
	 * @return
	 */
	public static long getFileSize(File file) {
		long size = 0;
		File fList[] = file.listFiles();
		for (int i = 0; i < fList.length; i++) {
			if (fList[i].isDirectory()) {
				size = size + getFileSize(fList[i]);
			} else {
				size = size + fList[i].length();
			}
		}
		return size;
	}

	/**
	 * judge the sdcard is available or not
	 * 
	 * @return
	 */
	public static boolean exsitSdcard() {
		boolean ret = false;
		String sDcString = Environment.getExternalStorageState();
		if (sDcString.equals(Environment.MEDIA_MOUNTED)) {
			ret = true;
		}
		return ret;
	}

	public static int freeSpaceOnData(String path) {
		StatFs stat = new StatFs(path);
		double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat
				.getBlockSize()) / MB;
		return (int) sdFreeMB;
	}

	/**
	 * remove expired files
	 * 
	 * @param dirPath
	 * @param filename
	 */
	public static boolean removeExpiredCache(File file, long time_diff) {
		if (System.currentTimeMillis() - file.lastModified() > time_diff) {
			return file.delete();
		}
		return false;
	}

	/**
	 * clear file cache on disk.
	 */
	public static void deleteAll(File dir) {
		File[] files = dir.listFiles();
		if (files == null)
			return;
		for (File f : files) {
			if (f.isDirectory()) {
				deleteAll(f);
				f.delete();
			} else {
				f.delete();
			}
		}
	}

	/**
	 * update the LastModified time of the file
	 * 
	 * @param dir
	 * @param fileName
	 */
	public static void updateFileTime(File file) {
		long newModifiedTime = System.currentTimeMillis();
		file.setLastModified(newModifiedTime);
	}

	/**
	 * file comparator
	 */
	public static class FileLastModifComparator implements Comparator<File> {
		@Override
		public int compare(File arg0, File arg1) {
			if (arg0.lastModified() > arg1.lastModified()) {
				return 1;
			} else if (arg0.lastModified() == arg1.lastModified()) {
				return 0;
			} else {
				return -1;
			}
		}
	}

	public static InputStream getStringStream(String sInputString) {
		ByteArrayInputStream tInputStringStream = null;
		if (sInputString != null && !sInputString.trim().equals("")) {
			tInputStringStream = new ByteArrayInputStream(
					sInputString.getBytes());
		}
		return tInputStringStream;
	}
	
	public static File createSDDir(String dirName) {
		String sdpath = Environment.getExternalStorageDirectory() + "/";
        File dir = new File(sdpath + dirName);  
        dir.mkdir();  
        return dir;  
    } 
	
	public static File createSDFile(String fileName) throws IOException {
		String sdpath = Environment.getExternalStorageDirectory() + "/";
        File file = new File(sdpath + fileName);  
        file.createNewFile();  
        return file;  
    }
	
	public static File write2SDFromInput(String path, String fileName, InputStream input) { 
        File file = null;  
        OutputStream output = null;  
        try {
            createSDDir(path);
            file = createSDFile(path + fileName);
            output = new FileOutputStream(file);
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((input.read(buffer)) != -1) {
                output.write(buffer);  
            }
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
	
	/**
	* @Title: isFileExists
	* @Description: check if the file exist
	* @param @param context
	* @param @param fileName
	* @param @param fileSize
	* @param @return
	* @return boolean 
	* @throws
	*/
	public static boolean isFileExists(Context context, String fileName, long fileSize) {
		File file = new File(fileName);
		if (file.exists()) {
			if (file.length() == fileSize) {
				return true;
			}
		}
		return false;
	}
	
	 /**
     * install apk
     */
    public static void installApk(Context ctx, File file) {
//    	File apkfile = new File(file);
        if (!file.exists()) {
            return;
        }
        
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + file.toString()), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }
}
