package cn.koolcloud.ipos.appstore.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.entity.Comment;
import cn.koolcloud.ipos.appstore.utils.ConvertUtils;

/**
 * <p>Title: CommentsListAdapter.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-11-5
 * @version 	
 */
public class CommentsListAdapter extends BaseAdapter {
	private List<Comment> dataList = new ArrayList<Comment>();
	private LayoutInflater mInflater;

	public CommentsListAdapter(Context context, List<Comment> dataSource) {
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
		CommentViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.soft_detail_main_comment_list_item, null);
			holder = new CommentViewHolder();
			holder.userHeaderImageView = (ImageView) convertView.findViewById(
					R.id.user_header);	
			holder.ratingBar = (RatingBar) convertView.findViewById(
					R.id.soft_detail_comment_item_rating);							
			holder.userNickNameTextView = (TextView) convertView.findViewById(
					R.id.soft_detail_comment_item_nickname);
			holder.commentDateTextView = (TextView) convertView.findViewById(
					R.id.soft_detail_comment_item_date);
			holder.commentTextView = (TextView) convertView.findViewById(
					R.id.soft_detail_comment_item_text);
			convertView.setTag(holder);
		} else {
			holder = (CommentViewHolder) convertView.getTag();
		}

		final Comment commentInfo = dataList.get(position);
		holder.userNickNameTextView.setText(commentInfo.getUser());
		holder.commentDateTextView.setText(ConvertUtils.longToString(commentInfo.getDate(), "yyyy-MM-dd"));
		holder.commentTextView.setText(commentInfo.getComment());
		holder.ratingBar.setRating(Float.parseFloat(commentInfo.getRating()));

		return convertView;
	}
	
	class CommentViewHolder {
		ImageView userHeaderImageView;
		TextView userNickNameTextView;
		TextView commentDateTextView;
		TextView commentTextView;
		RatingBar ratingBar;
	}
}
