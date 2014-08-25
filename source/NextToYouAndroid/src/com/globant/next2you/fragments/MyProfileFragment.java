package com.globant.next2you.fragments;

import java.util.Locale;
import java.util.concurrent.Callable;

import com.globant.next2you.App;
import com.globant.next2you.R;
import com.globant.next2you.async.UICallback;
import com.globant.next2you.net.ApiServices;
import com.globant.next2you.objects.Person;
import com.globant.next2you.objects.ResetCurrentUserPasswordRequest;
import com.globant.next2you.util.UIUtils;
import com.globant.next2you.view.TransparentListOverlay;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MyProfileFragment extends Fragment {
	private static final String TAG = "MyProfileFragment";
	private View holder;
	private TextView usernameText;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		holder = inflater.inflate(R.layout.my_profile, null);
//		usernameText = (TextView) holder.findViewById(R.id.username);
//		UIUtils.prepareTextView(getActivity(), usernameText);
//
//		final EditText oldPass = (EditText) holder.findViewById(R.id.old_pass);
//		final EditText newPass = (EditText) holder.findViewById(R.id.new_pass);
//		final EditText verifyPass = (EditText) holder
//				.findViewById(R.id.verify_pass);
//		UIUtils.prepareTextView(getActivity(), oldPass);
//		UIUtils.prepareTextView(getActivity(), newPass);
//		UIUtils.prepareTextView(getActivity(), verifyPass);
//
//		App.getTaskManager().assignNet(new Callable<Object>() {
//			@Override
//			public Object call() throws Exception {
//				Person p = ApiServices.getPerson(App.app().getAuth().getToken());
//				return p;
//			}
//		}, new UICallback() {
//
//			@Override
//			public void onResult(Object result) {
//				Person p = ((Person) result);
//				ImageView avatarPic = (ImageView) holder
//						.findViewById(R.id.avatar);
//				String userImage = p.getImage();
//				String avatarPicUrl = String.format(Locale.US, "%simages/%s",
//						ApiServices.API_URL, userImage);
//				usernameText.setText(p.getName());
//
//				Log.d(TAG, "avatarPicUrl:" + avatarPicUrl);
//				App.loadImage(avatarPicUrl, avatarPic);
//			}
//		});
//		
//		TransparentListOverlay t = (TransparentListOverlay) holder.findViewById(R.id.avatar_overlay);
//		t.changePaint(getResources().getColor(R.color.login_background));
//		t.invalidate();
//
//		holder.findViewById(R.id.reset_pass).setOnClickListener(
//				new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						final String oldPassTxt = oldPass.getText().toString();
//						final String newPassTxt = newPass.getText().toString();
//						String newPassTxt1 = verifyPass.getText().toString();
//
//						if (!newPassTxt.equals(newPassTxt1)) {
//							NotificationDialog nd = new NotificationDialog(
//									getActivity());
//							nd.setMessage(R.string.fill_in_pass);
//							nd.show();
//							return;
//						}
//
//						App.getTaskManager().assignNet(new Callable<Object>() {
//							@Override
//							public Object call() throws Exception {
//								boolean ok = ApiServices.resetCurrentUserPassword(App.app()
//										.getAuth().getToken(),
//										new ResetCurrentUserPasswordRequest(
//												oldPassTxt, newPassTxt));
//								return ok;
//							}
//						}, new UICallback() {
//							
//							@Override
//							public void onResult(Object result) {
//								boolean ok = ((Boolean)result).booleanValue();
//								if(!ok) {
//									NotificationDialog nd = new NotificationDialog(
//											getActivity());
//									nd.setMessage(R.string.fill_in_pass);
//									nd.show();
//								}
//							}
//						});
//					}
//				});

		return holder;
	}
}
