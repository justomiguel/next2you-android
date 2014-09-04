package com.globant.next2you.fragments;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
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
import com.globant.next2you.App.State;
import com.globant.next2you.R;
import com.globant.next2you.async.UICallback;
import com.globant.next2you.net.ApiServices;
import com.globant.next2you.objects.Destination;
import com.globant.next2you.objects.Person;
import com.globant.next2you.objects.RetrievePendingTravelsResponse;
import com.globant.next2you.objects.Travel;
import com.globant.next2you.objects.TravelPersonResponse;
import com.globant.next2you.util.UIUtils;

public class AskRideScreen {
	@SuppressWarnings("unused")
	private static final String TAG = "AskRideScreen";
	private WeakReference<Context> ctx;
	private View offerDialogView;
	private LinearLayout holder;
	private boolean pendingTravelsActive = false;
	private ArrayList<Travel> offeringTravels = new ArrayList<Travel>();
	private ArrayList<Travel> pendingTravels = new ArrayList<Travel>();
	private static final double DELTA_LOC_MAX_DIFF = 0.00001;
	private static final double MIN_TIME_INTERVAL_DIFF = 1000 * 60 * 30;

	private SimpleDateFormat formatterParser;

	public AskRideScreen(Context ctx) {
		this.ctx = new WeakReference<Context>(ctx);
	}

	private Context getContext() {
		return ctx.get();
	}

	public void close() {
		holder.removeAllViews();
		holder.setBackgroundColor(Color.WHITE);
	}

	@SuppressLint("InflateParams")
	public void initialize(LinearLayout dialogHolder) {
		this.holder = dialogHolder;
		formatterParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
		formatterParser.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		final Context ctx = getContext();
		LayoutInflater inflater = LayoutInflater.from(ctx);
		dialogHolder.setBackgroundColor(Color.BLACK);
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
					pendingTravelsActive = true;
				} else {
					pending.setTextColor(inactiveColor);
					offers.setTextColor(activeColor);
					pendingTravelsActive = false;
				}
				loadPeople();
			}
		};
		pending.setOnClickListener(tabToggle);
		offers.setOnClickListener(tabToggle);

		loadPeople();
		// dialogHolder.addView(offerDialogView);
		dialogHolder.setVisibility(View.VISIBLE);
		dialogHolder.bringChildToFront(offerDialogView);

	}

	private void loadPeople() {
		if (App.app().userState == State.NONE) {
			Log.w(TAG, "user state is NONE skip tabs reloading");
			return;
		}
		if (pendingTravelsActive) {
			App.app().loadPendingTavels(new UICallback() {
				@Override
				public void onResult(Object result) {
					pendingTravels = (ArrayList<Travel>) result;
					ListView ridesList = (ListView) offerDialogView
							.findViewById(R.id.rides_list);
					ridesList.setAdapter(new RidesAdapter(pendingTravels));
				}
			});
		} else {
			App.app().loadOfferedTavels(new UICallback() {

				@Override
				public void onResult(Object result) {
					if (result == null) {
						return;
					}
					ListView ridesList = (ListView) offerDialogView
							.findViewById(R.id.rides_list);
					ArrayList<Travel> travels = (ArrayList<Travel>) result;
					offeringTravels = filterTravels(travels);
					ridesList.setAdapter(new RidesAdapter(offeringTravels));
				}
			});
		}
	}
	

	private ArrayList<Travel> filterTravels(
			ArrayList<Travel> listToFilter) {
		ArrayList<Travel> filteredList = new ArrayList<Travel>();
		
		for (Travel tp : listToFilter) {
			App app = App.app();
			
			// Remove pending travels from the offers array
			for(Travel pendingTravel : pendingTravels) {
				long otherPersonId = pendingTravel.getPersonIdToBeApproved();
				//Remove pending travels with other person identifier equal to the travel offer person identifier
				if(tp.getPersonId() == otherPersonId) {
					continue;
				}
			}
			
			// Remove the owner travels
			if (tp.isOwner()) {
				Log.d(TAG, "filter travel is OWNER " + tp);
				continue;
			}

			// Remove unrelated travels to the current users's state
			State state = app.userState;
			if (state.equals(State.OFFER)) {
				if (tp.isHasCar()) {
					//If the current user offers travel remove other users travel offers
					Log.d(TAG, "filter travel state=ASK but has car true " + tp);
					continue;
				}
			} else {
				if (!tp.isHasCar()) {
					//If the current user searches for travel remove other users searches
					Log.d(TAG, "filter travel state=OFFER but has car false "
							+ tp);
					continue;
				}
			}

			// Remove all travels with destination different from ours
			Destination curDest = app.currentDestination;
			if (tp.getFromLatitude() > 0
					&& tp.getFromLongitude() > 0
					&& tp.getToLatitude() > 0
					&& tp.getToLongitude() > 0
					&& Math.abs(tp.getToLatitude() - curDest.getLatitude()) > DELTA_LOC_MAX_DIFF
					&& Math.abs(tp.getToLongitude() - curDest.getLongitude()) > DELTA_LOC_MAX_DIFF) {
				Log.d(TAG, "filter travel because of location coordinate " + tp);
				continue;
			}
			
			// Remove all travels with time span different from ours
			if(tp.getStartTime() != null)
			{
				try {
					Date travelStartTime = formatterParser.parse(tp.getStartTime());
					long travelTimeStamp = travelStartTime.getTime();
					long curTravelTimeStamp = app.travelDateRequested.getTime();
					if(Math.abs(travelTimeStamp - curTravelTimeStamp) > MIN_TIME_INTERVAL_DIFF)
					{
						continue;
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
			}

			filteredList.add(tp);
		}
		return filteredList;
	}

	private class RidesAdapter extends BaseAdapter {
		private ArrayList<Travel> travels;
		private HashMap<Long, Person> personCache;
		private SimpleDateFormat formatter;

		public RidesAdapter(ArrayList<Travel> travels) {
			this.travels = travels;
			personCache = new HashMap<Long, Person>();
			formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
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
			// load data objects
			Travel travel = travels.get(position);
			
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
			Button leftBtn = (Button) holder
					.findViewById(R.id.ignore_request);
			leftBtn.setText(pendingTravelsActive ? R.string.reject : R.string.ask_ride_ignore);
			Button rightBtn = (Button) holder.findViewById(R.id.offer_ride);
			rightBtn.setText(pendingTravelsActive ? R.string.approve_travel : R.string.offer_ride);
			if (convertView == null) {
				UIUtils.prepareTextView(ctx, timeLabel);
				UIUtils.prepareTextView(ctx, userNameLabel);
				UIUtils.prepareTextView(ctx, userDescrLabel);
				UIUtils.prepareTextView(ctx, spanLabel);
				UIUtils.prepareTextView(ctx, leftBtn);
				UIUtils.prepareTextView(ctx, rightBtn);
			}
			
			
			timeLabel.setText(formatter.format(new Date()));
			if(travel.isHasCar()) {
				userDescrLabel.setText("is offering ride");
			}else {
				userDescrLabel.setText("is searching for a ride");
			}
			spanLabel.setText("");
			try {
				spanLabel.setText("Travel starts at " + formatter.format(formatterParser.parse(travel.getStartTime())) + "hs");
			} catch (ParseException e) {
				e.printStackTrace();
			}
	
			loadPersonForPendingTravelsTab(position, userNameLabel, avatar, leftBtn, rightBtn);
			
			return holder;
		}

		private void loadPersonForPendingTravelsTab(final int position,
				final TextView userNameLabel, final ImageView avatar, final Button leftBtn, final Button rightBtn) {
			App.getTaskManager().assignNet(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					Person person = null;
					Travel travel = (Travel) getItem(position);
					long personId = travel.getPersonIdToBeApproved();
					if(personId == 0)
					{
						personId = travel.getPersonId();
					}
					person = personCache.get(personId);
					if (person != null) {
						return person;
					}
					String token = App.app().getAuth().getToken();
					person = ApiServices.getPersonInfo(token,
							personId);
					personCache.put(personId, person);
					return person;
				}
			}, new UICallback() {
				@Override
				public void onResult(Object result) {
					if (position != ((Integer) avatar.getTag()).intValue()) {
						return;
					}
					Person p = (Person) result;
					final Travel travel = (Travel) getItem(position);
					if (p != null) {
						userNameLabel.setText(p.getNickname());
						String imageUri = ApiServices.API_URL + "images/"
								+ p.getImage();
						App.loadImage(imageUri, avatar);
					} else {
						Log.d(TAG, "invalid person object");
					}

					// manage buttons
					if(pendingTravelsActive) {
						setupButtonsForPendingTab(leftBtn, rightBtn, p, travel);
					} else {
						if(App.app().userState == State.OFFER) {
							leftBtn.setVisibility(View.VISIBLE);
							rightBtn.setVisibility(View.GONE);
						} else {
							leftBtn.setVisibility(View.VISIBLE);
							rightBtn.setVisibility(View.VISIBLE);
							rightBtn.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									if(!pendingTravelsActive) {
										App.getTaskManager().assignNet(new Callable<Object>() {
											@Override
											public Object call()
													throws Exception {
												String travelId = String.valueOf(travel.getTravelId());
												String currentToken = App.app().getAuth().getToken();
												boolean res = ApiServices.subscribeForTravel(currentToken , travelId);
												Log.d(TAG, "subscribe for travel result:" + res);
												return null;
											}
										});
									}
								}
							});
						}
						leftBtn.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								// ignore action
								Log.d(TAG, "add to ignore list");
								App.app().ignoreList.add(travel.getTravelId());
								// TODO reload tab
							}
						});
					}
				}

				private void setupButtonsForPendingTab(final Button leftBtn,
						final Button rightBtn, Person p, Travel travel) {
					int visibility;
					if(p.getPersonId() == travel.getPersonIdToBeApproved()) {
						visibility = View.VISIBLE;
						leftBtn.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								// reject action
								App.getTaskManager().assignNet(new Callable<Object>() {
									@Override
									public Object call() throws Exception {
										int travelPersonId = 0;
										String token = App.app().getAuth().getToken();
										ApiServices.approveOrRejectForTravel(token, travelPersonId, false);
										return null;
									}
								});
							}
						});
						
						rightBtn.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								// approve action
								App.getTaskManager().assignNet(new Callable<Object>() {
									@Override
									public Object call() throws Exception {
										int travelPersonId = 0;
										String token = App.app().getAuth().getToken();
										ApiServices.approveOrRejectForTravel(token, travelPersonId, true);
										return null;
									}
								});
							}
						});
					} else {
						visibility = View.GONE;
					}
					leftBtn.setVisibility(visibility);
					rightBtn.setVisibility(visibility);
				}
			});
		}

	}

}
