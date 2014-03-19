package cn.koolcloud.ipos.appstore.cache;

import android.content.Context;

/**
 * some operate need executing long times on background(e.g : get xml file)
 *  show progress dialog when during download,
 *  if loaded, call back the result via callback method for invoker .
 *	@author Teddy
 * 	@Create Date:2013-10-29
 *
 */
public interface BindDataIf {
    
    public interface Callback {
        
        /**
         * normal, not to invoke main thread (UI thread)
         * @param obj
         */
        public void callback(BindHolder holder);
    }
    
    public class BindHolder{
        
        public static final int TYPE_IMAGE = 0X0001;
        public static final int TYPE_JSON = 0X0002;
        public static final int TYPE_XML = 0X0003;
        public static final int TYPE_MEDIA = 0X0004;
        
        private int type;
        private String url;
        private String saveFileName;
        private Object resource;
        
        public BindHolder(int type, String url) {
			this.type = type;
			this.url = url;
		}
        
        public BindHolder(int type, String url, String saveFileName) {
			this.type = type;
			this.url = url;
			this.saveFileName = saveFileName;
		}

		public int getType() {
            return type;
        }
       
        public void setType(int type) {
            this.type = type;
        }
       
        public String getUrl() {
            return url;
        }
       
        public void setUrl(String url) {
            this.url = url;
        }

		public Object getResource() {
			return resource;
		}

		public void setResource(Object resource) {
			this.resource = resource;
		}

		public String getSaveFileName() {
			return saveFileName;
		}

		public void setSaveFileName(String saveFileName) {
			this.saveFileName = saveFileName;
		}
    }
    
	@Deprecated
    public void bindData(Context context, BindHolder holder, Callback callback, boolean useNewThread, boolean forceDownload);
}
