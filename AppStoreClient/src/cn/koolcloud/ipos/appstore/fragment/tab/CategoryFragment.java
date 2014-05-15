package cn.koolcloud.ipos.appstore.fragment.tab;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.Toast;
import cn.koolcloud.ipos.appstore.AppStorePreference;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.adapter.CategoryListAdapter;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.cache.ImageDownloader;
import cn.koolcloud.ipos.appstore.cache.database.CacheDB;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.entity.Category;
import cn.koolcloud.ipos.appstore.entity.ResultSet;
import cn.koolcloud.ipos.appstore.fragment.base.BaseFragment;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.ui.CategoryMainActivity;
import cn.koolcloud.ipos.appstore.ui.SoftwareDetailActivity;
import cn.koolcloud.ipos.appstore.ui.WebViewActivity;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.Logger;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;
import cn.koolcloud.ipos.appstore.utils.Utils;
import cn.koolcloud.ipos.appstore.views.AppStoreListView;
import cn.koolcloud.ipos.appstore.views.AppStoreListView.AppstoreListViewListener;

public class CategoryFragment extends BaseFragment implements OnItemClickListener, AppstoreListViewListener, View.OnClickListener {
	private static final String TAG = "CategoryFragment";
	private static final int HANDLE_CACHE_CATEGORY_TO_SHOW = 0;
	private static final int HANDLE_CACHE_AD_AND_SHOW = 1;

	private LayoutInflater inflater;										//view inflater
	private AppStoreListView categoryListView;								//the list view of local software
	private ImageView adOneImageView;
	private ImageView adTwoImageView;
	private ImageView adThreeImageView;
	private ImageView adFourImageView;
	private ImageView adFiveImageView;
	
	private CategoryListAdapter mCategoryListAdapter;						//adapter for category list view
	
	private List<Category> categoryDataSource = new ArrayList<Category>();	//category data source.
	private List<App> adPromotionDataSource = new ArrayList<App>();	//promotion data source.
	
	private int position = 0;												//promotion current position
	
	public static CategoryFragment getInstance() {
		CategoryFragment categoryFragment = new CategoryFragment();
		//save params
//		Bundle args = new Bundle();
//		args.putInt("index", index);
//		localSoftFragment.setArguments(args);
		return categoryFragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflate, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.inflater = inflate;
		return inflate.inflate(R.layout.category_content_list, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		initViews();
		new GetCacheCategoryThread().start();
		
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		getAppCategories();
		new GetCacheAdPromotionThread().start();
	}

	private void initViews() {
		categoryListView = (AppStoreListView) getActivity().findViewById(R.id.contentListView);
		categoryListView.setOnItemClickListener(this);
		categoryListView.setPullLoadEnable(false);
		categoryListView.setPullRefreshEnable(true);
		categoryListView.setAppStoreListViewListener(this);
		
		adOneImageView = (ImageView) getActivity().findViewById(R.id.adOneImageView);
		adOneImageView.setOnClickListener(this);
		adTwoImageView = (ImageView) getActivity().findViewById(R.id.adTwoImageView);
		adTwoImageView.setOnClickListener(this);
		adThreeImageView = (ImageView) getActivity().findViewById(R.id.adThreeImageView);
		adThreeImageView.setOnClickListener(this);
		adFourImageView = (ImageView) getActivity().findViewById(R.id.adFourImageView);
		adFourImageView.setOnClickListener(this);
//		adFiveImageView = (ImageView) getActivity().findViewById(R.id.adFiveImageView);
		
	}
	
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_CACHE_CATEGORY_TO_SHOW:
				mCategoryListAdapter = new CategoryListAdapter(application, categoryDataSource);
				categoryListView.setAdapter(mCategoryListAdapter);
				mCategoryListAdapter.notifyDataSetChanged();
				break;

			case HANDLE_CACHE_AD_AND_SHOW:
				initAdPromotion();
				//get ad promotion from server
				getAdPromotion();
				break;
			default:
				break;
			}
		}
		
	};
	
	private void initAdPromotion() {
		if (isAdded()) {
			
			if (adPromotionDataSource != null && adPromotionDataSource.size() > 0) {
				Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pic_ad_default);
				for (int i = 0; i < adPromotionDataSource.size(); i++) {
					App app = adPromotionDataSource.get(i);
					
					switch (i) {
					case 0:
						ImageDownloader.getInstance(application).download(app.getAdPromotionImageName(), defaultBitmap, adOneImageView);
						break;
					case 1:
						ImageDownloader.getInstance(application).download(app.getAdPromotionImageName(), defaultBitmap, adTwoImageView);
						break;
					case 2:
						ImageDownloader.getInstance(application).download(app.getAdPromotionImageName(), defaultBitmap, adThreeImageView);
						break;
					case 3:
						ImageDownloader.getInstance(application).download(app.getAdPromotionImageName(), defaultBitmap, adFourImageView);
						break;
						
					default:
						break;
					}
				}
			}
		}
	}
	
	private void getAppCategories() {
		ApiService.getAppCategories(getActivity(), AppStorePreference.getTerminalID(getActivity()), 
				AppStorePreference.getCategoryHash(getActivity()), getAppCategoriesCallBack);
	}
	
	//get categories call back
	private CallBack getAppCategoriesCallBack = new CallBack() {
		@Override
		public void onCancelled() {
			dismissLoading();
		}

		@Override
		public void onStart() {
			showLoading();
		}

		@Override
		public void onSuccess(JSONObject jsonObj) {
			try {
				String retCode = "";
				String data = "";
				
				Logger.d("-------getCategoryInfo=" + jsonObj.toString());
				
				retCode = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_STATUS);
				data = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_DATA);
				
				/*if (!Constants.REQUEST_STATUS_OK.equals(retCode)) {
					onFailure(data);
				} else {
					if (TextUtils.isEmpty(data)) {
						onFailure(Utils.getResourceString(getApplicationContext(), R.string.nonetwork_prompt_server_error));
					} else {
						JSONObject dataJson = JsonUtils.getJSONObject(jsonObj, Constants.REQUEST_DATA);
						
						if (AppStorePreference.getCategoryHash(getApplicationContext()).equals(JsonUtils.getStringValue(dataJson,
								Constants.REQUEST_HASH))) {
							//TODO: no need to update category but local cache category data
							mCategoryListAdapter = new CategoryListAdapter(getApplicationContext(), categoryDataSource);
						} else {
							
							categoryDataSource = JsonUtils.parseJSONCategories(jsonObj);
							mCategoryListAdapter = new CategoryListAdapter(getApplicationContext(), categoryDataSource);
							categoryListView.setAdapter(mCategoryListAdapter);
							mCategoryListAdapter.notifyDataSetChanged();
						}
						
						//save category hash
						AppStorePreference.saveCategoryHash(getApplicationContext(), JsonUtils.getStringValue(dataJson,
								Constants.REQUEST_HASH));
						
					}
				}*/
				categoryDataSource = JsonUtils.parseJSONCategories(jsonObj);
				mCategoryListAdapter = new CategoryListAdapter(application, categoryDataSource);
				categoryListView.setAdapter(mCategoryListAdapter);
				mCategoryListAdapter.notifyDataSetChanged();
				
				//cache category to database
				new InsertCategoryThread().start();
				dismissLoading();
			} catch (Exception e) {
				e.printStackTrace();
				onFailure("category response error!");
			}
		}

		@Override
		public void onFailure(String msg) {
			dismissLoading();
			Logger.d("describe=" + msg);
			if (msg.contains(ResultSet.NET_ERROR.describe)) {
				if (isAdded()) {
					ToastUtil.showToast(application, R.string.nonetwork_prompt_server_error, Toast.LENGTH_LONG);
		        }
			}
		}
	};
	
	private void getAdPromotion() {
		ApiService.getPromotions(application, AppStorePreference.getTerminalID(application), getAdPromotionCallBack);
	}
	
	//get Ad Promotion call back
	private CallBack getAdPromotionCallBack = new CallBack() {
		@Override
		public void onCancelled() {
//			dismissLoading();
		}
		
		@Override
		public void onStart() {
//			showLoading();
		}
		
		@Override
		public void onSuccess(JSONObject jsonObj) {
			try {
				String retCode = "";
				int id = 0;
				JSONObject dataJson = null;
				
				Logger.d("-------getAdPromotionInfo=" + jsonObj.toString());
				
				retCode = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_STATUS);
				dataJson = JsonUtils.getJSONObject(jsonObj, Constants.REQUEST_DATA);
				id = JsonUtils.getIntValue(dataJson, Constants.JSON_KEY_ID);
				AppStorePreference.savePromotinDataID(application, id);
				
				List<App> tmpPromotion = JsonUtils.parseJSONAdPromotionApps(jsonObj);
				if (tmpPromotion != null && tmpPromotion.size() > 0) {
					adPromotionDataSource = tmpPromotion;
					if (adPromotionDataSource != null && adPromotionDataSource.size() > 0) {
						initAdPromotion();
						new CacheAdPromotionThread().start();
					}
				}
//				dismissLoading();
			} catch (Exception e) {
				e.printStackTrace();
				onFailure("getAdPromotion response error!");
			}
		}
		
		@Override
		public void onFailure(String msg) {
			dismissLoading();
			Logger.d("describe=" + msg);
			if (msg.contains(ResultSet.NET_ERROR.describe)) {
				if(isAdded()){
					ToastUtil.showToast(application, R.string.nonetwork_prompt_server_error, Toast.LENGTH_LONG);
				}
			}
		}
	};

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		if (position > 0) {
			
			Intent mIntent = new Intent(getActivity(), CategoryMainActivity.class);
			Bundle mBundle = new Bundle();
			mBundle.putSerializable(Constants.SER_KEY, (Serializable) categoryDataSource);
			mBundle.putInt(Constants.CATEGORY_LIST_POSITION, --position);	//ListView header is on first position so to remove one
			mIntent.putExtras(mBundle);
			startActivity(mIntent);
		}
	}
	
	class InsertCategoryThread extends Thread {

		@Override
		public void run() {
			CacheDB cacheDB = CacheDB.getInstance(application);
			cacheDB.insertCategories(categoryDataSource);
		}
	}
	
	class CacheAdPromotionThread extends Thread {
		
		@Override
		public void run() {
			CacheDB cacheDB = CacheDB.getInstance(application);
			cacheDB.insertAdPromotions(adPromotionDataSource);
			cacheDB.insertApps(adPromotionDataSource, null);
		}
	}
	
	class GetCacheCategoryThread extends Thread {

		@Override
		public void run() {
			CacheDB cacheDB = CacheDB.getInstance(application);
			categoryDataSource = cacheDB.selectAllCategories();
			Message msg = mHandler.obtainMessage();
			msg.what = HANDLE_CACHE_CATEGORY_TO_SHOW;
			mHandler.sendMessage(msg);
		}
		
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		CacheDB.getInstance(application).closeDB();
	}

	class GetCacheAdPromotionThread extends Thread {
		
		@Override
		public void run() {
			CacheDB cacheDB = CacheDB.getInstance(application);
			adPromotionDataSource = cacheDB.selectAllAdPromotion();
			Message msg = mHandler.obtainMessage();
			msg.what = HANDLE_CACHE_AD_AND_SHOW;
			mHandler.sendMessage(msg);
		}
		
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		getAppCategories();
		onFinishLoading();
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		onFinishLoading();
	}
	
	private void onFinishLoading() {
		categoryListView.stopRefresh();
		categoryListView.stopLoadMore();
		categoryListView.setRefreshTime(Utils.getResourceString(application, R.string.appstore_list_header_hint_just_now));
	}

	@Override
	public void onClick(View view) {
		
		App app = null;
		//promotion resource not exist
		if (adPromotionDataSource == null || adPromotionDataSource.size() <= 0) {
			ToastUtil.showToast(application, R.string.promotions_resources_not_exist);
			return;
		}
		switch (view.getId()) {
		case R.id.adOneImageView:
			app = adPromotionDataSource.get(0);
			position = 0;
			break;
		case R.id.adTwoImageView:
			if (adPromotionDataSource == null || adPromotionDataSource.size() < 2) {
				ToastUtil.showToast(application, R.string.promotions_resources_not_exist);
				return;
			}
			app = adPromotionDataSource.get(1);
			position = 1;
			break;
		case R.id.adThreeImageView:
			if (adPromotionDataSource == null || adPromotionDataSource.size() < 3) {
				ToastUtil.showToast(application, R.string.promotions_resources_not_exist);
				return;
			}
			app = adPromotionDataSource.get(2);
			position = 2;
			break;
		case R.id.adFourImageView:
			if (adPromotionDataSource == null || adPromotionDataSource.size() < 4) {
				ToastUtil.showToast(application, R.string.promotions_resources_not_exist);
				return;
			}
			app = adPromotionDataSource.get(3);
			position = 3;
			break;
		default:
			break;
		}
		jumpToActivity(app);
	}
	
	private void jumpToActivity(App app) {
		if (app.getType() == Constants.TYPE_AD_TYPE_APP) {
			Intent mIntent = new Intent(getActivity(), SoftwareDetailActivity.class);
			Bundle mBundle = new Bundle();
			mBundle.putSerializable(Constants.SER_KEY, (Serializable) adPromotionDataSource);
			mBundle.putInt(Constants.APP_LIST_POSITION, position);
			mIntent.putExtras(mBundle);
			startActivity(mIntent);
		} else if (app.getType() == Constants.TYPE_AD_TYPE_WEBVIEW) {
			Intent mWebIntent = new Intent();
			mWebIntent.setClass(getActivity(), WebViewActivity.class);
			mWebIntent.putExtra("url", app.getUrl());
			startActivity(mWebIntent);
		}
	}
}
