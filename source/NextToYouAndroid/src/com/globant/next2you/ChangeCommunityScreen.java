package com.globant.next2you;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.globant.next2you.async.UICallback;
import com.globant.next2you.net.ApiServices;
import com.globant.next2you.objects.Community;
import com.globant.next2you.util.UIUtils;

public class ChangeCommunityScreen extends FragmentActivity {
	private static final String TAG = "ChangeCommunityScreen";
	private ArrayList<Community> communities = new ArrayList<Community>();

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.change_community);

		findViewById(R.id.go_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.slide_out_animation,
						R.anim.slide_in_animation);
			}
		});

		final ListView communitiesListView = (ListView) findViewById(R.id.community_list);
		communitiesListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				App.getTaskManager().assignNet(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						Community c = communities.get(position);
						App.app().currentCommunity = c.getName();
						String res = ApiServices.selectCommunity(App.app()
								.getAuth().getToken(), c.getCommunityId());
						Log.d(TAG, "select community:" + res);
						return res;
					}
				}, new UICallback() {
					@Override
					public void onResult(Object result) {
						sendBroadcast(new Intent(
								AppMainContentActivity.UPDATE_COMMUNITY));
						finish();
						overridePendingTransition(R.anim.slide_out_animation,
								R.anim.slide_in_animation);
					}
				});
			}
		});
		communitiesListView.setAdapter(new BaseAdapter() {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = convertView;
				if (view == null) {
					view = LayoutInflater.from(ChangeCommunityScreen.this)
							.inflate(R.layout.dest_list_item, null);
				}
				TextView tv = (TextView) view.findViewById(R.id.txt);
				tv.setText(communities.get(position).getName());
				UIUtils.prepareTextView(ChangeCommunityScreen.this, tv);

				View closeBtn = view.findViewById(R.id.close_btn);
				closeBtn.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
				closeBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});

				return view;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public Object getItem(int position) {
				return communities.get(position);
			}

			@Override
			public int getCount() {
				return communities.size();
			}
		});

		// App.getTaskManager().assignNet(new Callable<Object>() {
		// @Override
		// public Object call() throws Exception {
		// GetCummunitiesResponse communitiesResp =
		// ApiServices.getCommunities();
		// return communitiesResp.getCommunities();
		// }
		// }, new UICallback() {
		// @Override
		// public void onResult(Object result) {
		// communities = (ArrayList<Community>) result;
		// BaseAdapter adapter = (BaseAdapter) communitiesListView.getAdapter();
		// adapter.notifyDataSetChanged();
		// }
		// });
		communities = App.app().getAuth().getCommunities();
		Log.d(TAG, "communities for user:" + communities.size());
		BaseAdapter adapter = (BaseAdapter) communitiesListView.getAdapter();
		adapter.notifyDataSetChanged();
	}

}
