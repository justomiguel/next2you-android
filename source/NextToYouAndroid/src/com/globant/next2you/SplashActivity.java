package com.globant.next2you;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.TextView;

import com.globant.next2you.util.UIUtils;

public class SplashActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle args) {
		super.onCreate(args);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fragment_splash);

		TextView title = (TextView) findViewById(R.id.travel_company_title);
		UIUtils.prepareTextView(this, title);

		TextView appVersionInfo = (TextView) findViewById(R.id.app_version_info);
		UIUtils.prepareTextView(this, appVersionInfo);

		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Intent i = new Intent(SplashActivity.this, LoginActivity.class);
				startActivity(i);
				finish();
			}
		}, 500);
	}

}
