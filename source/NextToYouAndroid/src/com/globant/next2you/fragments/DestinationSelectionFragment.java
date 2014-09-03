package com.globant.next2you.fragments;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.globant.next2you.App;
import com.globant.next2you.App.State;
import com.globant.next2you.AppMainContentActivity;
import com.globant.next2you.GPSCollectorService;
import com.globant.next2you.R;
import com.globant.next2you.async.UICallback;
import com.globant.next2you.kankan.wheel.widget.OnWheelChangedListener;
import com.globant.next2you.kankan.wheel.widget.WheelView;
import com.globant.next2you.kankan.wheel.widget.adapters.WheelViewAdapter;
import com.globant.next2you.net.ApiServices;
import com.globant.next2you.objects.CreateTravelRequest;
import com.globant.next2you.objects.CreateTravelResponse;
import com.globant.next2you.objects.Destination;
import com.globant.next2you.objects.ListDestinationsResponse;
import com.globant.next2you.util.UIUtils;
import com.google.android.gms.maps.model.LatLngBounds;

@SuppressLint("InflateParams")
public class DestinationSelectionFragment extends BaseFragment {
	private DestinationsAdapter adapter;
	private boolean listExpanded = false;
	private ListView destinationsList;
	private View timerSetContainer;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		final View rootView = inflater.inflate(
				R.layout.destination_selection_screen, null);

		UIUtils.prepareTextView(getActivity(),
				(TextView) rootView.findViewById(R.id.screen_title));

		rootView.findViewById(R.id.go_home).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						doGoHome();
					}
				});

		destinationsList = (ListView) rootView
				.findViewById(R.id.destinations_list);

		timerSetContainer = rootView.findViewById(R.id.view_dialog);

		App.getTaskManager().assignNet(new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				String currentToken = App.app().getAuth().getToken();
				return ApiServices.listDestinations(currentToken);
			}
		}, new UICallback() {
			@Override
			public void onResult(Object result) {
				ListDestinationsResponse dest = (ListDestinationsResponse) result;
				ArrayList<Destination> destinations = dest.getDestinations();
				if(destinations.size() > 0) {
					App.app().currentDestination = destinations.get(0);
				}
				adapter = new DestinationsAdapter(destinations ,
						rootView, timerSetContainer);
				destinationsList
						.setOnItemClickListener(new OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> arg0,
									View arg1, int pos, long arg3) {
								if (pos == adapter.selectedPos) {
									adapter.selectedPos = -1;
								} else {
									adapter.selectedPos = pos;
								}
								if(adapter.selectedPos != -1) {
									adapter.putFirst(adapter.selectedPos);
								}
								adapter.notifyDataSetChanged();
							}
						});
				destinationsList.setAdapter(adapter);
				adapter.setupDialogView(0, timerSetContainer);
			}
		});

		return rootView;
	}

	private void doGoHome() {
		AppMainContentActivity activity = (AppMainContentActivity) getActivity();
		activity.initAndOpenMapScreen();
	}

	@Override
	protected String getLogTag() {
		return "DestinationSelectionFragment";
	}

	private class DestinationsAdapter extends BaseAdapter {

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
		private int selectedPos = 0;
		private View holder;
		private String[] hoursList;
		private View timerSetContainer;

		public DestinationsAdapter(ArrayList<Destination> destinations,
				View rootView, View timerSetContainer) {
			this.destinations = destinations;
			this.holder = rootView;
			this.timerSetContainer = timerSetContainer;
		}
		
		public void putFirst(int position) {
			Collections.swap(destinations, 0, position);
		}

		@Override
		public int getCount() {
			if (!listExpanded) {
				return 1;
			}
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
			// if (position == selectedPos) {
			// return getViewForSelectedPosition(position, convertView, parent);
			// } else {
			// }

			return getViewForNonSelectedItem(position, convertView, parent,
					timerSetContainer);
		}

		public View setupDialogView(int position, View view) {
			Log.d(TAG, "total positions:" + destinations.size() + ";cur pos="
					+ position);
			Context ctx = view.getContext();
			View rootView = view;
			rootView.setTag(Integer.valueOf(2));
			TextView lbl = (TextView) rootView.findViewById(R.id.lbl);
			UIUtils.prepareTextView(ctx, lbl);

			// ImageView img = (ImageView) rootView.findViewById(R.id.img);
			// img.setImageResource(R.drawable.arrow_down);
			// img.setVisibility(position == 0 ? View.VISIBLE : View.GONE);

			final WheelView wh1 = (WheelView) rootView
					.findViewById(R.id.wheel1);
			hoursList = new String[] { "1", "2", "3", "4", "5", "6", "7", "8",
					"9", "10", "11", "12" };
			initWheelView(wh1, hoursList);

			String[] mins = new String[60];
			for (int i = 0; i < mins.length; i++) {
				mins[i] = String.valueOf(i);
			}
			final WheelView wh2 = (WheelView) rootView
					.findViewById(R.id.wheel2);
			initWheelView(wh2, mins);

			final WheelView wh3 = (WheelView) rootView
					.findViewById(R.id.wheel3);
			initWheelView(wh3, new String[] { "Pm", "Am" });

			TextView dots = (TextView) rootView.findViewById(R.id.dots);
			UIUtils.prepareTextView(ctx, dots);

			Button offerBtn = (Button) rootView.findViewById(R.id.travel_offer);
			UIUtils.prepareTextView(ctx, offerBtn);
			offerBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final Context ctx = v.getContext();
					final String totalInterval = getTotalInterval(wh1, wh2, wh3);
					if (totalInterval == null) {
						return;
					}
					App.getTaskManager().assignNet(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							LatLngBounds bounds = App.app().mapBounds;
							CreateTravelRequest request = new CreateTravelRequest(
									bounds.northeast.latitude,
									bounds.northeast.longitude,
									bounds.southwest.latitude,
									bounds.southwest.longitude, true,
									totalInterval);
							ApiServices.createTravel(App.app().getAuth()
									.getToken(), request);
							return null;
						}
					});

					LayoutInflater inflater = LayoutInflater.from(ctx);
					final LinearLayout dialogHolder = (LinearLayout) holder
							.findViewById(R.id.dialog_holder);
					dialogHolder.removeAllViews();
					final View offerDialogView = inflater.inflate(
							R.layout.offer_ride_dialog, dialogHolder);

					// setup views
					TextView personsCountField = (TextView) offerDialogView
							.findViewById(R.id.notification_text_2);
					personsCountField.setText(String.format(
							Locale.US,
							ctx.getResources().getString(
									R.string.offer_ride_persons), 20));
					UIUtils.prepareTextView(ctx, personsCountField);

					TextView titleLine1 = (TextView) offerDialogView
							.findViewById(R.id.notification_text);
					TextView titleLine3 = (TextView) offerDialogView
							.findViewById(R.id.notification_text_3);
					UIUtils.prepareTextView(ctx, titleLine1);
					UIUtils.prepareTextView(ctx, titleLine3);

					// dialogHolder.addView(offerDialogView);
					dialogHolder.setVisibility(View.VISIBLE);

					TextView ribbonText = (TextView) offerDialogView
							.findViewById(R.id.ribbon_text);
					UIUtils.prepareTextView(ctx, ribbonText);
					String format = ctx
							.getString(R.string.offer_ride_travels_completed);
					String formatted = String.format(Locale.US, format, 15);
					ribbonText.setText(Html.fromHtml(formatted));

					TextView congratsTxt = (TextView) offerDialogView
							.findViewById(R.id.contgrats_improve);
					UIUtils.prepareTextView(ctx, congratsTxt);

					offerDialogView.findViewById(R.id.close_notification)
							.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									dialogHolder.removeView(offerDialogView);
									dialogHolder.setVisibility(View.GONE);
								}
							});

					offerDialogView.findViewById(R.id.share_fb_btn)
							.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									shareToFbWall(ctx);
								}
							});

					offerDialogView.findViewById(R.id.share_twitter_btn)
							.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									postTweet(ctx);
								}
							});
				}
			});

			Button askBtn = (Button) rootView.findViewById(R.id.travel_ask);
			UIUtils.prepareTextView(ctx, askBtn);
			askBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View v) {
					App.getTaskManager().assignNet(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							LatLngBounds bounds = App.app().mapBounds;
							String totalInterval = getTotalInterval(wh1, wh2,
									wh3);
							if (totalInterval == null) {
								return null;
							}
							double toLongitude = 0;
							double toLatitude = 0;
							Destination selectedDest = destinations
									.get(selectedPos);
							
							App.app().currentDestination = selectedDest;
							if (selectedDest != null) {
								toLatitude = selectedDest.getLatitude();
								toLongitude = selectedDest.getLongitude();
							}
							if (selectedDest.getName().toLowerCase().equals("casa")) {
								toLatitude = App.app().userLocation.latitude;
								toLongitude = App.app().userLocation.longitude;
							}
							
							CreateTravelRequest request = new CreateTravelRequest(
									bounds.northeast.latitude,
									bounds.northeast.longitude,
									toLongitude,
									toLatitude, true,
									totalInterval);
							CreateTravelResponse createTravelResponse = ApiServices
									.createTravel(App.app().getAuth()
											.getToken(), request);
							return createTravelResponse;
						}
					}, new UICallback() {
						@Override
						public void onResult(Object result) {
							CreateTravelResponse createTravelResponse = (CreateTravelResponse) result;
							App.app().userState = State.ASK;
							App.app().setCurrentTravelId(createTravelResponse.getTravelId());
							showPendingOffersAlert(v);
						}
					});
				}

				private void showPendingOffersAlert(View v) {
					final Context ctx = v.getContext();
					LayoutInflater inflater = LayoutInflater.from(ctx);
					final LinearLayout dialogHolder = (LinearLayout) holder
							.findViewById(R.id.dialog_holder);
					dialogHolder.removeAllViews();
					final View offerTravelDialog = inflater.inflate(
							R.layout.offer_travel_dialog, dialogHolder);
					dialogHolder.setVisibility(View.VISIBLE);

					// setup notification text
					TextView notificationText = (TextView) offerTravelDialog
							.findViewById(R.id.notification_text);
					UIUtils.prepareTextView(ctx, notificationText);
					String formatted = ctx.getResources().getString(
							R.string.offer_ride_notification_text);
					// TODO this should be equal to the number of items in the
					// corresponding tab
					formatted = String.format(Locale.US, formatted,
							Integer.valueOf(5));
					notificationText.setText(Html.fromHtml(formatted));

					// setup ribbon text
					TextView ribbonTxt = (TextView) offerTravelDialog
							.findViewById(R.id.ribbon_text);
					UIUtils.prepareTextView(ctx, ribbonTxt);
					ribbonTxt.setText(String.format(Locale.US,
							ctx.getString(R.string.travels_done),
							Integer.valueOf(5)));

					// setup close handle
					dialogHolder.findViewById(R.id.close_notification)
							.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									dialogHolder.removeView(dialogHolder);
									dialogHolder.setVisibility(View.GONE);

									// trigger refresh of clusters
									Intent intent = new Intent(
											GPSCollectorService.BROADCAST_INTENT_FILTER);
									intent.putExtra(GPSCollectorService.LAT,
											Float.NaN);
									intent.putExtra(GPSCollectorService.LON,
											Float.NaN);
									ctx.sendBroadcast(intent);

									doGoHome();
								}
							});
				}
			});

			return rootView;
		}

		private String getTotalInterval(final WheelView wh1,
				final WheelView wh2, final WheelView wh3) {
			long curTime = System.currentTimeMillis() / 1000;

			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR,
					Integer.parseInt(hoursList[wh1.getCurrentItem()]));
			c.set(Calendar.MINUTE, wh2.getCurrentItem());
			c.set(Calendar.AM_PM, wh3.getCurrentItem() == 0 ? Calendar.PM
					: Calendar.PM);
			Log.d(TAG,
					"set current " + wh1.getCurrentItem() + ":"
							+ wh2.getCurrentItem() + ":" + wh3.getCurrentItem());
			long selectedTime = c.getTimeInMillis() / 1000;

			int minsDiff = (int) ((selectedTime - curTime) / 60);
			int hrsDiff = minsDiff / 60;
			int mntsDiff = minsDiff % 60;

			final String totalInterval = hrsDiff + ":" + mntsDiff;
			Log.d(TAG, "totalInterval:" + totalInterval);

			if (minsDiff < 0) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(wh1.getContext(),
								R.string.invalid_timespan_selected,
								Toast.LENGTH_SHORT).show();
					}
				});
				return null;
			}
			
			App.app().travelDateRequested = new Date(c.getTimeInMillis());
			
			return totalInterval;
		}

		private void postTweet(Context ctx) {
			Intent tweetIntent = new Intent(Intent.ACTION_SEND);
			tweetIntent.putExtra(Intent.EXTRA_TEXT, "Test tweet");
			tweetIntent.setType("text/plain");
			PackageManager pm = ctx.getPackageManager();
			List<ResolveInfo> lract = pm.queryIntentActivities(tweetIntent,
					PackageManager.MATCH_DEFAULT_ONLY);

			boolean resolved = false;

			for (ResolveInfo ri : lract) {
				// Log.d(TAG, "name:" + ri.activityInfo.name);
				if (ri.activityInfo.name.toLowerCase(Locale.US).contains(
						"twitter")) {
					tweetIntent.setClassName(ri.activityInfo.packageName,
							ri.activityInfo.name);
					resolved = true;
					break;
				}
			}

			ctx.startActivity(resolved ? tweetIntent : Intent.createChooser(
					tweetIntent, "Choose one"));
		}

		private void shareToFbWall(Context ctx) {
			// http://stackoverflow.com/questions/7545254/android-and-facebook-share-intent
			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
					"http://www.test.com");
			PackageManager pm = ctx.getPackageManager();
			List<ResolveInfo> activityList = pm.queryIntentActivities(
					shareIntent, 0);
			for (final ResolveInfo app : activityList) {
				if ((app.activityInfo.name).contains("facebook")) {
					final ActivityInfo activity = app.activityInfo;
					final ComponentName name = new ComponentName(
							activity.applicationInfo.packageName, activity.name);
					shareIntent.addCategory(Intent.CATEGORY_LAUNCHER);
					shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					shareIntent.setComponent(name);
					ctx.startActivity(shareIntent);
					return;
				}
			}

			Toast.makeText(ctx, R.string.facebook_app_not_available,
					Toast.LENGTH_SHORT).show();
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
			int[] components = getDateComponents();
			for (int i = 0; i < components.length; i++) {
				Log.d(TAG, "components " + i + "=" + components[i]);
			}
			if (wh.getId() == R.id.wheel1) {
				wh.setCurrentItem(components[0] - 1);
			} else if (wh.getId() == R.id.wheel3) {
				wh.setCurrentItem(components[3] == 0 ? 0 : 1);
			} else if (wh.getId() == R.id.wheel2) {
				wh.setCurrentItem(components[1]);
			}
		}

		private int[] getDateComponents() {
			Calendar c = Calendar.getInstance();
			return new int[] {
					c.get(Calendar.HOUR) == 0 ? 12 : c.get(Calendar.HOUR),
					c.get(Calendar.MINUTE), c.get(Calendar.SECOND),
					c.get(Calendar.AM_PM) == Calendar.AM ? 1 : 0 };
		}

		private View getViewForNonSelectedItem(int position, View convertView,
				ViewGroup parent, final View timerSetContainer) {
			if (convertView != null) {
				int type = ((Integer) convertView.getTag()).intValue();
				if (type != 1) {
					convertView = null;
				}
			}
			View view = convertView;
			TextView title;
			final Context ctx = parent.getContext();
			if (view == null) {
				view = LayoutInflater.from(ctx).inflate(
						R.layout.destination_list_item, null);
			}
			view.setTag(Integer.valueOf(1));
			title = (TextView) view.findViewById(R.id.title);
			UIUtils.prepareTextView(ctx, title);
			title.setText(destinations.get(position).getName());

			final ImageView img = (ImageView) view.findViewById(R.id.img);
			img.setImageResource(listExpanded ? R.drawable.arrow_up : R.drawable.arrow_down);
			img.setTag(Integer.valueOf(position));
			img.setClickable(true);
			img.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
			img.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					listExpanded = !listExpanded;
					img.setImageResource(listExpanded ? R.drawable.arrow_up : R.drawable.arrow_down);
					BaseAdapter adapter = (BaseAdapter) destinationsList
							.getAdapter();
					adapter.notifyDataSetChanged();
					int bCol = ctx.getResources().getColor(listExpanded ? 
							R.color.shadow : R.color.login_background);
					destinationsList.setBackgroundColor(bCol);
					
					RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) destinationsList
							.getLayoutParams();
					params.height = listExpanded ? android.widget.RelativeLayout.LayoutParams.MATCH_PARENT
							: android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT;
					destinationsList.setLayoutParams(params);

				}
			});
			
			title.setTextColor(Color.WHITE);
			img.setVisibility(position == 0 ? View.VISIBLE : View.GONE);

			return view;
		}
	}
}
