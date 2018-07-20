package ben.dateselect;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RVFilterAdapter extends RecyclerView.Adapter<RVFilterAdapter.ViewHolder> {

	private LayoutInflater mInflater;
	private List<String> mData;
	private List<Boolean> isClicks;

	public RVFilterAdapter(Context context, List<String> data) {
		mInflater = LayoutInflater.from(context);
		mData = data;


		isClicks = new ArrayList<>();
		for (int i = 0; i < mData.size(); i++) {
			if (i == 0) {
				isClicks.add(true);
			}
			isClicks.add(false);
		}
	}


	public void setData(List<String> data) {
		mData = data;
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		public ViewHolder(View arg0) {
			super(arg0);
		}

		RelativeLayout mRlContent;
		TextView mTvName;
	}

	@Override
	public int getItemCount() {
		return mData.size();
	}

	/**
	 * 创建ViewHolder
	 */
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
		View view = mInflater.inflate(R.layout.view_filter_item, viewGroup, false);
		ViewHolder viewHolder = new ViewHolder(view);
		viewHolder.mRlContent = view.findViewById(R.id.rl_content);
		viewHolder.mTvName = view.findViewById(R.id.tv_name);
		return viewHolder;
	}

	/**
	 * 设置值
	 */
	@Override
	public void onBindViewHolder(final ViewHolder viewHolder, int position) {
		String name = mData.get(position);
		viewHolder.mTvName.setText(name);
		viewHolder.mTvName.setSelected(isClicks.get(position));
		viewHolder.mRlContent.setSelected(isClicks.get(position));
		viewHolder.mRlContent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setOnclick(viewHolder.getAdapterPosition());
			}
		});
	}


	public void setOnclick(int position) {
		for (int i = 0; i < isClicks.size(); i++) {
			isClicks.set(i, false);
		}
		isClicks.set(position, true);
		notifyDataSetChanged();
	}

	public String getSelectedItem() {
		for (int i = 0; i < isClicks.size(); i++) {
			if (isClicks.get(i)) return mData.get(i);
		}
		return "";
	}
}
