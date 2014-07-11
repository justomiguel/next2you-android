package com.globant.next2you.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.globant.next2you.R;
import com.globant.next2you.util.UIUtils;

public class NotificationDialog extends Dialog {

	public NotificationDialog(Context ctx) {
		super(ctx);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		setContentView(R.layout.notification_dialog);
		((ImageView) findViewById(R.id.close_notification))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
	}

	public void setMessage(int msgId) {
		TextView tv = (TextView) findViewById(R.id.notification_text);
		UIUtils.prepareTextView(getContext(), tv);
		tv.setText(msgId);
	}

}
