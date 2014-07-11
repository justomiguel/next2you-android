package com.globant.next2you;

import android.app.Application;

import com.globant.next2you.async.TaskManager;
import com.globant.next2you.objects.CreateUserTokenResponse;

public class App extends Application {
	@SuppressWarnings("unused")
	private static final String TAG = "App";
	private TaskManager taskManager;
	private CreateUserTokenResponse auth;
	
	private static App app;
	
	@Override
	public void onCreate() {
		super.onCreate();
		app = this;
	}
	
	public static TaskManager getTaskManager() {
		App app = app();
		if(app.taskManager == null) {
			app.taskManager = new TaskManager();
		}
		return app.taskManager;
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
