package com.globant.next2you.fragments;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.Callable;

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
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globant.next2you.App;
import com.globant.next2you.R;
import com.globant.next2you.async.UICallback;
import com.globant.next2you.net.ApiServices;
import com.globant.next2you.objects.Person;
import com.globant.next2you.util.UIUtils;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class FragmentMap extends BaseFragment implements
		ClusterManager.OnClusterClickListener<Person>,
		ClusterManager.OnClusterInfoWindowClickListener<Person>,
		ClusterManager.OnClusterItemClickListener<Person>,
		ClusterManager.OnClusterItemInfoWindowClickListener<Person> {
	private GoogleMap map;
	private SupportMapFragment mMapFragment;

	public FragmentMap() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_map, container,
				false);

		// workaround to have non null map when creating SupportMapFragment in code instead of in xml
		mMapFragment = new SupportMapFragment() {
			@Override
			public void onActivityCreated(Bundle savedInstanceState) {
				super.onActivityCreated(savedInstanceState);
				map = mMapFragment.getMap();
				if (map != null) {
					try {
						MapsInitializer.initialize(getActivity());
					} catch (GooglePlayServicesNotAvailableException e) {
						log(e);
					}
					loadPeople();
				}
			}
		};

		FragmentTransaction transaction = getChildFragmentManager()
				.beginTransaction();
		transaction.add(R.id.fragment_map_root, mMapFragment).commit();

		return rootView;
	}

	public void drawCircle(LatLng center, int radius) {
		Resources res = getActivity().getResources();
		map.addCircle(new CircleOptions()
        .center(new LatLng(center.latitude, center.longitude))
        .radius(radius)
        .strokeColor(Color.TRANSPARENT)
        .fillColor(res.getColor(R.color.marker_radius_purple)));
	}

	@Override
	protected String getLogTag() {
		return "FragmentMap";
	}

	// *************** Clusters ******************
	private ClusterManager<Person> mClusterManager;
	private Random mRandom = new Random(1984);

	/**
	 * Draws profile photos inside markers (using IconGenerator). When there are
	 * multiple people in the cluster, draw multiple photos (using
	 * MultiDrawable).
	 */
	private class PersonRenderer extends DefaultClusterRenderer<Person> {
		// private final IconGenerator mIconGenerator = new
		// IconGenerator(getActivity().getApplicationContext());
		// private final IconGenerator mClusterIconGenerator = new
		// IconGenerator(getActivity().getApplicationContext());

		public PersonRenderer() {
			super(getActivity().getApplicationContext(), map, mClusterManager);

			// View multiProfile =
			// getActivity().getLayoutInflater().inflate(R.layout.multi_profile,
			// null);
			// mClusterIconGenerator.setContentView(multiProfile);
			// mClusterImageView = (ImageView)
			// multiProfile.findViewById(R.id.image);

		}

		@Override
		protected void onBeforeClusterItemRendered(Person person,
				MarkerOptions markerOptions) {

			if (person instanceof Me) {
				Bitmap icon = BitmapFactory.decodeResource(getResources(),
						R.drawable.marker_my_position);
				markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
						.title(person.getAddress());
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
				if(person instanceof Me) {
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
			
			Bitmap me = type5 > 0 ? createMarker(R.drawable.marker_my_position, type5, R.color.purple) : null;
			Bitmap type1Bitmap = type1 > 0 ? createMarker(R.drawable.car, type1, R.color.red_marker) : null;
			Bitmap type2Bitmap = type2 > 0 ? createMarker(R.drawable.black_car, type2,
					R.color.black_marker) : null;
			Bitmap type3Bitmap = type3 > 0 ? createMarker(R.drawable.green_smiley, type3,
					R.color.green_marker) : null;
			Bitmap type4Bitmap = type4 > 0 ? createMarker(R.drawable.black_smiley, type4,
					R.color.black_marker) : null;
			
			Bitmap composed = compose(me, type1Bitmap, type2Bitmap, type3Bitmap, type4Bitmap);
			return composed;
		}

		private Bitmap compose(Bitmap... arr) {
			int totalWidth = 0;
			int height = 0;
			for (int i = 0; i < arr.length; i++) {
				Bitmap b = arr[i];
				if(b != null) {
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
				if(b != null) {
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

	protected void addMarkers() {
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186,
				-0.126446), 9.5f));

		mClusterManager = new ClusterManager<Person>(getActivity(), map);
		mClusterManager.setRenderer(new PersonRenderer());
		map.setOnCameraChangeListener(mClusterManager);
		map.setOnMarkerClickListener(mClusterManager);
		map.setOnInfoWindowClickListener(mClusterManager);
		mClusterManager.setOnClusterClickListener(this);
		mClusterManager.setOnClusterInfoWindowClickListener(this);
		mClusterManager.setOnClusterItemClickListener(this);
		mClusterManager.setOnClusterItemInfoWindowClickListener(this);

		addItems();
		mClusterManager.cluster();
	}

	private void addItems() {
		Person p = new Person();
		p.setHasCar(true);
		p.setLatLng(position());
		mClusterManager.addItem(p);

		p = new Person();
		p.setLatLng(position());
		p.setHasCar(true);
		mClusterManager.addItem(p);

		p = new Person();
		p.setLatLng(position());
		p.setHasCar(true);
		p.setHasActiveTravel(true);
		mClusterManager.addItem(p);

		p = new Person();
		p.setLatLng(position());
		p.setHasCar(false);
		p.setHasActiveTravel(true);
		mClusterManager.addItem(p);

		p = new Person();
		p.setLatLng(position());
		p.setHasCar(false);
		p.setHasActiveTravel(true);
		mClusterManager.addItem(p);

		p = new Person();
		p.setLatLng(position());
		p.setHasCar(false);
		p.setHasActiveTravel(false);
		mClusterManager.addItem(p);

		p = new Person();
		p.setLatLng(position());
		p.setHasCar(false);
		p.setHasActiveTravel(false);
		mClusterManager.addItem(p);

		p = new Person();
		p.setLatLng(position());
		p.setHasCar(false);
		p.setHasActiveTravel(true);
		mClusterManager.addItem(p);

		p = new Person();
		p.setLatLng(position());
		p.setHasCar(false);
		p.setHasActiveTravel(true);
		mClusterManager.addItem(p);

		Resources res = getActivity().getResources();
		int radius = (int) res.getDimension(R.dimen.map_my_location_radius);
		LatLng pos = position();
		drawCircle(pos, radius);
		// map.addMarker(
		// new MarkerOptions().position(pos)
		// .icon(BitmapDescriptorFactory
		// .fromResource(R.drawable.marker_my_position)));
		mClusterManager.addItem(new Me(pos));
	}

	private LatLng position() {
		return new LatLng(random(51.6723432, 51.38494009999999), random(
				0.148271, -0.3514683));
	}

	private double random(double min, double max) {
		return mRandom.nextDouble() * (max - min) + min;
	}

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

	// ********* load people *************
	private void loadPeople() {
		App.getTaskManager().assignNet(new Callable<Object>() {
			
			@Override
			public Object call() throws Exception {
				String token = App.app().getAuth().getToken();
				return ApiServices.getPeople(token);
			}
		}, new UICallback() {
			
			@Override
			public void onResult(Object result) {
				addMarkers();
			}
		});
	}
}
