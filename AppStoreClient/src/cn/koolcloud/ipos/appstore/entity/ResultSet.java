package cn.koolcloud.ipos.appstore.entity;

public enum ResultSet {
	
	SUCCESS(0, "success"),
	FAIL(-1, "fail"),
	NET_ERROR(100, "server network error!"),
	CURRENT_NET_NOT_ALLOWED(101, "network error"),
	SERVER_ERROR(3,"server response  error! "),
	NET_ENCODING_ERROR(4,"encoding error"),
	LOGIN_AGAIN(5,"please login again ! "),
	UNKNOW_ERROR(2, "Unknown Error");
	
	
	
	public int retcode;
	public String describe;
	public Object dataObject;
	
	ResultSet(int retcode,String describe) {
		  this.retcode = retcode;
	      this.describe = describe;
	}
	
//	public ResultSet() {
//	}
//	
	ResultSet(int retcode, String describe, Object dataObject) {
		this.retcode = retcode;
		this.describe = describe;
		this.dataObject = dataObject;
	}
	
	public int getRetcode() {
		return retcode;
	}
	public void setRetcode(int retcode) {
		this.retcode = retcode;
	}
	public String getDescribe() {
		return describe;
	}
	public void setDescribe(String describe) {
		this.describe = describe;
	}
	public Object getDataObject() {
		return dataObject;
	}
	public void setDataObject(Object dataObject) {
		this.dataObject = dataObject;
	}
	
	
    public static class Response {
    	
    	public static final String OK = "OK";
    	public static final String COMPLETION_STATUS_SUCCESS = "1.0";
    	
    	
    	
    	public static final String RETCODE = "retcode";
        public static final String DESCRIBE = "describe";
        public static final String RESPONSE_TYPE = "responseType";
        public static final String CONTENT = "content";
        public static final String PROCESSED_RETCODE = "processed_retcode";
        public static final String PROCESSED_DATA = "processed_data";
        public static final String DATAOBJECT = "dataObject";
        public static final String HEADER = "head";
        public static final String STATUS = "status";
        public static final String ERROR = "error";
        public static final String MESSAGE = "message";
        public static final String APPSESSIONID = "appSessionId";
        public static final String TASKID = "taskId";
        public static final String COMPLETION = "completion";
        public static final String RUNAPPDATA = "runAppData";
        public static final String PAYLOAD = "payload";
        public static final String DATA = "data";
        public static final String APPMETADATAID = "appMetadataId";
        public static final String ONTOLOGY_METADATAID = "ontologyMetadataId";
        public static final String RESOURCE = "resource";
        public static final String VALUE = "value";
        public static final String LISTCONTEXT = "listContext";
        public static final String LISTAPMETADATA = "listAppMetadata";
        public static final String CONTEXTID = "contextId";
        public static final String DATACONTROLLERID = "dataControllerId";
		public static final String NULL 	= "null";
		public static final String STYLE 	= "style";
        
    }

    public static class RESPONSETYPE {
    	public static final int RESPONSE_TYPE_JSON_ARRAY = 0;
        public static final int RESPONSE_TYPE_JSON_OBJECT = 1;
        public static final int RESPONSE_TYPE_STREAM = 2;
    }
    
}
