package com.globant.next2you.fragments;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.globant.next2you.App;
import com.globant.next2you.AppMainContentActivity;
import com.globant.next2you.R;
import com.globant.next2you.async.UICallback;
import com.globant.next2you.kankan.wheel.widget.OnWheelChangedListener;
import com.globant.next2you.kankan.wheel.widget.WheelView;
import com.globant.next2you.kankan.wheel.widget.adapters.WheelViewAdapter;
import com.globant.next2you.net.ApiServices;
import com.globant.next2you.objects.Destination;
import com.globant.next2you.objects.ListDestinationsResponse;
import com.globant.next2you.util.UIUtils;

public class DestinationSelectionFragment extends BaseFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.destination_selection_screen,
				null);

		UIUtils.prepareTextView(getActivity(),
				(TextView) rootView.findViewById(R.id.screen_title));

		rootView.findViewById(R.id.go_home).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						AppMainContentActivity activity = (AppMainContentActivity) getActivity();
						activity.initAndOpenMapScreen();
					}
				});

		final ListView destinationsList = (ListView) rootView
				.findViewById(R.id.destinations_list);
		
		App.getTaskManager().assignNet(new Callable<Object>() {
			
			@Override
			public Object call() throws Exception {
				String currentToken = App.app().getAuth().getToken();
				return ApiServices.listDestinations(currentToken );
			}
		}, new UICallback() {
			
			@Override
			public void onResult(Object result) {
				ListDestinationsResponse dest = (ListDestinationsResponse) result;
				final DestinationsAdapter adapter = new DestinationsAdapter(dest.getDestinations());
				destinationsList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
							long arg3) {
						if (pos == adapter.selectedPos) {
							adapter.selectedPos = -1;
						} else {
							adapter.selectedPos = pos;
						}
						adapter.notifyDataSetChanged();
					}
				});
				destinationsList.setAdapter(adapter);
			}
		});

		return rootView;
	}

	@Override
	protected String getLogTag() {
		return "DestinationSelectionFragment";
	}

	private static class DestinationsAdapter extends BaseAdapter {
		private final class DestinationWheelAdapter implements WheelViewAdapter {
			private final WheelView wh;
			private String[] items;

			private DestinationWheelAdapter(WheelView wh, String[] items) {
				this.wh = wh;
				this.items = items;
			}

			@Override
			public void unregisterDataSetObserver(DataSetObserver observer) {
			}

			@Override
			public void registerDataSetObserver(DataSetObserver observer) {
			}

			@Override
			public int getItemsCount() {
				return items.length;
			}

			@Override
			public View getItem(int index, View convertView, ViewGroup parent) {
				Context context = parent.getContext();
				TextView tv = getWheelLabel(index, convertView, context);
				return tv;
			}

			private TextView getWheelLabel(int index, View convertView,
					Context context) {
				TextView tv = (TextView) convertView;
				if (tv == null) {
					tv = new TextView(context);
				}
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				tv.setLayoutParams(params);
				UIUtils.prepareTextView(context, tv);
				boolean selected = wh.getCurrentItem() == index;
				int col = selected ? R.color.purple : R.color.wheel_view_text;
				tv.setTextColor(context.getResources().getColor(col));
				tv.setTextSize(context.getResources().getDimension(
						R.dimen.text_size_info));
				tv.setText(String.valueOf(items[index]));
				tv.setGravity(Gravity.CENTER);
				if (selected) {
					tv.setBackgroundResource(R.drawable.wheel_selected_row);
				} else {
					tv.setBackgroundColor(Color.TRANSPARENT);
				}
				return tv;
			}

			@Override
			public View getEmptyItem(View convertView, ViewGroup parent) {
				return null;
			}
		}

		@SuppressWarnings("unused")
		private static final String TAG = "DestinationsAdapter";
		private ArrayList<Destination> destinations;
		private int selectedPos = -1;

		public DestinationsAdapter(ArrayList<Destination> destinations) {
			this.destinations = destinations;
		}

		@Override
		public int getCount() {
			return destinations.size();
		}

		@Override
		public Object getItem(int position) {
			return destinations.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (position == selectedPos) {
				return getViewForSelectedPosition(position, convertView, parent);
			} else {
				return getViewForNonSelectedItem(position, convertView, parent);
			}
		}

		private View getViewForSelectedPosition(int position, View convertView,
				ViewGroup parent) {
			Context ctx = parent.getContext();
			if (convertView != null) {
				int type = ((Integer) convertView.getTag()).intValue();
				if (type != 2) {
					convertView = null;
				}
			}
			View rootView = convertView;
			if (rootView == null) {
				rootView = LayoutInflater.from(ctx).inflate(
						R.layout.destination_list_item_selected, null);
			}
			rootView.setTag(Integer.valueOf(2));
			TextView lbl = (TextView) rootView.findViewById(R.id.lbl);
			UIUtils.prepareTextView(ctx, lbl);
			TextView title = (TextView) rootView.findViewById(R.id.title);
			title.setText(destinations.get(position).getName());
			UIUtils.prepareTextView(ctx, title);

			ImageView img = (ImageView) rootView.findViewById(R.id.img);
			img.setImageResource(R.drawable.arrow_down);
			img.setVisibility(position == 0 ? View.VISIBLE : View.GONE);

			WheelView wh1 = (WheelView) rootView.findViewById(R.id.wheel1);
			initWheelView(wh1, new String[] { "1", "2", "3", "4", "5", "6",
					"7", "8", "9", "10", "11", "12" });

			String[] mins = new String[60];
			for (int i = 0; i < mins.length; i++) {
				mins[i] = String.valueOf(i);
			}
			WheelView wh2 = (WheelView) rootView.findViewById(R.id.wheel2);
			initWheelView(wh2, mins);

			WheelView wh3 = (WheelView) rootView.findViewById(R.id.wheel3);
			initWheelView(wh3, new String[] { "Pm", "Am" });

			TextView dots = (TextView) rootView.findViewById(R.id.dots);
			UIUtils.prepareTextView(ctx, dots);
			
			Button offerBtn = (Button) rootView.findViewById(R.id.travel_offer);
			UIUtils.prepareTextView(ctx, offerBtn);

			Button askBtn = (Button) rootView.findViewById(R.id.travel_ask);
			UIUtils.prepareTextView(ctx, askBtn);

			return rootView;
		}

		private void initWheelView(final WheelView wh, String[] items) {
			OnWheelChangedListener changedListener = new OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue,
						int newValue) {
					wheel.invalidateWheel(false);
				}
			};
			wh.addChangingListener(changedListener);
			wh.setViewAdapter(new DestinationWheelAdapter(wh, items));
		}

		private View getViewForNonSelectedItem(int position, View convertView,
				ViewGroup parent) {
			if (convertView != null) {
				int type = ((Integer) convertView.getTag()).intValue();
				if (type != 1) {
					convertView = null;
				}
			}
			View view = convertView;
			TextView title;
			Context ctx = parent.getContext();
			if (view == null) {
				view = LayoutInflater.from(ctx).inflate(
						R.layout.destination_list_item, null);
			}
			view.setTag(Integer.valueOf(1));
			title = (TextView) view.findViewById(R.id.title);
			UIUtils.prepareTextView(ctx, title);
			title.setText(destinations.get(position).getName());

			ImageView img = (ImageView) view.findViewById(R.id.img);
			img.setImageResource(R.drawable.arrow_up);
			img.setTag(Integer.valueOf(position));

			if (position != destinations.size() - 1) {
				title.setTextColor(Color.WHITE);
			} else {
				title.setTextColor(ctx.getResources().getColor(R.color.purple));
			}
			img.setVisibility(position == 0 ? View.VISIBLE : View.GONE);

			return view;
		}

	}
}
