package cn.koolcloud.ipos.appstore.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.AppStorePreference;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.adapter.CommentsListAdapter;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.entity.Comment;
import cn.koolcloud.ipos.appstore.entity.ResultSet;
import cn.koolcloud.ipos.appstore.fragment.base.BaseFragment;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.Logger;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;
import cn.koolcloud.ipos.appstore.utils.Utils;
import cn.koolcloud.ipos.appstore.views.AppStoreListView;
import cn.koolcloud.ipos.appstore.views.AppStoreListView.AppstoreListViewListener;

public class SoftwareDetailCommentFragment extends BaseFragment implements View.OnClickListener, AppstoreListViewListener {
	
	private LayoutInflater inflater;			//view inflater
	private TextView closeCommnentTextView;	
	private AppStoreListView commentsListView;
	private OnFragmentActionListener mCallback;
	
	private App app = null;
	private List<Comment> dataList = new ArrayList<Comment>();
	private CommentsListAdapter adapter = null;
	
	// Container Activity must implement this interface
    public interface OnFragmentActionListener {
        public void closeCommentFragment();
    }

	public static SoftwareDetailCommentFragment getInstance() {
		SoftwareDetailCommentFragment commentFragment = new SoftwareDetailCommentFragment();
		return commentFragment;
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnFragmentActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentActionListener");
        }
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		app = (App) getArguments().getSerializable(Constants.SER_KEY);
	}

	@Override
	public View onCreateView(LayoutInflater inflate, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.inflater = inflate;
		return inflater.inflate(R.layout.software_more_comment_list, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		initViews();
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		getComments();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.close_comment:
			mCallback.closeCommentFragment();
			break;

		default:
			break;
		}
	}
	
	private void initViews() {
		closeCommnentTextView = (TextView) getActivity().findViewById(R.id.close_comment);
		closeCommnentTextView.setOnClickListener(this);
		commentsListView = (AppStoreListView) getActivity().findViewById(R.id.sw_detail_more_comment_list);
		
		commentsListView.setPullLoadEnable(false);
		commentsListView.setPullRefreshEnable(true);
		commentsListView.setAppStoreListViewListener(this);
	}
	
	private void getComments() {
		ApiService.getComments(application, AppStorePreference.getTerminalID(application), 
				app.getId(), getCommentsCallBack);
	}
	
	//get categories call back
	private CallBack getCommentsCallBack = new CallBack() {
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
				
				Logger.d("-------get Comment=" + jsonObj.toString());
				
				retCode = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_STATUS);
				data = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_DATA);
				
				/*if (!Constants.REQUEST_STATUS_OK.equals(retCode)) {
					onFailure(data);
				} else {
					if (TextUtils.isEmpty(data)) {
						onFailure(Utils.getResourceString(getApplicationContext(), R.string.nonetwork_prompt_server_error));
					} else {
						
					}
				}*/
				dataList = JsonUtils.parseJSONComments(jsonObj);
				if (dataList != null && dataList.size() > 0) {
					
					adapter = new CommentsListAdapter(application, dataList);
					commentsListView.setAdapter(adapter);
					adapter.notifyDataSetChanged();
				}
				
				dismissLoading();
			} catch (Exception e) {
				e.printStackTrace();
				onFailure("get comment response error!");
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

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		getComments();
		onFinishLoading();
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		onFinishLoading();
	}
	
	private void onFinishLoading() {
		commentsListView.stopRefresh();
		commentsListView.stopLoadMore();
		commentsListView.setRefreshTime(Utils.getResourceString(application, R.string.appstore_list_header_hint_just_now));
	}
}
