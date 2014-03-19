package cn.koolcloud.ipos.appstore.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.entity.AppInfo;
import cn.koolcloud.ipos.appstore.utils.ConvertUtils;
import cn.koolcloud.ipos.appstore.utils.Env;


/**
 * <p>Title: LocalSoftListAdapter.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-11-5
 * @version 	
 */
public class LocalSoftListAdapter extends BaseAdapter {
	private List<AppInfo> dataList = new ArrayList<AppInfo>();
	private Context ctx;
	private LayoutInflater mInflater;

	public LocalSoftListAdapter(Context context, List<AppInfo> dataSource) {
		this.ctx = context;
		dataList = dataSource;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return dataList.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		SoftItemViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.local_software_list_item, null);
			holder = new SoftItemViewHolder();
			holder.IconImageView = (ImageView) convertView.findViewById(R.id.software_icon);	
			holder.softwareVersionTextView = (TextView) convertView.findViewById(R.id.software_version);							
			holder.softwareNameTextView = (TextView) convertView.findViewById(R.id.software_item_name);
			holder.softwareSizeTextView = (TextView) convertView.findViewById(R.id.software_size);
			holder.uninstallButton = (Button) convertView.findViewById(R.id.bt_uninstall);
			convertView.setTag(holder);
		} else {
			holder = (SoftItemViewHolder) convertView.getTag();
		}

		final AppInfo appInfo = dataList.get(position);
		holder.IconImageView.setImageDrawable(appInfo.getIcon());
		holder.softwareNameTextView.setText(appInfo.getName());
		holder.softwareSizeTextView.setText(ConvertUtils.bytes2kb(appInfo.getSoftSize()));
		holder.softwareVersionTextView.setText(appInfo.getVersionName());
		
		holder.uninstallButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Env.uninstallApp(ctx, appInfo.getPackageName());
			}
		});

		return convertView;
	}
	
	class SoftItemViewHolder {
		ImageView IconImageView;
		TextView softwareNameTextView;
		TextView softwareVersionTextView;
		TextView softwareSizeTextView;
		Button uninstallButton;
	}
}
