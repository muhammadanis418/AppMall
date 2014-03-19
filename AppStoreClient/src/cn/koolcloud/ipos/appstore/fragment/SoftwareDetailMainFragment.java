package cn.koolcloud.ipos.appstore.fragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.AppStorePreference;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.adapter.AdImageAdapter;
import cn.koolcloud.ipos.appstore.adapter.CommentsListAdapter;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.dialogs.SoftwareCommentDialog;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.entity.Comment;
import cn.koolcloud.ipos.appstore.entity.ResultSet;
import cn.koolcloud.ipos.appstore.fragment.base.BaseFragment;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.Logger;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;
import cn.koolcloud.ipos.appstore.utils.Utils;
import cn.koolcloud.ipos.appstore.views.GuideGallery;

public class SoftwareDetailMainFragment extends BaseFragment implements View.OnClickListener {
	private static final String TAG = "SoftwareDetailMainFragment";
	public static final int SOFTWARE_DETAIL_MAIN_COMMENT_REQUEST = 5;
	
	private final int Gallery_PLAY_TIME = 3000;								//screen picture display time
	private List<App> appListDataSource = null;								//apps data source
	private int currentPosition = 0;
	private App app = null;
	
	private ScrollView rootScrollView;
	private TextView softwareDespTextView;
	private TextView softwareNewFeatureDespTextView;
	private TextView moreCommentTextView;
	private TextView addCommentTextView;
//	private LinearLayout snapImageContainer;
	private GuideGallery imagesGallery;
	private LinearLayout pointLinearLayout;
	
	private FragmentManager fragmentManager;
	private OnSoftwareDetailAttachedListener mCallback;
	private LayoutInflater mInflater;
	
	//comment components
	private LinearLayout commentListLayout;
	private ListView commentListView;
	private TextView commentRatingCountTextView;
	private TextView commentMarksTextView;
	private RatingBar ratingComment;
	private ProgressBar progressOneStar;
	private ProgressBar progressTwoStar;
	private ProgressBar progressThreeStar;
	private ProgressBar progressFourStar;
	private ProgressBar progressFiveStar;
	private TextView oneStarTextView;
	private TextView twoStarTextView;
	private TextView threeStarTextView;
	private TextView fourStarTextView;
	private TextView fiveStarTextView;
	
	private Timer autoGalleryTimer = new Timer();
	public ImageTimerTask timeTaks = null;
	private int gallery_positon = 0;
	public static boolean timeFlag = true;
	private int size = 3;
	private List<String> snapShortList = new ArrayList<String>();
	
	//comments summary
	private List<Comment> commentList = new ArrayList<Comment>();
	private CommentsListAdapter adapter = null;
	
	// Container Activity must implement this interface
    public interface OnSoftwareDetailAttachedListener {
        public void onDetailActivityCreated();
    }
    
    @Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnSoftwareDetailAttachedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSoftwareDetailAttachedListener");
        }
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		fragmentManager = getFragmentManager();
		mInflater = (LayoutInflater) application.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		initViews();
		mCallback.onDetailActivityCreated();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.soft_detail_main, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
	}
	
	private void initViews() {
		rootScrollView = (ScrollView) getActivity().findViewById(R.id.rootScrollView);
		softwareDespTextView = (TextView) getActivity().findViewById(R.id.sw_main_introduce);
		softwareNewFeatureDespTextView = (TextView) getActivity().findViewById(R.id.sw_main_new_feature);
		moreCommentTextView = (TextView) getActivity().findViewById(R.id.more_comment);
		addCommentTextView = (TextView) getActivity().findViewById(R.id.sw_main_comment_add_bt);
		addCommentTextView.setOnClickListener(this);
		moreCommentTextView.setVisibility(View.VISIBLE);
		moreCommentTextView.setOnClickListener(this);
//		snapImageContainer = (LinearLayout) getActivity().findViewById(R.id.sw_main_gallery_container);
		imagesGallery = (GuideGallery) getActivity().findViewById(R.id.image_wall_gallery);
		
		pointLinearLayout = (LinearLayout) getActivity().findViewById(R.id.gallery_point_linear);
		
		//comment components
		commentListLayout = (LinearLayout) getActivity().findViewById(R.id.sw_detail_comment_list_layout);
		commentListView = (ListView) getActivity().findViewById(R.id.contentListView);
		commentRatingCountTextView = (TextView) getActivity().findViewById(R.id.sw_main_comment_rating_count);
		commentMarksTextView = (TextView) getActivity().findViewById(R.id.sw_main_comment_marks);
		ratingComment = (RatingBar) getActivity().findViewById(R.id.sw_main_comment_rating);
		progressOneStar = (ProgressBar) getActivity().findViewById(R.id.sw_detail_processbar_one_star);
		progressTwoStar = (ProgressBar) getActivity().findViewById(R.id.sw_detail_processbar_two_star);
		progressThreeStar = (ProgressBar) getActivity().findViewById(R.id.sw_detail_processbar_three_star);
		progressFourStar = (ProgressBar) getActivity().findViewById(R.id.sw_detail_processbar_four_star);
		progressFiveStar = (ProgressBar) getActivity().findViewById(R.id.sw_detail_processbar_five_star);
		oneStarTextView = (TextView) getActivity().findViewById(R.id.sw_detail_processbar_one_count);
		twoStarTextView = (TextView) getActivity().findViewById(R.id.sw_detail_processbar_two_count);
		threeStarTextView = (TextView) getActivity().findViewById(R.id.sw_detail_processbar_three_count);
		fourStarTextView = (TextView) getActivity().findViewById(R.id.sw_detail_processbar_four_count);
		fiveStarTextView = (TextView) getActivity().findViewById(R.id.sw_detail_processbar_five_count);
	}
	
	public void setArguments(Bundle bundle) {
		appListDataSource = (List<App>) bundle.getSerializable(Constants.SER_KEY);
		currentPosition = bundle.getInt(Constants.APP_LIST_POSITION);
		app = appListDataSource.get(currentPosition);
		Logger.d("set app succucessfull");
		
		getAppDetails();
		getCommentSummary();
	}
	
	public void getAppDetails() {
		ApiService.getAppDetailsByIds(application, AppStorePreference.getTerminalID(application), 
				app.getId(), getAppDetailsCallBack);
	}
	
	private CallBack getAppDetailsCallBack = new CallBack() {
		
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
				
				Logger.d("-------getAppsInfo=" + jsonObj.toString());
				
				retCode = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_STATUS);
				data = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_DATA);
				
				/*if (!Constants.REQUEST_STATUS_OK.equals(retCode)) {
					onFailure(data);
				} else {
					if (TextUtils.isEmpty(data)) {
						onFailure(Utils.getResourceString(getActivity(), R.string.nonetwork_prompt_server_error));
					} else {
						client = JsonUtils.parseJSONClient(jsonObj);
					}
				}*/
				JSONObject appsObj = JsonUtils.getJSONObject(jsonObj, Constants.REQUEST_DATA);
				JSONArray appArray = JsonUtils.getJSONArray(appsObj, Constants.JSON_KEY_APPS);
				
				if (appArray != null && appArray.length() > 0) {
					
					for (int i = 0; i < appArray.length(); i++) {
						JSONObject appObj = appArray.getJSONObject(i);
						String appDesp = JsonUtils.getStringValue(appObj, Constants.JSON_KEY_DESCRIPTION);
						softwareDespTextView.setText(appDesp);
						String newFeatureDesp = JsonUtils.getStringValue(appObj, Constants.JSON_KEY_NEW_FEATURE_DES);
						softwareNewFeatureDespTextView.setText(newFeatureDesp);
						
						String snapShort1 = JsonUtils.getStringValue(appObj, Constants.JSON_KEY_SCREENSHOT1);
						String snapShort2 = JsonUtils.getStringValue(appObj, Constants.JSON_KEY_SCREENSHOT2);
						String snapShort3 = JsonUtils.getStringValue(appObj, Constants.JSON_KEY_SCREENSHOT3);
						snapShortList.add(app.getSnapShortImageName(snapShort1));
						snapShortList.add(app.getSnapShortImageName(snapShort2));
						snapShortList.add(app.getSnapShortImageName(snapShort3));
					}
					size = snapShortList.size();
					initPointLayout();
				}
				
				//fix snap list null start 03-17
				if (null != snapShortList && snapShortList.size() > 0) {
					
					AdImageAdapter imageAdapter = new AdImageAdapter(snapShortList, application);
					imagesGallery.setAdapter(imageAdapter);
					setGallerySelectListener();
					timeTaks = new ImageTimerTask();
					autoGalleryTimer.scheduleAtFixedRate(timeTaks, Gallery_PLAY_TIME, Gallery_PLAY_TIME);
				}
				//fix snap list null end 03-17
				dismissLoading();
			} catch (Exception e) {
				onFailure("get apps by category response error!");
			}
		}

		@Override
		public void onFailure(String msg) {
			dismissLoading();
			if (msg.contains(ResultSet.NET_ERROR.describe)) {
				
				ToastUtil.showToast(application, R.string.nonetwork_prompt_server_error);
			}
		}
	};
	
	public void getCommentSummary() {
		ApiService.getCommentSummary(application, AppStorePreference.getTerminalID(application), 
				app.getId(), getCommentSummaryCallBack);
	}
	
	private CallBack getCommentSummaryCallBack = new CallBack() {
		
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
				
				Logger.d("-------getCommentSummary=" + jsonObj.toString());
				
				retCode = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_STATUS);
				data = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_DATA);
				
				/*if (!Constants.REQUEST_STATUS_OK.equals(retCode)) {
					onFailure(data);
				} else {
					if (TextUtils.isEmpty(data)) {
						onFailure(Utils.getResourceString(getActivity(), R.string.nonetwork_prompt_server_error));
					} else {
						client = JsonUtils.parseJSONClient(jsonObj);
					}
				}*/
				JSONObject dataObj = JsonUtils.getJSONObject(jsonObj, Constants.REQUEST_DATA);
				
				initCommentDetailComponents(dataObj);
				
				commentList = JsonUtils.parseJSONComments(jsonObj);
				//comment not null initialize comments listview
				if (commentList != null && commentList.size() > 0) {
					commentListLayout.setVisibility(View.VISIBLE);
					moreCommentTextView.setVisibility(View.VISIBLE);
					adapter = new CommentsListAdapter(application, commentList);
					commentListView.setAdapter(adapter);
					adapter.notifyDataSetChanged();
					/*if (commentList.size() >= 10) {
						moreCommentTextView.setVisibility(View.VISIBLE);
					} else {
						moreCommentTextView.setVisibility(View.GONE);
					}*/
				} else {
					commentListLayout.setVisibility(View.INVISIBLE);
					moreCommentTextView.setVisibility(View.GONE);
				}
				//keep ScrollView at the top
				rootScrollView.smoothScrollTo(0, 20);
				dismissLoading();
			} catch (Exception e) {
				onFailure("getCommentSummary response error!");
			}
		}
		
		@Override
		public void onFailure(String msg) {
			dismissLoading();
			if (msg.contains(ResultSet.NET_ERROR.describe)) {
				
				ToastUtil.showToast(application, R.string.nonetwork_prompt_server_error);
			}
		}
	};
	
	private void initCommentDetailComponents(JSONObject dataObj) {
		try {
			JSONObject scoresObj = JsonUtils.getJSONObject(dataObj,
					Constants.JSON_KEY_SCORES);
			JSONObject detailsObj = JsonUtils.getJSONObject(scoresObj,
					Constants.JSON_KEY_DETAILS);
			//TODO:get score details and then initialize the views
			String totalUserCommentsNum = JsonUtils.getStringValue(scoresObj, Constants.JSON_KEY_TOTAL);
			String average = JsonUtils.getStringValue(scoresObj, Constants.JSON_KEY_AVERAGE);
			int oneStarPercent = JsonUtils.getIntValue(detailsObj, Constants.JSON_KEY_ONE);
			int twoStarPercent = JsonUtils.getIntValue(detailsObj, Constants.JSON_KEY_TWO);
			int threeStarPercent = JsonUtils.getIntValue(detailsObj, Constants.JSON_KEY_THREE);
			int fourStarPercent = JsonUtils.getIntValue(detailsObj, Constants.JSON_KEY_FOUR);
			int fiveStarPercent = JsonUtils.getIntValue(detailsObj, Constants.JSON_KEY_FIVE);
			int[] percentArray = { oneStarPercent, twoStarPercent,
					threeStarPercent, fourStarPercent, fiveStarPercent };
			percentArray = Utils.bubbleSort(percentArray);
			
			commentRatingCountTextView.setText(totalUserCommentsNum + Utils.getResourceString(application, R.string.people_comment));
			commentMarksTextView.setText(average + "");
			
			//fix average null error start 03-17
			if (null != average) {
				ratingComment.setRating(Float.parseFloat(average));
			} else {
				ratingComment.setRating(0f);
			}
			//fix average null error end 03-17
			
			oneStarTextView.setText(oneStarPercent + "%");
			twoStarTextView.setText(twoStarPercent + "%");
			threeStarTextView.setText(threeStarPercent + "%");
			fourStarTextView.setText(fourStarPercent + "%");
			fiveStarTextView.setText(fiveStarPercent + "%");
			
			if (percentArray[0] != 0) {
				BigDecimal divider = new BigDecimal(percentArray[0]);
				BigDecimal hundred = new BigDecimal(100);
				if (oneStarPercent == percentArray[0]) {
					progressOneStar.setProgress(100);
				} else {
					int result = (int) Math.round(new BigDecimal(oneStarPercent).divide(divider, 2, BigDecimal.ROUND_HALF_DOWN).multiply(hundred).doubleValue());
					progressOneStar.setProgress(result);
				}
				
				if (twoStarPercent == percentArray[0]) {
					progressTwoStar.setProgress(100);
				} else {
					int result = (int) Math.round(new BigDecimal(twoStarPercent).divide(divider, 2, BigDecimal.ROUND_HALF_DOWN).multiply(hundred).doubleValue());
					progressTwoStar.setProgress(result);
				}
				
				if (threeStarPercent == percentArray[0]) {
					progressThreeStar.setProgress(100);
				} else {
					int result = (int) Math.round(new BigDecimal(threeStarPercent).divide(divider, 2, BigDecimal.ROUND_HALF_DOWN).multiply(hundred).doubleValue());
					progressThreeStar.setProgress(result);
				}
				
				if (fourStarPercent == percentArray[0]) {
					progressFourStar.setProgress(100);
				} else {
					int result = (int) Math.round(new BigDecimal(fourStarPercent).divide(divider, 2, BigDecimal.ROUND_HALF_DOWN).multiply(hundred).doubleValue());
					progressFourStar.setProgress(result);
				}
				
				if (fiveStarPercent == percentArray[0]) {
					progressFiveStar.setProgress(100);
				} else {
					int result = (int) Math.round(new BigDecimal(fiveStarPercent).divide(divider, 2, BigDecimal.ROUND_HALF_DOWN).multiply(hundred).doubleValue());
					progressFiveStar.setProgress(result);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setGallerySelectListener() {
		imagesGallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent,
					View view, int position, long id) {
				changePointView(position % size);
				gallery_positon = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}
	
	final Handler autoGalleryHandler = new Handler() {
		public void handleMessage(Message message) {
			super.handleMessage(message);
			switch (message.what) {
			case 1:
				imageViewOutAniamtion(imagesGallery.getSelectedView());
				imagesGallery.setSelection(gallery_positon + 1, true);
				imageViewInAniamtion(imagesGallery.getSelectedView());
				break;
			}
		}
	};
	
	public void imageViewOutAniamtion(View view) {
		view.startAnimation(AnimationUtils.loadAnimation(application,
				R.anim.right_left_out));
	}
	
	public void imageViewInAniamtion(View view) {
		view.startAnimation(AnimationUtils.loadAnimation(application,
				R.anim.right_left_in));
	}
	
	public void changePointView(int cur) {
		for (int i = 0; i < size; i++) {
			View imageView = pointLinearLayout.getChildAt(i);
			if (i == cur) {
				imageView.setBackgroundResource(R.drawable.feature_point_cur);
			} else {
				imageView.setBackgroundResource(R.drawable.feature_point);
			}
		}
	}
	
	private void initPointLayout() {
		for (int i = 0, size = snapShortList.size(); i < size; i++) {
			ImageView pointView = new ImageView(application);
			if (i == 0) {
				pointView.setBackgroundResource(R.drawable.feature_point_cur);
			} else {
				pointView.setBackgroundResource(R.drawable.feature_point);
			}
			pointLinearLayout.addView(pointView);
		}
	}
	
	class ImageTimerTask extends TimerTask {
		public void run() {
			if (timeFlag) {
				autoGalleryHandler.sendEmptyMessage(1);
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		autoGalleryTimer.cancel();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.sw_main_comment_add_bt:
			Intent intent = new Intent(getActivity(), SoftwareCommentDialog.class);
			Bundle args = new Bundle();
			args.putSerializable(Constants.SER_KEY, app);
			intent.putExtras(args);
			getActivity().startActivityForResult(intent, SOFTWARE_DETAIL_MAIN_COMMENT_REQUEST);
			break;
		case R.id.more_comment:
			SoftwareDetailCommentFragment commentFragment = SoftwareDetailCommentFragment.getInstance();
			FragmentTransaction fragTransaction = fragmentManager.beginTransaction();
			Bundle bundle = new Bundle();
			bundle.putSerializable(Constants.SER_KEY, app);
			commentFragment.setArguments(bundle);
			fragTransaction.replace(R.id.software_detail_main, (Fragment) commentFragment);
			fragTransaction.commit();
			
			break;
		default:
			break;
		}
		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SOFTWARE_DETAIL_MAIN_COMMENT_REQUEST) {
			getCommentSummary();
		}
	}

}
