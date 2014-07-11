package com.globant.next2you;

import java.util.concurrent.Callable;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.globant.next2you.async.UICallback;
import com.globant.next2you.fragments.NotificationDialog;
import com.globant.next2you.net.ApiServices;
import com.globant.next2you.objects.CreateUserTokenRequest;
import com.globant.next2you.objects.CreateUserTokenResponse;
import com.globant.next2you.objects.PasswordResetRequest;
import com.globant.next2you.util.UIUtils;
import com.globant.next2you.util.Util;

public class LoginActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle args) {
		super.onCreate(args);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		switchToLoginForm();
	}

	private void switchToLoginForm() {
		setContentView(R.layout.activity_login);

		TextView title = (TextView) findViewById(R.id.login_screen_title);
		UIUtils.prepareTextView(this, title);

		EditText emailEditText = (EditText) findViewById(R.id.enter_email);
		UIUtils.prepareTextView(this, emailEditText);

		EditText passEditText = (EditText) findViewById(R.id.enter_password);
		UIUtils.prepareTextView(this, passEditText);

		Button forgottenPassword = (Button) findViewById(R.id.forgotten_password);
		UIUtils.prepareTextView(this, forgottenPassword);
		forgottenPassword
				.setOnClickListener(getForgottenPasswordClickHandler());

		Button loginBtn = (Button) findViewById(R.id.login_btn);
		UIUtils.prepareTextView(this, loginBtn);
		loginBtn.setOnClickListener(getLoginBtnClickHandler(emailEditText,
				passEditText, loginBtn));
	}

	private OnClickListener getForgottenPasswordClickHandler() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				switchToForgottenPassScreen();
			}
		};
	}

	private void switchToForgottenPassScreen() {
		setContentView(R.layout.activity_forgotten_password);

		TextView title = (TextView) findViewById(R.id.title_forgotten_pass);
		UIUtils.prepareTextView(this, title);

		TextView descr = (TextView) findViewById(R.id.forgotten_pass_descr);
		UIUtils.prepareTextView(this, descr);

		ImageButton close = (ImageButton) findViewById(R.id.close_forgotten_pass_screen);
		close.setOnClickListener(getCloseForgottenPassScreenClickHandler());

		final EditText emailForgottenPass = (EditText) findViewById(R.id.enter_email);
		UIUtils.prepareTextView(this, emailForgottenPass);

		Button sendRequest = (Button) findViewById(R.id.request_forgotten_pass_change);
		UIUtils.prepareTextView(this, sendRequest);
		sendRequest.setOnClickListener(getSendForgottenPasswordRequest(
				emailForgottenPass, sendRequest));
	}

	private OnClickListener getSendForgottenPasswordRequest(
			final EditText emailForgottenPass, final Button sendRequest) {
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				final String email = emailForgottenPass.getText().toString();
				if (email.isEmpty()) {
					showNotification(R.string.login_empty_email);
					return;
				}
				boolean isValid = Util.isValidEmail(email);
				if(!isValid) {
					showNotification(R.string.invalid_email);
					return;
				}
				sendRequest.setEnabled(false);
				App.getTaskManager().assignNet(new Callable<Object>() {

					@Override
					public Object call() throws Exception {
						boolean result = ApiServices.passwordReset(new PasswordResetRequest(
								email));
						return result;
					}
				}, new UICallback() {

					@Override
					public void onResult(Object result) {
						sendRequest.setEnabled(true);
						boolean resetOk = (Boolean) result;
						int msgId = resetOk ? R.string.login_reset_ok
								: R.string.login_reset_failed;
						showNotification(msgId);
					}
				});
			}
		};
	}

	private OnClickListener getCloseForgottenPassScreenClickHandler() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				switchToLoginForm();
			}
		};
	}

	private OnClickListener getLoginBtnClickHandler(
			final TextView emailEditText, final TextView passEditText,
			final Button loginBtn) {
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				String email = emailEditText.getText().toString();
				String pass = passEditText.getText().toString();
				if (email.isEmpty()) {
					showNotification(R.string.login_empty_email);
					return;
				}
				
				if(!Util.isValidEmail(email)) {
					showNotification(R.string.invalid_email);
					return;
				}

				if (pass.isEmpty()) {
					showNotification(R.string.login_empty_pass);
					return;
				}
				makeLoginRequest(email, pass, loginBtn);
			}
		};
	}
	
	private void showNotification(int msgId) {
		NotificationDialog notification = new NotificationDialog(
				LoginActivity.this);
		notification.setMessage(msgId);
		notification.show();
	}

	private void makeLoginRequest(final String email, final String password,
			final Button loginBtn) {
		loginBtn.setText(R.string.login_progress);
		loginBtn.setEnabled(false);
		App.getTaskManager().assignNet(new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				CreateUserTokenRequest request = new CreateUserTokenRequest(
						email, password);
				CreateUserTokenResponse response = ApiServices.createOrUpdateUserToken(request);
				return response;
			}
		}, new UICallback() {

			@Override
			public void onResult(Object result) {
				loginBtn.setText(R.string.login);
				loginBtn.setEnabled(true);
				CreateUserTokenResponse resp = (CreateUserTokenResponse) result;
				if (resp != null) {
					App.app().setAuth(resp);
					Intent i = new Intent(LoginActivity.this,
							AppMainContentActivity.class);
					startActivity(i);
					finish();
				} else {
					NotificationDialog notification = new NotificationDialog(
							LoginActivity.this);
					notification.setMessage(R.string.login_failed);
					notification.show();
				}
			}
		});
	}

}
