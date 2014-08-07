package cn.koolcloud.ipos.appstore.download.multithread;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import cn.koolcloud.ipos.appstore.utils.MyLog;

public class FileDownloader {
	private Context context;
	private DownloadRecord record;
	
	/* 已下载文件长度 */
	private int downloadSize = 0;
	
	/* 原始文件长度 */
	private int fileSize = 0;
	
	/* 多线程单元 */
	private MultiThreadUnit mtUnit = new MultiThreadUnit();
	
	/* 本地保存文件 */
	private File saveFile;
	
	/* 缓存各线程下载的长度*/
	private Map<Integer, Integer> data = new ConcurrentHashMap<Integer, Integer>();
	
	/* 每条线程下载的长度 */
	private int block;
	
	/* 下载路径  */
	private String downloadUrl;
	
	/* 更新下载进度的毫秒间隔  */
	private int millisecond = 1000;

	private String paramJson;
	private boolean notFinish = true;//下载未完成

	/**
	 * 获取线程数
	 */
	public int getThreadSize() {
		return mtUnit.threads.length;
	}
	
	/**
	 * 获取文件大小
	 * @return
	 */
	public int getFileSize() {
		return fileSize;
	}

	
	/**
	 * 累计已下载大小
	 * @param size
	 */
	protected synchronized void append(int size) {
		downloadSize += size;
	}
	
	/**
	 * 更新指定线程最后下载的位置
	 * @param threadId 线程id
	 * @param pos 最后下载的位置
	 */
	protected synchronized void update(int threadId, int pos) {
		this.data.put(threadId, pos);
		this.record.update(this.paramJson, this.data);
	}
	
	private void writeStream(OutputStream os, String jsonString)
			throws IOException {
//		StringEntity stringEntity = new StringEntity(jsonString, HTTP.UTF_8);
//		stringEntity.setContentEncoding("UTF-8");
		
		DataOutputStream out = new DataOutputStream(os);
		out.write(jsonString.getBytes());
		MyLog.i(jsonString);
		out.flush();
		out.close();
	}

	/**
	 * 构建文件下载器
	 * @param downloadUrl 下载路径
	 * @param fileSaveDir 文件保存目录
	 * @param threadNum 下载线程数
	 */
	public FileDownloader(Context context, String downloadUrl, File fileSaveDir, int threadNum,
			String paramJson, String saveName) {
		this.paramJson = paramJson;
		try {
			MyLog.e("fileSaveDir.exists():"+fileSaveDir.exists());
			if(!fileSaveDir.exists()) fileSaveDir.mkdirs();

			this.downloadUrl = downloadUrl;
			URL url = new URL(this.downloadUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5*1000);
			conn.setRequestMethod("POST");
//			conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, " +
//					"application/x-shockwave-flash, application/xaml+xml, " +
//					"application/vnd.ms-xpsdocument, application/x-ms-xbap, " +
//					"application/x-ms-application, application/vnd.ms-excel, " +
//					"application/vnd.ms-powerpoint, application/msword, application/octet-stream, */*");
//			conn.setRequestProperty("Accept-Language", "zh-CN");
//			conn.setRequestProperty("Referer", downloadUrl); 
//			conn.setRequestProperty("Charset", "UTF-8");
//			conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; " +
//					"Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; " +
//					".NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
//			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Accept-Encoding", "identity");
			conn.setRequestProperty("Content-Type", "application/json;charset=UTF8");
			
			writeStream(conn.getOutputStream(), paramJson);
			
			conn.connect();
			printResponseHeader(conn);
			
			int resCode = conn.getResponseCode();
			MyLog.e("resCode:"+resCode);
			if (resCode==200) {
				this.fileSize = conn.getContentLength();//根据响应获取文件大小
				MyLog.e("this.fileSize:"+this.fileSize);
				if (this.fileSize <= 0) throw new RuntimeException("Unkown file size ");
						
//				String filename = getFileName(conn);//获取文件名称
				this.saveFile = new File(fileSaveDir, saveName);//构建保存文件
				if(!this.saveFile.exists()) {
					this.saveFile.createNewFile();
				}
				this.context = context;
				record = DownloadRecord.getInstance(this.context);
				Map<Integer, Integer> logdata = record.getData(paramJson);//获取下载记录
				
				if(logdata.size() > 0){//如果存在下载记录
					for(Map.Entry<Integer, Integer> entry : logdata.entrySet())
						data.put(entry.getKey(), entry.getValue());//把各条线程已经下载的数据长度放入data中
					threadNum = data.size();
				}

				this.mtUnit.threads = new DownloadThread[threadNum];
				if(this.data.size() == this.mtUnit.threads.length){//下面计算所有线程已经下载的数据长度
					for (int i = 0; i < this.mtUnit.threads.length; i++) {
						this.downloadSize += this.data.get(i+1);
					}
					print("已经下载的长度"+ this.downloadSize);
				}
				else
				{
					this.data.clear();
					for (int i = 0; i < this.mtUnit.threads.length; i++) {
						this.data.put(i+1, 0);//初始化每条线程已经下载的数据长度为0
					}
					this.downloadSize = 0;
				}

				//计算每条线程下载的数据长度
				this.block = (this.fileSize % this.mtUnit.threads.length)==0? this.fileSize / this.mtUnit.threads.length 
						: this.fileSize / this.mtUnit.threads.length + 1;
				print("block的长度"+ this.block);
			}else{
				throw new RuntimeException("server no response ");
			}
		} catch (Exception e) {
			print(e.toString());
			e.printStackTrace();
			throw new RuntimeException("don't connection this url");
		}
	}
	
	/**
	 *  开始下载文件
	 * @param listener 监听下载数量的变化,如果不需要了解实时下载的数量,可以设置为null
	 * @return 已下载文件大小
	 * @throws Exception
	 */
	public int download(final DownloadProgressListener listener) throws Exception{
		try {
			MyLog.e("this.saveFile:"+this.saveFile.getAbsolutePath()+
					"\nthis.saveFile.isDirectory():"+this.saveFile.isDirectory());
			RandomAccessFile randOut = new RandomAccessFile(this.saveFile, "rw");
			if(this.fileSize > 0) randOut.setLength(this.fileSize);
			randOut.close();
			final URL url = new URL(this.downloadUrl);
			
			for (int i = 0; i < this.mtUnit.threads.length; i++) {//开启线程进行下载
				int downLength = this.data.get(i+1);
				
				if(downLength < this.block && this.downloadSize < this.fileSize){//判断线程是否已经完成下载,否则继续下载	
					this.mtUnit.threads[i] = new DownloadThread(this, url, this.saveFile, this.block,
							this.data.get(i+1), i+1, paramJson, mtUnit);
					this.mtUnit.threads[i].setPriority(7);
					this.mtUnit.threads[i].start();
				}else{
					this.mtUnit.threads[i] = null;
				}
			}

			this.record.insert(this.paramJson, this.data);
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (notFinish) {// 循环判断所有线程是否完成下载，不下载完不会结束。
						try {
							Thread.sleep(millisecond);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						notFinish = false;//假定全部线程下载完成
						for (int i = 0; i < mtUnit.threads.length; i++){
							if (mtUnit.threads[i] != null && !mtUnit.threads[i].isFinish()) {//如果发现线程未完成下载
								notFinish = true;//设置标志为下载没有完成
								
								if(mtUnit.threads[i].getDownLength() == -1){//如果下载失败,再重新下载
									mtUnit.threads[i] = new DownloadThread(FileDownloader.this, url, saveFile, block,
											data.get(i+1), i+1, paramJson, mtUnit);
									mtUnit.threads[i].setPriority(7);
									mtUnit.threads[i].start();
								}
							}
						}
						
						if(listener!=null) listener.onDownloadSize(downloadSize);//通知目前已经下载完成的数据长度
						if(mtUnit.isCancel()) {
							return;
						}
					}

					record.delete(paramJson);
				}
			}).start();
		} catch (Exception e) {
			print(e.toString());
			e.printStackTrace();
			throw new Exception("file download fail");
		}
		return this.downloadSize;
	}
	
	public void start()
	{
		mtUnit.setCancel(false);
	}

	public void pause()
	{
		mtUnit.setCancel(true);
	}

	public void cancel()
	{
		pause();
	}

	/**
	 * 获取Http响应头字段
	 * @param http
	 * @return
	 */
	public static Map<String, String> getHttpResponseHeader(HttpURLConnection http) {
		Map<String, String> header = new LinkedHashMap<String, String>();
		
		for (int i = 0;; i++) {
			String mine = http.getHeaderField(i);
			if (mine == null) break;
			header.put(http.getHeaderFieldKey(i), mine);
		}
		
		return header;
	}
	
	/**
	 * 打印Http头字段
	 * @param http
	 */
	public static void printResponseHeader(HttpURLConnection http){
		Map<String, String> header = getHttpResponseHeader(http);
		
		for(Map.Entry<String, String> entry : header.entrySet()){
			String key = entry.getKey()!=null ? entry.getKey()+ ":" : "";
			print(key+ entry.getValue());
		}
	}

	/**
	 * 打印日志信息
	 * @param msg
	 */
	private static void print(String msg){
		MyLog.i(msg);
	}
}
