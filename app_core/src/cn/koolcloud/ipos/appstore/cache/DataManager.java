package cn.koolcloud.ipos.appstore.cache;

import android.content.Context;

public class DataManager implements BindDataIf {
	
	/* 
	 * This constructor was deprecated from 2013-10-03. 
	 * please use ImageDownloader.getInstance(context).download(String url, Bitmap defaultBitmap, ImageView imageView);
	 */
	@Override
	@Deprecated
	public void bindData(final Context context, final BindHolder holder, final Callback callback, boolean useNewThread, final boolean forceDownload) {
		int type = holder.getType();
		final String url = holder.getUrl();
		if(null == url || "".equals(url) || "null".equals(url)) {
			holder.setResource(null);
			callback.callback(holder);
			return ;
		}
		if(useNewThread) {
			if(type == BindHolder.TYPE_IMAGE) {
				ImageDownloader.getInstance(context).download(holder, callback);
			} 
		}
	}
}
