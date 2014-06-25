package com.globant.next2you.fragments;

import android.support.v4.app.Fragment;
import android.util.Log;

public abstract class BaseFragment extends Fragment {

	protected abstract String getLogTag();

	protected void log(String msg) {
		Log.d(getLogTag(), msg);
	}

	protected void log(Exception e) {
		Log.e(getLogTag(), "", e);
	}

	protected void log(String msg, Exception e) {
		Log.e(getLogTag(), msg, e);
	}
}
