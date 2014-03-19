package cn.koolcloud.ipos.appstore.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.koolcloud.ipos.appstore.service.MSCService;
import cn.koolcloud.ipos.appstore.service.RegisterService;

public class BootBroadCastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

         /*start service on boot completed*/
         Intent service = new Intent(context, MSCService.class);
         context.startService(service);
         
         Intent registerService = new Intent(context, RegisterService.class);
         context.startService(registerService);
           
         /*start Activity on boot completed*/
         /*Intent activityIntent = new Intent(context, MyActivity.class);  
         activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );	//this flag must be added otherwise export error
         context.startActivity(activityIntent);*/
  
        /* start app on boot completed */  
        /*Intent appIntent = context.getPackageManager().getLaunchIntentForPackage("com.allinpay.ipos.appstore");  
        context.startActivity(appIntent);*/
	}

}
