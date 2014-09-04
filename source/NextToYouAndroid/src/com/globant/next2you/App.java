package com.globant.next2you;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.Date;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.globant.next2you.async.Callback;
import com.globant.next2you.async.TaskManager;
import com.globant.next2you.net.ApiServices;
import com.globant.next2you.objects.CreateUserTokenResponse;
import com.globant.next2you.objects.Destination;
import com.globant.next2you.objects.RetrievePendingTravelsResponse;
import com.globant.next2you.objects.Travel;
import com.globant.next2you.objects.TravelPersonResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class App extends Application {
	@SuppressWarnings("unused")
	private static final String TAG = "App";
	private TaskManager taskManager;
	private CreateUserTokenResponse auth;
	private ImageLoaderConfiguration config;
	// private DisplayImageOptions options;
	private ImageLoader loader;
	public String currentCommunity = "";
	public LatLngBounds mapBounds = new LatLngBounds(new LatLng(0, 0),
			new LatLng(0, 0));
	public LatLng userLocation = new LatLng(0, 0);
	private int currentTravelId = 0;

	private static App app;
	private static final String SHARED_PREFS_NEXT2YOU = "next2you_app";
	
	public State userState = State.NONE;
	public Destination currentDestination = null;
	public Date travelDateRequested;
	public ArrayList<Long> ignoreList = new ArrayList<Long>();

	public enum State {
		NONE, ASK, OFFER
	}
	
	public Callable<Object> getOfferedTravelsTask() {
		return new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				TravelPersonResponse response = ApiServices
						.listSimpleTravels(App.app().getAuth().getToken());
				if (response != null) {
					return filterTravelsFromIgnoreList(response.getTravels());
				}
				return null;
			}
		};
	}
	
	public void loadOfferedTavels(Callback callback) {
		getTaskManager().assignNet(getOfferedTravelsTask(), callback);
	}
	
	public void loadPendingTavels(Callback callback) {
		getTaskManager().assignNet(getLoadPendingTravelsTask(), callback);
	}
	
	public Callable<Object> getLoadPendingTravelsTask() {
		return new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				String currentToken = App.app().getAuth().getToken();
				RetrievePendingTravelsResponse pendingTravels = ApiServices.retrievePendingTravels(currentToken);
				ArrayList<Travel> travels = pendingTravels != null ? pendingTravels
						.getTravels() : null;
				travels = filterTravelsFromIgnoreList(travels);
				if (travels != null) {
					Log.d(TAG, "pendingTravels:" + travels.size());
				}
				return travels;
			}
		};
	}
	
	private ArrayList<Travel> filterTravelsFromIgnoreList(
			ArrayList<Travel> listToFilter) {
		if (App.app().ignoreList.isEmpty()) {
			return listToFilter;
		}
		ArrayList<Travel> filteredList = new ArrayList<Travel>();
		for (Travel t : listToFilter) {
			if (!App.app().ignoreList.contains(t.getTravelId())) {
				filteredList.add(t);
			}
		}
		return filteredList;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		app = this;
		initImageLoader();
		setCurrentTravelId(getSharedPreferences(SHARED_PREFS_NEXT2YOU,
				Context.MODE_PRIVATE).getInt("travelId", 0));
	}

	public static TaskManager getTaskManager() {
		App app = app();
		if (app.taskManager == null) {
			app.taskManager = new TaskManager();
		}
		return app.taskManager;
	}

	private void initImageLoader() {
		config = new ImageLoaderConfiguration.Builder(getApplicationContext())
				.build();
		loader = ImageLoader.getInstance();
		loader.init(config);
		// options = new DisplayImageOptions.Builder().build();
	}

	public static void loadImage(String imageUri, ImageView imageView) {
		app().loader.displayImage(imageUri, imageView);
	}

	public static App app() {
		return app;
	}

	public CreateUserTokenResponse getAuth() {
		return auth;
	}

	public void setAuth(CreateUserTokenResponse auth) {
		this.auth = auth;
	}

	public int getCurrentTravelId() {
		return currentTravelId;
	}

	public void setCurrentTravelId(int currentTravelId) {
		this.currentTravelId = currentTravelId;
		getSharedPreferences(SHARED_PREFS_NEXT2YOU, Context.MODE_PRIVATE)
				.edit().putInt("travelId", currentTravelId).commit();
	}

}
