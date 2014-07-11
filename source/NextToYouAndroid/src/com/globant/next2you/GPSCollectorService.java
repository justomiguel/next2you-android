package com.globant.next2you;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GPSCollectorService extends Service implements
		LocationListener {
	private static final String TAG = "GPSCollectorService";
	private LocationManager locationService;
	public static final String BROADCAST_INTENT_FILTER = "com.globant.next2you.LOCATIONGPSHANDLER";
	public static final String LAT = "LATITUDE";
	public static final String LON = "LONGITUDE";
	public static final String ACCURACY = "ACCURACY";
	private static boolean inProgress = false;
	
	public static void call(Context ctx) {
		if(inProgress) {
			return;
		}
		Intent i = new Intent(ctx, GPSCollectorService.class);
		ctx.startService(i);
	}
	
	public GPSCollectorService() {
	}

	@Override
	public void onCreate() {
		try {
			locationService = (LocationManager) getSystemService(LOCATION_SERVICE);
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		inProgress = true;
		int res = super.onStartCommand(intent, flags, startId);
		try {
			Log.d(TAG, "onHandleIntent");
			final int maxAge = 1000 * 30; // 1 minute
			long bestAge = 0;

			Location lastValid = null;
			long time;
			Location loc = locationService
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (loc != null) {
				time = loc.getTime();
				if (time > System.currentTimeMillis() - maxAge) {
					bestAge = time;
					lastValid = loc;
				}
			}
			loc = locationService
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (loc != null) {
				time = loc.getTime();
				if (time > System.currentTimeMillis() - maxAge
						&& time > bestAge) {
					bestAge = time;
					lastValid = loc;
				}
			}
			loc = locationService
					.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
			if (loc != null) {
				time = loc.getTime();
				if (time > System.currentTimeMillis() - maxAge
						&& time > bestAge) {
					bestAge = time;
					lastValid = loc;
				}
			}
			if (lastValid != null) {
				Log.d(TAG, "reuse cached location");
				sendLocation(lastValid);
				return res;
			}
			// boolean networkProviderEnabled = locationService
			// .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			// boolean gpsProviderEnabled = locationService
			// .isProviderEnabled(LocationManager.GPS_PROVIDER);
			// boolean passiveProviderEnabled = locationService
			// .isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
			// if (networkProviderEnabled) {
			// } else if (gpsProviderEnabled) {
			// }
			locationService.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, this);
			locationService.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, this);
			locationService.requestLocationUpdates(
					LocationManager.PASSIVE_PROVIDER, 0, 0, this);
		} catch (Exception e) {
			Log.e(TAG, "", e);
			stopService();
			inProgress = false;
		}
		return res;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "onLocationChanged");
		sendLocation(location);
	}

	private void stopService() {
		inProgress = false;
		locationService.removeUpdates(this);
		stopSelf();
	}

	private void sendLocation(Location location) {
		synchronized (this) {
			try {
				Log.d(TAG, "sendLocation");
				double lat = location.getLatitude();
				double lng = location.getLongitude();
				float accuracy = location.getAccuracy();
				Intent i = new Intent(BROADCAST_INTENT_FILTER);
				i.putExtra(LAT, Double.valueOf(lat));
				i.putExtra(LON, Double.valueOf(lng));
				i.putExtra(ACCURACY, accuracy);
				sendBroadcast(i);
			} catch (Exception e) {
				Log.d(TAG, "", e);
			} finally {
				stopService();
			}
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(TAG, "onProviderDisabled=" + provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, "onProviderEnabled=" + provider);
		locationService.requestLocationUpdates(provider, 0, 0, this);
		startService(new Intent(this, GPSCollectorService.class));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG, "onStatusChanged provider=" + provider + ";status=" + status
				+ ";extras=" + extras);
	}

}
