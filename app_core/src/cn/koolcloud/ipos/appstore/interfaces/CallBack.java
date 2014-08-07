package cn.koolcloud.ipos.appstore.interfaces;

import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class CallBack {

	public void onStart() {

	}

	public void onSuccess(String response) {
	}

	public void onSuccess(JSONArray response) {

	}

	public void onSuccess(JSONObject response) {

	}

	public void onSuccess(InputStream response) {

	}

	public void onFailure(String msg) {

	}

	public void onCancelled() {

	}

}