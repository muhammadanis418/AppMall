package cn.koolcloud.ipos.appstore.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * <p>Title: MD5.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-11-11
 * @version 	
 */
public class MD5 {
    
    //public static final String server_addr = "http://192.168.1.103:8004/";
	
    //public static final String server_addr = "http://140.206.112.78:8006/";
    
    public static final String formatter ="xml";// "xml";//json
    
    public static final String formatter_json = "json";//json
    
    public static final String formatter_xml = "xml";//json
    
    
    public static String getMD5Str(String str) {
        MessageDigest messageDigest = null;
        
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            
            messageDigest.reset();
            
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
        	MyLog.d("NoSuchAlgorithmException caught!");
            System.exit(-1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        byte[] byteArray = messageDigest.digest();
        
        StringBuffer md5StrBuff = new StringBuffer();
        
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0")
                        .append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        
        return md5StrBuff.toString();
    }
    
    /*public static String getSessionId() throws Exception{
        return Utils.parseOutSessionId(LoginTestHtml.login());
    }*/
    

//	    public static String parseOutSessionId(WebResponse response) throws ParserConfigurationException, SAXException, IOException{
//	        if(formatter.equals("xml")){
//	            InputStream is = response.getContentAsStream();
//	            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//	            Document document = documentBuilder.parse(is);
//	            NodeList nodeList = document.getElementsByTagName("session_id");
//	            
//	            if(nodeList.getLength() > 0){
//	                Node sessionIdNode = nodeList.item(0);
//	                return sessionIdNode.getTextContent();
//	            }
//	        }
//	        else{
//	            
//	        }
//	        return null;
//	    }
    
    public static String sortParameters(List<String> parameters){
        Set<String> sort = new TreeSet<String>();
        for(String p : parameters){
            sort.add(p);
        }
        String ret = "";
        for(String s : sort){
            ret = ret.concat(s);
        }
        return ret;
    }
    
    public static  List<NameValuePair> appendParameters(List<NameValuePair> list, boolean type) {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        NameValuePair sign_method = new BasicNameValuePair("sign_method","md5");
        NameValuePair format = null;
        if (type) {
        	format = new BasicNameValuePair("format", formatter_json);
        } else {
        	format = new BasicNameValuePair("format", formatter_xml);
        }
        NameValuePair time = new BasicNameValuePair("time", ""+new Date().getTime());
        
        parameters.add(sign_method);
        parameters.add(format);
        parameters.add(time);
        parameters.addAll(list);
        
        List<String> pList = new ArrayList<String>();
        for(NameValuePair p : parameters){
            pList.add(p.getName()+"="+p.getValue());
        }
        String signstr = sortParameters(pList);
        signstr = getMD5Str(signstr);
        NameValuePair sign = new BasicNameValuePair("sign",signstr);
        parameters.add(sign);
        return parameters;
    }
    public static List<NameValuePair> appendAppsParameters(List<NameValuePair> list){
    	return appendParameters(list, false);
    }
        
    
    public static List<NameValuePair> appendAppsParametersJSON(List<NameValuePair> list) {
    	return appendParameters(list, true);
    }
    
    public static void main(String[] args){
        System.out.println(getMD5Str("account=testage=10format=xmlgender=malelocation=ZH_CNmail=feng.wei.355300@gmail.comnickName=fweipassword=testsign_method=md5time=1341373791611"));
    }
}