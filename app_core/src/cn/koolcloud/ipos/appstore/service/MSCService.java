package cn.koolcloud.ipos.appstore.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.StrictMode;
import cn.koolcloud.ipos.appstore.MainActivity;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.service.aidl.IMSCService;
import cn.koolcloud.ipos.appstore.service.aidl.ParcelableApp;
import cn.koolcloud.ipos.appstore.utils.MySPEdit;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.MyLog;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;

public class MSCService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return new ImplMSCService(); //return service instance.  
	}

	public class ImplMSCService extends IMSCService.Stub {
		
        @Override
        public ParcelableApp checkUpdate(String packageName, int versionCode) throws RemoteException {
        	
        	if (android.os.Build.VERSION.SDK_INT > 9) {
        	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        	    StrictMode.setThreadPolicy(policy);
        	}
        	
        	String terminalId = MySPEdit.getTerminalID(getApplicationContext());
        	JSONObject appObj = ApiService.checkSoftUpdate(terminalId, packageName, versionCode);
        	
        	ParcelableApp app = JsonUtils.parseJSONParcelApp(appObj);
        	return app;
        }
        
        @Override
        public void openAppDetail(ParcelableApp app) throws RemoteException {
        	if (app != null) {
        		MyLog.d("appName:" + app.getName());
        		List<App> list = new ArrayList<App>();
        		App normalApp = new App();
        		normalApp.setId(app.getId());
        		normalApp.setName(app.getName());
        		normalApp.setDate(app.getDate());
        		normalApp.setDownloadId(app.getDownloadId());
        		normalApp.setIcon(app.getIcon());
        		normalApp.setPackageName(app.getPackageName());
        		normalApp.setRating(app.getRating());
        		normalApp.setSize(app.getSize());
        		normalApp.setVendor(app.getVender());
        		normalApp.setVersion(app.getVersion());
        		normalApp.setVersionCode(app.getVersionCode());
        		
        		list.add(normalApp);
        		
        		Intent mIntent = new Intent(getApplicationContext(), MainActivity.class);
        		mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			Bundle mBundle = new Bundle();
    			mBundle.putSerializable(Constants.SER_KEY, (Serializable) list);
    			mBundle.putInt(Constants.APP_LIST_POSITION, 0);
    			mBundle.putString("NAME", "MSCService");
    			mIntent.putExtras(mBundle);
    			startActivity(mIntent);
    			
        	} else {
        		Looper.prepare();
        		ToastUtil.showToast(getApplicationContext(), R.string.msg_argument_error);
        		Looper.loop();
        		Looper.getMainLooper().quit();
        	}
        }
    }
}
