package com.globant.next2you;

import java.util.Date;

import android.app.Application;
import android.content.Context;
import android.widget.ImageView;

import com.globant.next2you.async.TaskManager;
import com.globant.next2you.objects.CreateUserTokenResponse;
import com.globant.next2you.objects.Destination;
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
	
	public enum State {
		NONE, ASK, OFFER
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
