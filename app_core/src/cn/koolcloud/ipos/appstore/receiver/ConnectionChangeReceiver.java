package cn.koolcloud.ipos.appstore.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.service.MSCService;
import cn.koolcloud.ipos.appstore.service.RegisterService;
import cn.koolcloud.ipos.appstore.utils.NetUtil;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;

public class ConnectionChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (NetUtil.isAvailable(context)) {
			//TODO:start check appstore update service
			ToastUtil.showToast(context, R.string.network_prompt_net);
			Intent service = new Intent(context, MSCService.class);
	        context.startService(service);
	        
	        Intent registerService = new Intent(context, RegisterService.class);
	        context.startService(registerService);
		} else {
			ToastUtil.showToast(context, R.string.nonetwork_prompt_check);
		}
	}
}
