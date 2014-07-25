package com.globant.next2you;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.globant.next2you.fragments.FragmentMap;
import com.globant.next2you.util.UIUtils;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class AppMainContentActivity extends FragmentActivity {
	private SlidingMenu slidingMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		initAndOpenMapScreen();
	}
	
	public void initAndOpenMapScreen() {
		setContentView(R.layout.app_main_content_screen);
		initSlidingMenu();

		setupMenuToggle();
		
		findViewById(R.id.send_email).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_EMAIL, "emailaddress@emailaddress.com");
				intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
				intent.putExtra(Intent.EXTRA_TEXT, "Email body.");
				startActivity(Intent.createChooser(intent, "Send Email"));
			}
		});

		TextView subtitle = (TextView) findViewById(R.id.subtitle);
		UIUtils.prepareTextView(this, subtitle);

		openSection(0);
	}

	public void openSection(int idx) {
		Fragment fragment = null;
		switch (idx) {
		case 0:
			fragment = new FragmentMap();
			break;
		default:
			break;
		}

		if (fragment != null) {
			FragmentTransaction tr = getSupportFragmentManager()
					.beginTransaction();
			tr.replace(R.id.container, fragment, fragment.getClass()
					.getSimpleName());
			tr.commit();
		}
	}

	private void setupMenuToggle() {
		findViewById(R.id.sliding_menu_toggle).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						slidingMenu.toggle(true);
						toggleShadowOverlay();
					}
				});
	}
	
	private void toggleShadowOverlay() {
		View shadow = findViewById(R.id.shadow_overlay);
		int visibility = shadow.getVisibility();
		visibility = visibility == View.VISIBLE ? View.GONE : View.VISIBLE;
		shadow.setVisibility(visibility);
	}

	private void initSlidingMenu() {
		slidingMenu = new SlidingMenu(this);
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		slidingMenu.setShadowWidthRes(R.dimen.slidingmenuWidth);
		slidingMenu.setBehindOffsetRes(R.dimen.slidingmenuOffset);
		slidingMenu.setFadeDegree(0.35f);
		slidingMenu.setSlidingEnabled(false);
		slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		ListView leftMenu = (ListView) LayoutInflater.from(this).inflate(
				R.layout.sliding_menu_options, null);
		leftMenu.setAdapter(new LeftMenuOptionsAdapter(this));
		leftMenu.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				if(pos == 0) {
					openSection(0);
					slidingMenu.toggle(true);
					toggleShadowOverlay();
				} else if (pos == 4) {
					// logout
					App.app().setAuth(null);
					Intent i = new Intent(AppMainContentActivity.this,
							LoginActivity.class);
					startActivity(i);
					finish();
				}
			}
		});
		slidingMenu.setMenu(leftMenu);
	}

	private class LeftMenuOptionsAdapter extends BaseAdapter {
		private String[] options;

		public LeftMenuOptionsAdapter(Context ctx) {
			options = ctx.getResources().getStringArray(
					R.array.sliding_menu_opitions);
		}

		@Override
		public int getCount() {
			return options.length;
		}

		@Override
		public Object getItem(int position) {
			return String.valueOf(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = (TextView) convertView;
			if (tv == null) {
				Context ctx = parent.getContext();
				tv = new TextView(parent.getContext());
				Resources res = ctx.getResources();
				int height = (int) res
						.getDimension(R.dimen.slidingmenuOptionHeight);
				AbsListView.LayoutParams params = new AbsListView.LayoutParams(
						android.widget.AbsListView.LayoutParams.MATCH_PARENT,
						height);
				int paddingLeft = (int) res
						.getDimension(R.dimen.slidingmenuOptionPaddingLeft);
				tv.setPadding(paddingLeft, 0, 0, 0);
				tv.setLayoutParams(params);
				tv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				UIUtils.prepareTextView(ctx, tv);
				tv.setTextColor(res.getColor(R.color.purple));
				tv.setTextSize(16f);
			}
			tv.setText(options[position]);
			return tv;
		}
	}
}
