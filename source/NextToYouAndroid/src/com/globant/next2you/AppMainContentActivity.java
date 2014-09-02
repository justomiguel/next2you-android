package com.globant.next2you;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.globant.next2you.fragments.AskRideScreen;
import com.globant.next2you.fragments.FragmentMap;
import com.globant.next2you.objects.Community;
import com.globant.next2you.util.UIUtils;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class AppMainContentActivity extends FragmentActivity {
	private static final String TAG = "AppMainContentActivity";
	public static final String UPDATE_COMMUNITY = "current_community";
	private SlidingMenu slidingMenu;
	private BroadcastReceiver receiver;
	private AskRideScreen askRideScreen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		initAndOpenMapScreen();
		
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				((TextView)findViewById(R.id.subtitle)).setText(App.app().currentCommunity);
			}
		};
		registerReceiver(receiver , new IntentFilter(UPDATE_COMMUNITY));
		
		// detect swipe on header view
//		final GestureDetector gestureDetector = new GestureDetector(this, new MyGestureDetector());
//        OnTouchListener gestureListener = new View.OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event) {
//                return gestureDetector.onTouchEvent(event);
//            }
//        };
//        findViewById(R.id.logo).setOnTouchListener(gestureListener);
	}
	
	private void openCloseAskRideScreen(boolean checkToCloseOnly) {
		boolean menuOpened = askRideScreen != null;
		if(!menuOpened && checkToCloseOnly) {
			return;
		}
		
		findViewById(R.id.subtitle).setVisibility(menuOpened ? View.VISIBLE : View.GONE);
		findViewById(R.id.pointer_ask_ride_screen).setVisibility(menuOpened ? View.GONE : View.VISIBLE);
		if(menuOpened) {
			askRideScreen.close();
			askRideScreen = null;
			openSection(0);
		} else {
			LinearLayout container = (LinearLayout) findViewById(R.id.container);
			container.removeAllViews();
			askRideScreen = new AskRideScreen(this);
			askRideScreen.initialize(container);
		}
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
		ArrayList<Community> communities = App.app().getAuth().getCommunities();
		if (communities != null && communities.size() > 0) {
			subtitle.setText(communities.get(0).getName());
		}
		UIUtils.prepareTextView(this, subtitle);

		openSection(0);
		
		findViewById(R.id.message_icon).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openCloseAskRideScreen(false);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unregisterReceiver(receiver);
		} catch (Exception e) {
		}
	}
	
	public void openSection(int idx) {
		Log.d(TAG, "open section:" + idx);
		Fragment fragment = null;
		if(idx != 1) {
			openCloseAskRideScreen(true);
		}
		switch (idx) {
		case 0:
			fragment = new FragmentMap();
			findViewById(R.id.subtitle).setVisibility(View.VISIBLE);
			break;
		case 1:
			openCloseAskRideScreen(false);
			return;
		case 3:
			startActivity(new Intent(this, MyProfileActivity.class));
			overridePendingTransition(R.anim.slide_out_animation,
					R.anim.slide_in_animation);
			return;
		case 2:
			startActivity(new Intent(this, ChangeCommunityScreen.class));
			overridePendingTransition(R.anim.slide_out_animation,
					R.anim.slide_in_animation);
			return;
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
						toggleSlidingMenu();
					}
				});
	}
	
	private void toggleSlidingMenu() {
		slidingMenu.toggle(true);
		toggleShadowOverlay();
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
				} else if(pos == 3) {
					openSection(3);
					slidingMenu.toggle(true);
					toggleShadowOverlay();
				} else if(pos == 2) {
					openSection(2);
					slidingMenu.toggle(true);
					toggleShadowOverlay();
				} else if(pos == 1) {
					openSection(1);
					slidingMenu.toggle(true);
					toggleShadowOverlay();
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
	
	
	private class MyGestureDetector extends SimpleOnGestureListener {
		private static final int SWIPE_MIN_DISTANCE = 120;
	    private static final int SWIPE_MAX_OFF_PATH = 250;
	    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	    
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	toggleSlidingMenu();
                }
            } catch (Exception e) {
            	Log.e(TAG, "", e);
            }
            return false;
        }

            @Override
        public boolean onDown(MotionEvent e) {
              return true;
        }
    }
}
