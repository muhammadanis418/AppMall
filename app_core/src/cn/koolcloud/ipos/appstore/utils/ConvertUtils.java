package cn.koolcloud.ipos.appstore.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * <p>Title: ConvertUtils.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-10-30
 * @version 	
 */
public class ConvertUtils {
	
	/**
	* @Title: drawableToByte
	* @Description: TODO
	* @param @param drawable
	* @param @return
	* @return byte[] 
	* @throws
	*/
	public static byte[] drawableToByte(Drawable drawable) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
	
	/**
	* @Title: inputSreamToString
	* @Description: TODO
	* @param @param in
	* @param @return
	* @param @throws IOException
	* @return String 
	* @throws
	*/
	public static String inputSreamToString(InputStream in) throws IOException {
		StringBuffer put = new StringBuffer();
		byte[] size = new byte[2048];
		for (int length; (length = in.read(size)) != -1;) {
			put.append(new String(size, 0, length));
		}
		return put.toString();
	}
	
	/**
	* @Title: InputStreamToByte
	* @Description: TODO
	* @param @param is
	* @param @return
	* @return byte[] 
	* @throws
	*/
	public static byte[] InputStreamToByte(InputStream is) {
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		byte[] buffer = new byte[2048];
		int len;
		try {
			while ((len = is.read(buffer)) > 0) {
				bytestream.write(buffer,   0,   len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte imgdata[] = bytestream.toByteArray();
		try {
			bytestream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imgdata;
	}
	
	/**
	* @Title: bitmapToDrawable
	* @Description: TODO
	* @param @param bitmap
	* @param @return
	* @return Drawable 
	* @throws
	*/
	public static Drawable bitmapToDrawable(Bitmap bitmap) {
		@SuppressWarnings("deprecation")
		Drawable drawable = new BitmapDrawable(bitmap);
		return drawable;
	}
	
	/**
	* @Title: bitmapToBytes
	* @Description: TODO
	* @param @param bm
	* @param @return
	* @return byte[] 
	* @throws
	*/
	public static byte[] bitmapToBytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
	
	/**
	* @Title: Bytes2Bimap
	* @Description: TODO
	* @param @param b
	* @param @return
	* @return Bitmap 
	* @throws
	*/
	public static Bitmap Bytes2Bimap(byte[] b) {
		if (b.length != 0) {   
		    return BitmapFactory.decodeByteArray(b, 0, b.length);   
		} else {   
		    return null;   
		}   
	}  
	
	/**
	* @Title: resizeBitmap
	* @Description: TODO
	* @param @param bitmap
	* @param @param scale
	* @param @return
	* @return Bitmap 
	* @throws
	*/
	public static Bitmap resizeBitmap(Bitmap bitmap, float scale) {
		if (bitmap == null) {
			return null;
		}
        int bmpW = bitmap.getWidth();
        int bmpH = bitmap.getHeight();
        
        float scaleW = 1;
	    float scaleH = 1;
       
        scaleW = (float)(scaleW * scale);
        scaleH = (float)(scaleH * scale);
        
        Matrix mt = new Matrix();
        
        mt.postScale(scaleW, scaleH);
        
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bmpW, bmpH, mt, true);
        
        return resizeBmp;
        
	}
	
    /**
    * @Title: bytes2kb
    * @Description: TODO byte to kb or mb
    * @param @param bytes
    * @param @return
    * @return String 
    * @throws
    */
    public static String bytes2kb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP).floatValue();
        if (returnValue > 1) {
        	
        	return (returnValue + "MB");
        }
        BigDecimal kilobyte = new BigDecimal(1024);
        returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP).floatValue();  
        return (returnValue + "KB");
    }
    
    /**
     * @Title: longToDate
     * @Description: parse long to date
     * @param currentTime
     * @param formatType yyyy-MM-dd HH:mm:ss //yyyy年MM月dd�?HH时mm分ss�?
     * @return
     * @throws ParseException
     * @return: Date
     */
    public static Date longToDate(long currentTime, String formatType)
 			throws ParseException {
    	//generate date instance with current millis
 		Date dateOld = new Date(currentTime);
 		//parse date to string
 		String sDateTime = dateToString(dateOld, formatType);
 		//parse string to date
 		Date date = stringToDate(sDateTime, formatType);
 		return date;
 	}
    
    public static String longToString(long currentTime, String formatType) {
 		String strTime = null;
		try {
			Date date = longToDate(currentTime, formatType);
			strTime = dateToString(date, formatType);
		} catch (Exception e) {
			e.printStackTrace();
		}
 		return strTime;
 	}
    
    public static String dateToString(Date data, String formatType) {
 		return new SimpleDateFormat(formatType, Locale.getDefault()).format(data);
 	}
    
    public static Date stringToDate(String strTime, String formatType)
 			throws ParseException {
 		SimpleDateFormat formatter = new SimpleDateFormat(formatType, Locale.getDefault());
 		Date date = null;
 		date = formatter.parse(strTime);
 		return date;
 	}
}
