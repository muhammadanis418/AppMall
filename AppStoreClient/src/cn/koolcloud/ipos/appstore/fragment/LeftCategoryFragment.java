package cn.koolcloud.ipos.appstore.fragment;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.adapter.SubCategoryListAdapter;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.entity.Category;
import cn.koolcloud.ipos.appstore.fragment.base.BaseFragment;
import cn.koolcloud.ipos.appstore.fragment.tab.NoNetworkFragment;
import cn.koolcloud.ipos.appstore.fragment.tab.NormalListFragment;
import cn.koolcloud.ipos.appstore.utils.Logger;
import cn.koolcloud.ipos.appstore.utils.NetUtil;

public class LeftCategoryFragment extends BaseFragment implements OnItemClickListener {
	private static final String TAG = "LeftCategoryFragment";
	
	private ListView categoryListView;
	
	private List<Category> categoryDataSource = null;	//category data source.
	private int currentSelectedPosition = 0;			//current position tag
	private SubCategoryListAdapter adapter = null;
	
	private FragmentManager fragManager;

	public static LeftCategoryFragment getInstance() {
		LeftCategoryFragment leftCategoryFragment = new LeftCategoryFragment();
		//get params
		return leftCategoryFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		categoryDataSource = (List<Category>) getArguments().getSerializable(Constants.SER_KEY);
		currentSelectedPosition = getArguments().getInt(Constants.CATEGORY_LIST_POSITION);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.left_category_list, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		fragManager = getFragmentManager();
		categoryListView = (ListView) getActivity().findViewById(R.id.listView);
		Logger.d("onActivityCreated");
		categoryListView.setOnItemClickListener(this);
		
		//init status
		showCategoryFragment(currentSelectedPosition);
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		initListViewAdapter();
		
	}

	private void initListViewAdapter() {
		adapter = new SubCategoryListAdapter(getActivity(), categoryDataSource, currentSelectedPosition);
		categoryListView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		// TODO Auto-generated method stub
		//recovery the selected item status
		Logger.d("onItemClick method is invoked");
		View rootView = categoryListView.getChildAt(currentSelectedPosition);
		View selectedView = rootView.findViewById(R.id.left_nav_item_root);
		selectedView.setSelected(false);
		rootView.findViewById(R.id.indicator).setVisibility(View.GONE);
		
		//set selected status on the clicking item
		view.findViewById(R.id.left_nav_item_root).setSelected(true);
		view.findViewById(R.id.indicator).setVisibility(View.VISIBLE);
		currentSelectedPosition = position;
		
		//TODO send selected category to content frame and display general app fragment
		showCategoryFragment(position);
	}
	
	//show local soft fragment
	private void showCategoryFragment(int pos) {
		//fragment management
		FragmentTransaction fragTransaction = fragManager.beginTransaction();
		if (NetUtil.isAvailable(getActivity())) {
			
			NormalListFragment normalAppFragment = NormalListFragment.getInstance();
			Bundle args = new Bundle();
			Category category = categoryDataSource.get(pos);
			args.putSerializable(Constants.SER_KEY, category);
			normalAppFragment.setArguments(args);
			
			fragTransaction.replace(R.id.frame_content, normalAppFragment);
		} else {
			NoNetworkFragment noNetFragment = NoNetworkFragment.getInstance();
			fragTransaction.replace(R.id.frame_content, noNetFragment);
			
		}
		fragTransaction.commit();
			
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
	
}
