package cn.koolcloud.ipos.appstore.entity;

import android.graphics.drawable.Drawable;

public class AppInfo {

	private String name;
	private String packageName;
	private String versionName;
	private long softSize;
	private int versionCode;
	private Drawable icon;
	
	public long getSoftSize() {
		return softSize;
	}
	public void setSoftSize(long softSize) {
		this.softSize = softSize;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getVersionName() {
		return versionName;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
	public int getVersionCode() {
		return versionCode;
	}
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
}
