package cn.koolcloud.ipos.appstore.fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.koolcloud.ipos.appstore.AppDetailActivity;
import cn.koolcloud.ipos.appstore.CategoryActivity;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.WebViewActivity;
import cn.koolcloud.ipos.appstore.adapter.CategoryListAdapter;
import cn.koolcloud.ipos.appstore.adapter.MainGalleryAdapter;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.cache.database.CacheDB;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.entity.Category;
import cn.koolcloud.ipos.appstore.entity.ResultSet;
import cn.koolcloud.ipos.appstore.fancycoverflow.FancyCoverFlow;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.MyLog;
import cn.koolcloud.ipos.appstore.utils.MySPEdit;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;

@SuppressWarnings("deprecation")
public class MainRightFragment extends BaseFragment implements OnItemClickListener, 
	OnItemSelectedListener {
	private static final int HANDLE_CACHE_CATEGORY_TO_SHOW = 0;
	private static final int HANDLE_CACHE_AD_AND_SHOW = 1;
	private static final int HANDLE_GALLERY_AUTO_CHANGE = 2;
	private GridView categoryGridView;								//the list view of local software
	private CategoryListAdapter mCategoryListAdapter;						//adapter for category list view
//	private Gallery gallery;
	private FancyCoverFlow fancyCoverFlow;
	private MainGalleryAdapter mainGalleryAdapter;
	private List<Category> categoryList = new ArrayList<Category>();	//category data source.
	private List<App> adsList = Collections.synchronizedList(new ArrayList<App>());	//promotion data source.
	private LinearLayout viewGroup;
	private TextView[] textViews;
	private float density;
	private int initSelectPosition;
//	private int index;
	private final int GALLERY_LOOP_TIME = 5000;
	private Timer timer;
	public static int galleryImageWidth;
	public static int galleryImageHeight;
	
	public static MainRightFragment getInstance() {
		MainRightFragment categoryFragment = new MainRightFragment();
		return categoryFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflate, ViewGroup container,
			Bundle savedInstanceState) {
		galleryImageWidth = (int) getResources().getDimension(R.dimen.ad_promotion_image_view_width);
		galleryImageHeight = (int) getResources().getDimension(R.dimen.ad_promotion_image_view_height);

		View v = inflate.inflate(R.layout.category_content_list, container, false);
		DisplayMetrics dm = new DisplayMetrics();  
		dm = getResources().getDisplayMetrics();
		density = dm.density;
		initSelectPosition = Integer.MAX_VALUE / 2;
		
		initViews(v);
		new GetCacheCategoryThread().start();
		
		return v;
	}
	
	@Override
	public void onDestroyView() {
		timer.cancel();
		super.onDestroyView();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		getAppCategories();
		new GetCacheAdPromotionThread().start();
	}

	private void initViews(View v) {
		categoryGridView = (GridView) v.findViewById(R.id.categoryGridView);
		categoryGridView.setOnItemClickListener(this);
		
//		gallery = (Gallery) v.findViewById(R.id.gallery);
//		mainGalleryAdapter = new MainGalleryAdapter(getActivity());
//		gallery.setAdapter(mainGalleryAdapter);
//		gallery.setSelection(initSelectPosition);
//		gallery.setOnItemClickListener(this);
		fancyCoverFlow = (FancyCoverFlow) v.findViewById(R.id.fancyCoverFlow);
		mainGalleryAdapter = new MainGalleryAdapter(getActivity());
		fancyCoverFlow.setAdapter(mainGalleryAdapter);
		fancyCoverFlow.setUnselectedAlpha(0.3f);
        fancyCoverFlow.setUnselectedSaturation(0.0f);
        fancyCoverFlow.setUnselectedScale(0.4f);
        fancyCoverFlow.setSpacing(-98);
        fancyCoverFlow.setMaxRotation(45);
        fancyCoverFlow.setScaleDownGravity(0.5f);
        fancyCoverFlow.setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);
		fancyCoverFlow.setSelection(initSelectPosition);
		fancyCoverFlow.setOnItemClickListener(this);
		
		viewGroup = (LinearLayout) v.findViewById(R.id.viewGroup);
		
		timer = new Timer();
        timer.schedule(task, GALLERY_LOOP_TIME, GALLERY_LOOP_TIME);
	}
	
	/**
	 * 定时器，实现自动播放
	 */
	private TimerTask task = new TimerTask() {
		@Override
		public void run() {
			Message message = new Message();
			message.what = 2;
//			index = gallery.getSelectedItemPosition();
//			index = fancyCoverFlow.getSelectedItemPosition();
//			index++;
			mHandler.sendMessage(message);
		}
	};

	Handler mHandler = new Handler(new Handler.Callback() {
		@SuppressLint("Recycle")
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_CACHE_CATEGORY_TO_SHOW:
				mCategoryListAdapter = new CategoryListAdapter(application, categoryList);
				categoryGridView.setAdapter(mCategoryListAdapter);
				mCategoryListAdapter.notifyDataSetChanged();
				break;

			case HANDLE_CACHE_AD_AND_SHOW:
//				initAdPromotion();
				mainGalleryAdapter.refreshGallery(adsList);
				initIndicator();
				//get ad promotion from server
				getAdPromotion();
				break;
			case HANDLE_GALLERY_AUTO_CHANGE:
//				gallery.setSelection(index);
//				fancyCoverFlow.setSelection(index);
				MotionEvent e1 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
						MotionEvent.ACTION_DOWN, 89.333336f, 265.33334f, 0);
				MotionEvent e2 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
						MotionEvent.ACTION_UP, 300.0f, 238.00003f, 0);

				try {
					WindowManager wm = getActivity().getWindowManager();
					fancyCoverFlow.onFling(e1, e2, -wm.getDefaultDisplay().getWidth()-260, 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			return false;
		}
	});
	
	private void getAppCategories() {
		ApiService.getAppCategories(getActivity(), MySPEdit.getTerminalID(getActivity()), 
				MySPEdit.getCategoryHash(getActivity()), getAppCategoriesCallBack);
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
				MyLog.d("-------getCategoryInfo=" + jsonObj.toString());
				
				categoryList = JsonUtils.parseJSONCategories(jsonObj);
				mCategoryListAdapter = new CategoryListAdapter(application, categoryList);
				categoryGridView.setAdapter(mCategoryListAdapter);
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
			MyLog.d("describe=" + msg);
			if (msg.contains(ResultSet.NET_ERROR.describe)) {
				if (isAdded()) {
					ToastUtil.showToast(application, R.string.nonetwork_prompt_server_error, Toast.LENGTH_LONG);
		        }
			}
		}
	};
	
	private void getAdPromotion() {
		ApiService.getPromotions(application, MySPEdit.getTerminalID(application),
				getAdPromotionCallBack);
	}
	
	private void initIndicator() {
		textViews = new TextView[adsList.size()];
		viewGroup.removeAllViews();
		for (int i = 0; i < adsList.size(); i++) {
			try {
				TextView textView = new TextView(getActivity());
				textView.setLayoutParams(new LayoutParams((int)(12 * density), (int)(12 * density)));
				textView.setPadding(0, 0, 2, 0);
				textViews[i] = textView;
				if (i == (initSelectPosition % adsList.size())) {
					textViews[i].setBackgroundResource(R.drawable.radio_sel);
				} else {
					textViews[i].setBackgroundResource(R.drawable.radio);
				}
				viewGroup.addView(textViews[i]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
//		gallery.setOnItemSelectedListener(this);
		fancyCoverFlow.setOnItemSelectedListener(this);
	}
	
	//get Ad Promotion call back
	private CallBack getAdPromotionCallBack = new CallBack() {
		@Override
		public void onCancelled() {
		}
		
		@Override
		public void onStart() {
		}
		
		@Override
		public void onSuccess(JSONObject jsonObj) {
			try {
				int id = 0;
				JSONObject dataJson = null;
				
				MyLog.e("main ads:\n" + jsonObj.toString());
				
				dataJson = JsonUtils.getJSONObject(jsonObj, Constants.REQUEST_DATA);
				id = JsonUtils.getIntValue(dataJson, Constants.JSON_KEY_ID);
				MySPEdit.savePromotinDataID(application, id);
				
				List<App> tmpPromotion = JsonUtils.parseJSONAdPromotionApps(jsonObj);
				if (tmpPromotion != null && tmpPromotion.size() > 0) {
					adsList = tmpPromotion;
					mainGalleryAdapter.refreshGallery(adsList);
					initIndicator();
				}
			} catch (Exception e) {
				e.printStackTrace();
				onFailure("getAdPromotion response error!");
			}
		}
		
		@Override
		public void onFailure(String msg) {
			dismissLoading();
			MyLog.d("describe=" + msg);
			if (msg.contains(ResultSet.NET_ERROR.describe)) {
				if(isAdded()){
					ToastUtil.showToast(application, R.string.nonetwork_prompt_server_error,
							Toast.LENGTH_LONG);
				}
			}
		}
	};

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		if(arg0.getId() == R.id.categoryGridView) {
			Intent mIntent = new Intent(getActivity(), CategoryActivity.class);
			Bundle mBundle = new Bundle();
			mBundle.putSerializable(Constants.SER_KEY, (Serializable) categoryList);
			mBundle.putInt(Constants.CATEGORY_LIST_POSITION, position);	//ListView header is on first position so to remove one
			mIntent.putExtras(mBundle);
			startActivity(mIntent);
//		} else if(arg0.getId() == R.id.gallery) {
		} else if(arg0.getId() == R.id.fancyCoverFlow) {
			if(adsList != null && adsList.size() > 0) {
				App app = adsList.get(position % adsList.size());
				jumpToActivity(app, position % adsList.size());
			}
		}
	}
	
	private void jumpToActivity(App app, int position) {
		if (app.getType() == Constants.TYPE_AD_TYPE_APP) {
			Intent mIntent = new Intent(getActivity(), AppDetailActivity.class);
			Bundle mBundle = new Bundle();
			mBundle.putSerializable(Constants.SER_KEY, (Serializable) adsList);
			mBundle.putInt(Constants.APP_LIST_POSITION, position);
			mIntent.putExtras(mBundle);
			getActivity().startActivity(mIntent);
		} else if (app.getType() == Constants.TYPE_AD_TYPE_WEBVIEW) {
			Intent mWebIntent = new Intent();
			mWebIntent.setClass(getActivity(), WebViewActivity.class);
			mWebIntent.putExtra("url", app.getUrl());
			getActivity().startActivity(mWebIntent);
		}
	}
	
	class InsertCategoryThread extends Thread {
		@Override
		public void run() {
			CacheDB cacheDB = CacheDB.getInstance(application);
			cacheDB.insertCategories(categoryList);
		}
	}
	
	class CacheAdPromotionThread extends Thread {
		@Override
		public void run() {
			CacheDB cacheDB = CacheDB.getInstance(application);
			cacheDB.insertAdPromotions(adsList);
			cacheDB.insertApps(adsList, null);
		}
	}
	
	class GetCacheCategoryThread extends Thread {
		@Override
		public void run() {
			CacheDB cacheDB = CacheDB.getInstance(application);
			categoryList = cacheDB.selectAllCategories();
			Message msg = mHandler.obtainMessage();
			msg.what = HANDLE_CACHE_CATEGORY_TO_SHOW;
			mHandler.sendMessage(msg);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		CacheDB.getInstance(application).closeDB();
	}

	class GetCacheAdPromotionThread extends Thread {
		@Override
		public void run() {
			CacheDB cacheDB = CacheDB.getInstance(application);
			adsList = cacheDB.selectAllAdPromotion();
			Message msg = mHandler.obtainMessage();
			msg.what = HANDLE_CACHE_AD_AND_SHOW;
			mHandler.sendMessage(msg);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View view, int position, long arg3) {
		if(adsList != null && adsList.size() > 0) {
			for (int i = 0; i < textViews.length; i++) {
				textViews[position % adsList.size()].setBackgroundResource(R.drawable.radio_sel);
				if (position % adsList.size() != i) {
					textViews[i].setBackgroundResource(R.drawable.radio);
				}
			}
		}
//		ImageView img = (ImageView) view;
//		img.startAnimation(AnimationUtils.loadAnimation(
//                getActivity(), R.anim.home_res_video_gallery_in));
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

}
