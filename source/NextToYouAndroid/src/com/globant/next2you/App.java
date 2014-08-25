package com.globant.next2you;

import android.app.Application;
import android.widget.ImageView;

import com.globant.next2you.async.TaskManager;
import com.globant.next2you.objects.CreateUserTokenResponse;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class App extends Application {
	@SuppressWarnings("unused")
	private static final String TAG = "App";
	private TaskManager taskManager;
	private CreateUserTokenResponse auth;
	private ImageLoaderConfiguration config;
	private DisplayImageOptions options;
	private ImageLoader loader;
	public String currentCommunity = "";
	
	private static App app;

	@Override
	public void onCreate() {
		super.onCreate();
		app = this;
		initImageLoader();
	}

	public static TaskManager getTaskManager() {
		App app = app();
		if (app.taskManager == null) {
			app.taskManager = new TaskManager();
		}
		return app.taskManager;
	}

	private void initImageLoader() {
		config = new ImageLoaderConfiguration.Builder(
				getApplicationContext()).build();
		loader = ImageLoader.getInstance();
		loader.init(config);
		options = new DisplayImageOptions.Builder().build();
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

}
