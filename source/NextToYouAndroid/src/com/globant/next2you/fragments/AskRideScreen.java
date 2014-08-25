package com.globant.next2you.fragments;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globant.next2you.App;
import com.globant.next2you.R;
import com.globant.next2you.async.UICallback;
import com.globant.next2you.net.ApiServices;
import com.globant.next2you.objects.Person;
import com.globant.next2you.objects.RetrievePendingTravelsResponse;
import com.globant.next2you.objects.Travel;
import com.globant.next2you.util.UIUtils;

public class AskRideScreen {
	@SuppressWarnings("unused")
	private static final String TAG = "AskRideScreen";
	private WeakReference<Context> ctx;
	private View offerDialogView;
	private View holder;

	public AskRideScreen(Context ctx) {
		this.ctx = new WeakReference<Context>(ctx);
	}

	private Context getContext() {
		return ctx.get();
	}

	public void close() {
		final LinearLayout dialogHolder = (LinearLayout) holder
				.findViewById(R.id.dialog_holder);
		dialogHolder.removeView(offerDialogView);
		dialogHolder.setVisibility(View.GONE);
	}

	@SuppressLint("InflateParams")
	public void initialize(View holder) {
		this.holder = holder;
		final Context ctx = getContext();
		LayoutInflater inflater = LayoutInflater.from(ctx);
		final LinearLayout dialogHolder = (LinearLayout) holder
				.findViewById(R.id.dialog_holder);
		dialogHolder.removeAllViews();
		offerDialogView = inflater.inflate(R.layout.ask_ride_screen,
				dialogHolder);

		// setup views
		final TextView pending = (TextView) offerDialogView
				.findViewById(R.id.pending_travels);
		final TextView offers = (TextView) offerDialogView
				.findViewById(R.id.offered_travels);
		UIUtils.prepareTextView(ctx, pending);
		UIUtils.prepareTextView(ctx, offers);

		// setup tab toggles
		final View position1 = offerDialogView.findViewById(R.id.pos1);
		final View position2 = offerDialogView.findViewById(R.id.pos2);
		OnClickListener tabToggle = new OnClickListener() {

			@Override
			public void onClick(View v) {
				View inactiveView = (v == pending) ? position2 : position1;
				inactiveView.setBackgroundColor(Color.TRANSPARENT);
				View activeView = (v == pending) ? position1 : position2;

				Resources res = ctx.getResources();
				int activeColor = res.getColor(R.color.offer_ride_green);
				int inactiveColor = res.getColor(R.color.ask_ride_grey);

				activeView.setBackgroundColor(activeColor);

				if (v == pending) {
					pending.setTextColor(activeColor);
					offers.setTextColor(inactiveColor);
				} else {
					pending.setTextColor(inactiveColor);
					offers.setTextColor(activeColor);
				}
			}
		};
		pending.setOnClickListener(tabToggle);
		offers.setOnClickListener(tabToggle);

		loadPeople();
		// dialogHolder.addView(offerDialogView);
		dialogHolder.setVisibility(View.VISIBLE);

	}

	private void loadPeople() {
		App.getTaskManager().assignNet(new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				String currentToken = App.app().getAuth().getToken();
				RetrievePendingTravelsResponse pendingTravels = ApiServices.retrievePendingTravels(currentToken);
				ArrayList<Travel> travels = pendingTravels != null ? pendingTravels
						.getTravels() : null;
				if (travels != null) {
					Log.d(TAG, "pendingTravels:" + travels.size());
				}
				if (travels == null || travels.size() == 0) {
					Log.d(TAG, "simulate response");
					InputStream reader = null;
					try {
						reader = getContext().getResources().openRawResource(
								R.raw.travels_mock);
						byte[] buff = new byte[1024];
						int n;
						StringBuilder sb = new StringBuilder();
						while ((n = reader.read(buff)) != -1) {
							sb.append(new String(buff, 0, n));
						}
						return new ObjectMapper().readValue(sb.toString(),
								RetrievePendingTravelsResponse.class)
								.getTravels();
					} catch (Exception e) {
						Log.e(TAG, "", e);
					} finally {
						if (reader != null) {
							reader.close();
						}
					}
				}
				return pendingTravels.getTravels();
			}
		}, new UICallback() {
			@Override
			public void onResult(Object result) {
				ArrayList<Travel> travels = (ArrayList<Travel>) result;
				ListView ridesList = (ListView) offerDialogView
						.findViewById(R.id.rides_list);
				ridesList.setAdapter(new RidesAdapter(travels));
			}
		});
	}

	private static class RidesAdapter extends BaseAdapter {
		private ArrayList<Travel> travels;
		private HashMap<Long, Person> personCache;
		
		public RidesAdapter(ArrayList<Travel> travels) {
			this.travels = travels;
			personCache = new HashMap<Long, Person>();
		}

		@Override
		public int getCount() {
			return travels.size();
		}

		@Override
		public Object getItem(int position) {
			return travels.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View holder = convertView;
			Context ctx = parent.getContext();
			if (holder == null) {
				holder = LayoutInflater.from(ctx).inflate(
						R.layout.ride_list_item, null);
			}
			TextView timeLabel = (TextView) holder.findViewById(R.id.time);
			final TextView userNameLabel = (TextView) holder
					.findViewById(R.id.user_name);
			userNameLabel.setText("");
			TextView userDescrLabel = (TextView) holder
					.findViewById(R.id.user_descr);
			TextView spanLabel = (TextView) holder.findViewById(R.id.span_time);
			final ImageView avatar = (ImageView) holder
					.findViewById(R.id.avatar);
			avatar.setImageBitmap(null);
			avatar.setTag(position);
			Button ignoreRequest = (Button) holder
					.findViewById(R.id.ignore_request);
			Button doOfferRide = (Button) holder.findViewById(R.id.offer_ride);

			if (convertView == null) {
				UIUtils.prepareTextView(ctx, timeLabel);
				UIUtils.prepareTextView(ctx, userNameLabel);
				UIUtils.prepareTextView(ctx, userDescrLabel);
				UIUtils.prepareTextView(ctx, spanLabel);
				UIUtils.prepareTextView(ctx, ignoreRequest);
				UIUtils.prepareTextView(ctx, doOfferRide);
			}
			timeLabel.setText("11:25 am");
			userDescrLabel.setText("wants to travel with you");
			spanLabel.setText("from 16 hs.");

			App.getTaskManager().assignNet(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					Person person = null;
					Travel travel = (Travel) getItem(position);
					person = personCache.get(travel.getPersonIdToBeApproved());
					if(person != null) {
						return person;
					}
					String token = App.app().getAuth().getToken();
					person = ApiServices.getPersonInfo(token,
							travel.getPersonIdToBeApproved());
					personCache.put(travel.getPersonIdToBeApproved(), person);
					return person;
				}
			}, new UICallback() {
				@Override
				public void onResult(Object result) {
					if (position != ((Integer) avatar.getTag()).intValue()) {
						return;
					}
					Person p = (Person) result;
					if (p != null) {
						userNameLabel.setText(p.getNickname());
						String imageUri = ApiServices.API_URL + "images/"
								+ p.getImage();
						App.loadImage(imageUri, avatar);
					} else {
						Log.d(TAG, "invalid person object");
					}
				}
			});

			return holder;
		}

	}

}
