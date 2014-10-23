package com.globant.next2you.fragments;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.globant.next2you.App;
import com.globant.next2you.App.State;
import com.globant.next2you.GPSCollectorService;
import com.globant.next2you.R;
import com.globant.next2you.async.UICallback;
import com.globant.next2you.net.ApiServices;
import com.globant.next2you.objects.CreateUserTokenResponse;
import com.globant.next2you.objects.Person;
import com.globant.next2you.util.UIUtils;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class FragmentMap extends BaseFragment implements
		ClusterManager.OnClusterClickListener<Person>,
		ClusterManager.OnClusterInfoWindowClickListener<Person>,
		ClusterManager.OnClusterItemClickListener<Person>,
		ClusterManager.OnClusterItemInfoWindowClickListener<Person> {
	private GoogleMap map;
	private SupportMapFragment mapFragment;
	private BroadcastReceiver receiver;
	private Circle circle;
	
	private DestinationSelectionFragment destinationSelectFragment;
	public DestinationSelectionFragment getDestinationSelectFragment() {
		return destinationSelectFragment;
	}

	public boolean isDestinationSelectOnTop = false;

	public FragmentMap() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_map, container,
				false);

		// workaround to have non null map when creating SupportMapFragment in
		// code instead of in xml
		mapFragment = new SupportMapFragment() {
			public View originalContentView;
			public TouchableWrapper touchView;

			@Override
			public void onActivityCreated(Bundle savedInstanceState) {
				super.onActivityCreated(savedInstanceState);
				map = mapFragment.getMap();
				if (map != null) {
					try {
						MapsInitializer.initialize(getActivity());
					} catch (Exception e) {
						log(e);
					}
					addLocationHandler();
				}
			}

			@Override
			public View onCreateView(LayoutInflater inflater, ViewGroup parent,
					Bundle savedInstanceState) {
				originalContentView = super.onCreateView(inflater, parent,
						savedInstanceState);
				touchView = new TouchableWrapper(getActivity());
				touchView.addView(originalContentView);
				return touchView;
			}

			@Override
			public View getView() {
				return originalContentView;
			}
		};

		Button goToBtn = (Button) rootView.findViewById(R.id.map_main_btn);
		goToBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				log("'i go to' btn pressed");
				FragmentTransaction tr = ((FragmentActivity) getActivity())
						.getSupportFragmentManager().beginTransaction();
				destinationSelectFragment = new DestinationSelectionFragment();
				tr.replace(android.R.id.content, destinationSelectFragment, destinationSelectFragment.getClass()
						.getSimpleName());
				tr.commit();
				isDestinationSelectOnTop = true;
			}
		});

		FragmentTransaction transaction = getChildFragmentManager()
				.beginTransaction();
		transaction.add(R.id.fragment_map_root, mapFragment).commit();
		UIUtils.prepareTextView(getActivity(),
				(Button) rootView.findViewById(R.id.map_main_btn));

		return rootView;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			getActivity().unregisterReceiver(receiver);
		} catch (Exception e) {
			log(e);
		}
	}

	private void addLocationHandler() {
		try {
			receiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					double lat = intent.getDoubleExtra(GPSCollectorService.LAT,
							0);
					double lon = intent.getDoubleExtra(GPSCollectorService.LON,
							0);
					log(String.format(Locale.US, "onReceive [%f %f]", lat, lon));
					loadPeople(lat, lon);
				}
			};
			IntentFilter filter = new IntentFilter(
					GPSCollectorService.BROADCAST_INTENT_FILTER);
			getActivity().registerReceiver(receiver, filter);
		} catch (Exception e) {
			log(e);
		}
		GPSCollectorService.call(getActivity());
	}

	public void drawCircle(LatLng center, int radius) {
		Resources res = getActivity().getResources();
		int color = Color.WHITE;
		State state = App.app().userState;
		if(state.equals(State.NONE)) {
			color = res.getColor(R.color.marker_radius_purple);
		} else if(state.equals(State.ASK)) {
			color = res.getColor(R.color.marker_radius_green);
		} else if(state.equals(State.OFFER)) {
			color = res.getColor(R.color.marker_radius_red);
		}
		CircleOptions circleOptions = new CircleOptions()
				.center(new LatLng(center.latitude, center.longitude))
				.radius(radius).strokeColor(Color.TRANSPARENT)
				.fillColor(color);
		removeCircle();
		circle = map.addCircle(circleOptions);
	}

	public void removeCircle() {
		if (circle != null) {
			circle.remove();
		}
	}

	@Override
	protected String getLogTag() {
		return "FragmentMap";
	}

	// *************** Clusters ******************
	private ClusterManager<Person> clusterManager;

	// private Random mRandom = new Random(1984);

	/**
	 * Draws profile photos inside markers (using IconGenerator). When there are
	 * multiple people in the cluster, draw multiple photos (using
	 * MultiDrawable).
	 */
	private class PersonRenderer extends DefaultClusterRenderer<Person> {

		public PersonRenderer() {
			super(getActivity().getApplicationContext(), map, clusterManager);
		}

		@Override
		protected void onBeforeClusterItemRendered(Person person,
				MarkerOptions markerOptions) {
			if (!isAdded()) {
				return;
			}
			if (person instanceof Me) {
				int myPosIconId = R.drawable.marker_my_position;
				State state = App.app().userState;
				if(state.equals(State.ASK)) {
					myPosIconId = R.drawable.marker_my_position_green;
				} else if(state.equals(State.OFFER)) {
					myPosIconId = R.drawable.marker_my_position_red;
				}
				Bitmap icon = BitmapFactory.decodeResource(getResources(),
						myPosIconId);
				markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
						.title(person.getAddress());

				Resources res = getActivity().getResources();
				int radius = (int) res
						.getDimension(R.dimen.map_my_location_radius);
				LatLng pos = new LatLng(person.getPosition().latitude,
						person.getPosition().longitude);
				drawCircle(pos, radius);

			} else {
				int icId = 0;
				if (person.getHasCar() && person.getHasActiveTravel()) {
					icId = R.drawable.car;
				} else if (person.getHasCar() && !person.getHasActiveTravel()) {
					icId = R.drawable.black_car;
				} else if (!person.getHasCar() && !person.getHasActiveTravel()) {
					icId = R.drawable.black_smiley;
				} else if (!person.getHasCar() && person.getHasActiveTravel()) {
					icId = R.drawable.green_smiley;
				}
				if (icId != 0) {
					Bitmap icon = BitmapFactory.decodeResource(getResources(),
							icId);
					markerOptions
							.icon(BitmapDescriptorFactory.fromBitmap(icon))
							.title(person.getAddress());
				}
			}
		}

		private Bitmap getCompositeBitmap(Collection<Person> list) {
			int type1 = 0, type2 = 0, type3 = 0, type4 = 0, type5 = 0;
			for (Person person : list) {
				if (person instanceof Me) {
					type5++;
				} else if (person.getHasCar() && person.getHasActiveTravel()) {
					type1++;
				} else if (person.getHasCar() && !person.getHasActiveTravel()) {
					type2++;
				} else if (!person.getHasCar() && !person.getHasActiveTravel()) {
					type3++;
				} else if (!person.getHasCar() && person.getHasActiveTravel()) {
					type4++;
				}
			}

			int myPosIconId = R.drawable.marker_my_position;
			int circleColor = Color.WHITE;
			State state = App.app().userState;
			if(state.equals(State.ASK)) {
				myPosIconId = R.drawable.marker_my_position_green;
			} else if(state.equals(State.OFFER)) {
				myPosIconId = R.drawable.marker_my_position_red;
			}
			if(state.equals(State.NONE)) {
				circleColor = R.color.purple;
			} else if(state.equals(State.ASK)) {
				circleColor = R.color.green_marker;
			} else if(state.equals(State.OFFER)) {
				circleColor = R.color.red_marker;
			}
			Bitmap me = type5 > 0 ? createMarker(myPosIconId,
					type5, circleColor) : null;
			Bitmap type1Bitmap = type1 > 0 ? createMarker(R.drawable.car,
					type1, R.color.red_marker) : null;
			Bitmap type2Bitmap = type2 > 0 ? createMarker(R.drawable.black_car,
					type2, R.color.black_marker) : null;
			Bitmap type3Bitmap = type3 > 0 ? createMarker(
					R.drawable.green_smiley, type3, R.color.green_marker)
					: null;
			Bitmap type4Bitmap = type4 > 0 ? createMarker(
					R.drawable.black_smiley, type4, R.color.black_marker)
					: null;

			Bitmap composed = compose(me, type1Bitmap, type2Bitmap,
					type3Bitmap, type4Bitmap);
			return composed;
		}

		private Bitmap compose(Bitmap... arr) {
			int totalWidth = 0;
			int height = 0;
			for (int i = 0; i < arr.length; i++) {
				Bitmap b = arr[i];
				if (b != null) {
					totalWidth += b.getWidth();
					height = Math.max(height, arr[i].getHeight());
				}
			}
			Bitmap bitmap = Bitmap.createBitmap(totalWidth, height,
					Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(bitmap);

			int totalOffset = 0;
			Paint p = new Paint();
			for (int i = 0; i < arr.length; i++) {
				Bitmap b = arr[i];
				if (b != null) {
					Matrix matrix = new Matrix();
					matrix.postTranslate(totalOffset, 0);

					c.drawBitmap(b, matrix, p);
					totalOffset += b.getWidth();
				}
			}

			return bitmap;
		}

		private Bitmap createMarker(int resId, int numberOfItems, int colorRes) {
			Resources resources = getResources();
			int labelHeight = (int) resources
					.getDimension(R.dimen.map_cluster_marker_height);
			Bitmap icon = BitmapFactory.decodeResource(resources, resId);
			int totalHeight = icon.getHeight() + labelHeight;
			Bitmap bitmap = Bitmap.createBitmap(icon.getWidth(), totalHeight,
					Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(bitmap);

			Matrix matrix = new Matrix();
			matrix.postTranslate(0, totalHeight - icon.getHeight());
			Paint paint = new Paint();
			c.drawBitmap(icon, matrix, paint);

			paint.setStyle(Paint.Style.FILL);
			paint.setColor(resources.getColor(colorRes));
			int radius = (int) resources.getDimension(R.dimen.circle_radius);
			float cx = icon.getWidth() / 2;
			float cy = radius;
			c.drawCircle(cx, cy, radius, paint);

			Typeface tf = UIUtils.getAppTypeFace(getActivity());
			paint.setTypeface(tf);
			paint.setColor(Color.WHITE);
			float textSize = resources.getDimension(R.dimen.text_size_info);
			paint.setTextSize(textSize);
			String txt = String.valueOf(numberOfItems);

			Rect bounds = new Rect();
			paint.getTextBounds(txt, 0, txt.length(), bounds);
			c.drawText(txt, cx - bounds.width() / 2, cy + bounds.height() / 2,
					paint);

			return bitmap;
		}

		@Override
		protected void onBeforeClusterRendered(Cluster<Person> cluster,
				MarkerOptions markerOptions) {
			if (!isAdded()) {
				return;
			}
			Bitmap compositeMarker = getCompositeBitmap(cluster.getItems());
			markerOptions.icon(BitmapDescriptorFactory
					.fromBitmap(compositeMarker));
		}

		@Override
		protected boolean shouldRenderAsCluster(Cluster<Person> cluster) {
			return cluster.getSize() > 1;
		}
	}

	@Override
	public boolean onClusterClick(Cluster<Person> cluster) {
		// Show a toast with some info when the cluster is clicked.
		return true;
	}

	@Override
	public void onClusterInfoWindowClick(Cluster<Person> cluster) {
		// Does nothing, but you could go to a list of the users.
	}

	@Override
	public boolean onClusterItemClick(Person item) {
		// Does nothing, but you could go into the user's profile page, for
		// example.
		return false;
	}

	@Override
	public void onClusterItemInfoWindowClick(Person item) {
		// Does nothing, but you could go into the user's profile page, for
		// example.
	}

	protected void addMarkers(double lat, double lon, List<Person> people2) {
		if (clusterManager == null && map != null) {
			clusterManager = new ClusterManager<Person>(getActivity(), map);
			clusterManager.setRenderer(new PersonRenderer());
			map.setOnCameraChangeListener(clusterManager);
			map.setOnMarkerClickListener(clusterManager);
			map.setOnInfoWindowClickListener(clusterManager);
			clusterManager.setOnClusterClickListener(this);
			clusterManager.setOnClusterInfoWindowClickListener(this);
			clusterManager.setOnClusterItemClickListener(this);
			clusterManager.setOnClusterItemInfoWindowClickListener(this);
			/*
			 * To avoid UnsupportedOperationException:
			 * NonHierarchicalDistanceBasedAlgorithm.remove not implemented.
			 * exception
			 * http://stackoverflow.com/questions/22048872/issue-on-removing
			 * -item-from-clustermanager
			 */
			clusterManager
					.setAlgorithm(new PreCachingAlgorithmDecorator<Person>(
							new GridBasedAlgorithm<Person>()));
		}

		if(lat != Float.NaN && lon != Float.NaN) {
			addItems(lat, lon, people2);
		}
		clusterManager.cluster();
	}

	private List<Person> people = new LinkedList<Person>();

	private void addItems(double lat, double lon, List<Person> newPeople) {
		for (Person p : people) {
			clusterManager.removeItem(p);
		}
		people.clear();

		for (Person p : newPeople) {
			clusterManager.addItem(p);
			log(String.format(Locale.US, "add person [%f %f]", p.getLatitude(),
					p.getLongitude()));
		}
		people.addAll(newPeople);

		// Person p = new Person();
		// p.setHasCar(true);
		// p.setLatLng(position());
		// people.add(p);
		// clusterManager.addItem(p);
		//
		// p = new Person();
		// p.setLatLng(position());
		// p.setHasCar(true);
		// people.add(p);
		// clusterManager.addItem(p);
		//
		// p = new Person();
		// p.setLatLng(position());
		// p.setHasCar(true);
		// p.setHasActiveTravel(true);
		// people.add(p);
		// clusterManager.addItem(p);
		//
		// p = new Person();
		// p.setLatLng(position());
		// p.setHasCar(false);
		// p.setHasActiveTravel(true);
		// people.add(p);
		// clusterManager.addItem(p);
		//
		// p = new Person();
		// p.setLatLng(position());
		// p.setHasCar(false);
		// p.setHasActiveTravel(true);
		// people.add(p);
		// clusterManager.addItem(p);
		//
		// p = new Person();
		// p.setLatLng(position());
		// p.setHasCar(false);
		// p.setHasActiveTravel(false);
		// people.add(p);
		// clusterManager.addItem(p);
		//
		// p = new Person();
		// p.setLatLng(position());
		// p.setHasCar(false);
		// p.setHasActiveTravel(false);
		// people.add(p);
		// clusterManager.addItem(p);
		//
		// p = new Person();
		// p.setLatLng(position());
		// p.setHasCar(false);
		// people.add(p);
		// p.setHasActiveTravel(true);
		// clusterManager.addItem(p);
		//
		// p = new Person();
		// p.setLatLng(position());
		// p.setHasCar(false);
		// p.setHasActiveTravel(true);
		// people.add(p);
		// clusterManager.addItem(p);
		// p = new Me(pos);
		// people.add(p);
		// clusterManager.addItem(p);
	}

	// private LatLng position() {
	// return new LatLng(random(51.6723432, 51.38494009999999), random(
	// 0.148271, -0.3514683));
	// }

	// private double random(double min, double max) {
	// return mRandom.nextDouble() * (max - min) + min;
	// }

	private static class Me extends Person {
		private LatLng pos;

		public Me(LatLng pos) {
			if (pos == null) {
				throw new IllegalArgumentException(
						"user's position can't be null");
			}
			this.pos = pos;
		}

		@Override
		public LatLng getPosition() {
			return pos;
		}

	}

	private boolean cameraMovedInitially = false;

	private void loadPeople(final double lat, final double lon) {
		if((lat == Float.NaN || lon == Float.NaN) && clusterManager != null) {
			clusterManager.cluster();
			return;
		}
		if (!isAdded()) {
			return;
		}
		if (!cameraMovedInitially) {
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,
					lon), 9.5f));
			cameraMovedInitially = true;
		}
		VisibleRegion region = map.getProjection().getVisibleRegion();
		final LatLngBounds bounds = region.latLngBounds;
		App.app().mapBounds = bounds;
		App.getTaskManager().assignNet(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				if (!isAdded()) {
					return null;
				}
				CreateUserTokenResponse auth = App.app().getAuth();
				String token = auth != null ? auth.getToken() : "";
				return ApiServices.getPeople(token, bounds.northeast.latitude,
						bounds.northeast.longitude, bounds.southwest.latitude,
						bounds.southwest.longitude, true);
			}
		}, new UICallback() {

			@Override
			public void onResult(Object result) {
				if (result == null || !isAdded()) {
					return;
				}
				@SuppressWarnings("unchecked")
				List<Person> list = (List<Person>) result;
				list.add(new Me(new LatLng(lat, lon)));
				// log("result:" + list);
				removeCircle();
				addMarkers(lat, lon, list);
			}
		});
	}

	public static class TouchableWrapper extends FrameLayout {

		public TouchableWrapper(Context context) {
			super(context);
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_UP:
				GPSCollectorService.call(getContext());
				break;
			}
			return super.dispatchTouchEvent(event);
		}
	}
}
