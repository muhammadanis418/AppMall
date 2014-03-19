package cn.koolcloud.ipos.appstore.entity;

import java.io.Serializable;

public class App implements Serializable {
	private static final long serialVersionUID = -3024435470949626422L;
	private String id;
	private String name;
	private String version;
	private String size;
	private String icon;
	private String downloadId;
	private String rating;
	private long date;
	private String vendor;
	private int versionCode;
	private String packageName;
	
	//add promotion properties
	private int type;						 //0£ºopen app details; 1£ºopen WebView with url
	private String img;
	private String url;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public App(String id, String name, String version, String size,
			String icon, String downloadId) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.size = size;
		this.icon = icon;
		this.setDownloadId(downloadId);
	}

	public App() {
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getDownloadId() {
		return downloadId;
	}

	public void setDownloadId(String downloadId) {
		this.downloadId = downloadId;
	}

	/**
	* @Title: getIconFileName
	* @Description: generate icon name
	* @param @return icon_id_.png eg:647_152_.png
	* @return String 
	* @throws
	*/
	public String getIconFileName() {
		return this.getIcon() + "_" + this.getId() + "_"  + ".png";
	}
	
	/**
	 * @Title: getSnapShortImageName
	 * @Description: generate gallery image name
	 * @param snapImgId
	 * @return
	 * @return: String
	 */
	public String getSnapShortImageName(String snapImgId) {
		return snapImgId + "_" + this.name + "_" + this.id + "_" + ".png"; 
	}
	
	/**
	 * @Title: getAdPromotionImageName
	 * @Description: generate ad image name
	 * @return
	 * @return: String
	 */
	public String getAdPromotionImageName() {
		return img + "_" + this.name + "_" + this.id + "_" + ".png"; 
	}
}