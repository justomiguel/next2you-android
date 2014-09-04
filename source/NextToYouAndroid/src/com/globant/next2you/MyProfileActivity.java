package com.globant.next2you;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.globant.next2you.async.UICallback;
import com.globant.next2you.fragments.NotificationDialog;
import com.globant.next2you.net.ApiServices;
import com.globant.next2you.objects.Destination;
import com.globant.next2you.objects.ListDestinationsResponse;
import com.globant.next2you.objects.Person;
import com.globant.next2you.util.UIUtils;
import com.globant.next2you.util.Util;
import com.globant.next2you.view.LockableScrollView;
import com.globant.next2you.view.TransparentListOverlay;

public class MyProfileActivity extends FragmentActivity {
	private static final String TAG = "MyProfileActivity";
	private static final int CAMERA_REQUEST = 12780;
	private static final int REQ_CODE_PICK_IMAGE = 12781;
	private ArrayList<Destination> destinations;
	private EditText locationSelection;
	private boolean showAddress;
	private LockableScrollView scrollView;
	private ListView locationsList;
	private RelativeLayout rootView;
	private boolean destListShown = false;
	private DestListAdapter adapter;
	private int selectedPosition;
	private Bitmap lastPhoto;
	private Person currentPerson;
	private EditText address;
	private EditText nickname;
	private EditText email;
	private EditText comments;
	private Button saveChanges;

	@Override
	protected void onCreate(Bundle args) {
		super.onCreate(args);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_profile);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		TextView myProfileTitle = (TextView) findViewById(R.id.my_profile_title);
		TextView changePhoto = (TextView) findViewById(R.id.change_photo_btn);
		TextView showExactAddr = (TextView) findViewById(R.id.show_exact_addr);
		nickname = (EditText) findViewById(R.id.nickname);
		email = (EditText) findViewById(R.id.email);
		locationSelection = (EditText) findViewById(R.id.location);
		scrollView = (LockableScrollView) findViewById(R.id.scroll_view);
		rootView = (RelativeLayout) findViewById(R.id.rootview);

		setupAvatarUI();

		setupLocationsDropDown();

		address = (EditText) findViewById(R.id.address);
		TextView sampleAddress = (TextView) findViewById(R.id.example_address);
		UIUtils.prepareTextView(this, sampleAddress);
		TextView mendatoryFields = (TextView) findViewById(R.id.mendatory_fields_holder);
		comments = (EditText) findViewById(R.id.comments);
		saveChanges = (Button) findViewById(R.id.save_changes);
		saveChanges.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveChanges.setEnabled(false);
				updatePerson();
			}
		});

		UIUtils.prepareTextView(this, showExactAddr);
		UIUtils.prepareTextView(this, saveChanges);
		UIUtils.prepareTextView(this, comments);
		UIUtils.prepareTextView(this, mendatoryFields);
		UIUtils.prepareTextView(this, address);
		UIUtils.prepareTextView(this, email);
		UIUtils.prepareTextView(this, locationSelection);
		UIUtils.prepareTextView(this, nickname);
		UIUtils.prepareTextView(this, changePhoto);
		UIUtils.prepareTextView(this, myProfileTitle);
		final ImageView showLocation = (ImageView) findViewById(R.id.loc_status);
		findViewById(R.id.exact_addr_holder).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						showAddress = !showAddress;
						showLocation
								.setImageResource(showAddress ? R.drawable.show_location_on
										: R.drawable.show_location_off);
					}
				});

		loadAndShowAvatar(showLocation);
		setupGoBackBtn();
	}

	private void setupGoBackBtn() {
		findViewById(R.id.go_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.slide_out_animation,
						R.anim.slide_in_animation);
			}
		});
	}

	private void loadAndShowAvatar(final ImageView showLocation) {
		App.getTaskManager().assignNet(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				Person p = ApiServices.getPerson(App.app().getAuth().getToken());
				return p;
			}
		}, new UICallback() {

			@Override
			public void onResult(Object result) {
				Person p = ((Person) result);
				currentPerson = p;
				ImageView avatarPic = (ImageView) findViewById(R.id.avatar);
				String userImage = p.getImage();
				String avatarPicUrl = String.format(Locale.US, "%simages/%s",
						ApiServices.API_URL, userImage);

				App.loadImage(avatarPicUrl, avatarPic);
				nickname.setText(p.getNickname());
				email.setText(p.getEmail());
				address.setText(p.getAddress());
				comments.setText(p.getComments());
				loadDestinations(p);
				showAddress = p.getIsAddressVisible();
				showLocation
						.setImageResource(showAddress ? R.drawable.show_location_on
								: R.drawable.show_location_off);
			}
		});

		TransparentListOverlay t = (TransparentListOverlay) findViewById(R.id.avatar_overlay);
		t.changePaint(getResources().getColor(R.color.grapefruit));
		t.markerSize = Util.dpToPx(this, 90);
		t.init();
		t.invalidate();
	}

	private void setupAvatarUI() {
		TextView useLastPhoto = (TextView) findViewById(R.id.use_last_photo);
		TextView takeNewPhoto = (TextView) findViewById(R.id.take_new_photo);
		TextView selectFromLibrary = (TextView) findViewById(R.id.select_from_library);
		TextView cancel = (TextView) findViewById(R.id.cancel_photo);
		OnClickListener closeAddPhotoHolderListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeAddPhotoHolder();
			}
		};
		cancel.setOnClickListener(closeAddPhotoHolderListener);
		// findViewById(R.id.close_add_photo_holder_top).setOnClickListener(closeAddPhotoHolderListener);
		// findViewById(R.id.close_add_photo_holder_bottom).setOnClickListener(closeAddPhotoHolderListener);
		useLastPhoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (lastPhoto != null) {
					ImageView avatarPic = (ImageView) findViewById(R.id.avatar);
					avatarPic.setImageBitmap(lastPhoto);
				}
				View view = findViewById(R.id.add_photo_holder);
				view.setVisibility(View.GONE);
				scrollView.setScrollingEnabled(true);
			}
		});
		selectFromLibrary.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);
				View view = findViewById(R.id.add_photo_holder);
				view.setVisibility(View.GONE);
				scrollView.setScrollingEnabled(true);
			}
		});
		takeNewPhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent cameraIntent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(cameraIntent, CAMERA_REQUEST);

				View view = findViewById(R.id.add_photo_holder);
				view.setVisibility(View.GONE);
				scrollView.setScrollingEnabled(true);
			}
		});
		UIUtils.prepareTextView(this, cancel);
		UIUtils.prepareTextView(this, useLastPhoto);
		UIUtils.prepareTextView(this, takeNewPhoto);
		UIUtils.prepareTextView(this, selectFromLibrary);

		findViewById(R.id.avatar_holder).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						// because we are in a scroll view we need to do it
						// programatically
						View view = findViewById(R.id.add_photo_holder);
						view.setVisibility(View.VISIBLE);
						int height = getWindow().getDecorView().getHeight()
								- (int) getResources().getDimension(
										R.dimen.login_screen_header_height);
						rootView.bringChildToFront(view);
						RelativeLayout.LayoutParams params = (LayoutParams) view
								.getLayoutParams();
						params.height = height;
						view.setLayoutParams(params);
						scrollView.scrollTo(0, 0);
						scrollView.setScrollingEnabled(false);
						hideKeyboard();
					}
				});
	}

	private void setupLocationsDropDown() {
		locationSelection.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "show locations");
				if (destinations.size() > 0) {
					View scrollAssistant = findViewById(R.id.scroll_assistant);
					scrollAssistant.setVisibility(View.VISIBLE);
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							destListShown = true;
							adapter.notifyDataSetChanged();

							int height = getWindow().getDecorView().getHeight()
									/*- (int) getResources().getDimension(
											R.dimen.login_screen_header_height)*/;
							RelativeLayout.LayoutParams params = (LayoutParams) locationsList
									.getLayoutParams();
							params.height = height;
							locationsList.setLayoutParams(params);
							scrollView.scrollTo(0, 0);
							scrollView.setScrollingEnabled(false);
							ObjectAnimator yTranslate = ObjectAnimator.ofInt(
									scrollView, "scrollY",
									locationsList.getTop());
							AnimatorSet animators = new AnimatorSet();
							animators.setDuration(500L);
							animators.playTogether(yTranslate);
							animators.addListener(new AnimatorListener() {
								@Override
								public void onAnimationStart(Animator animation) {
								}

								@Override
								public void onAnimationRepeat(Animator animation) {
								}

								@Override
								public void onAnimationEnd(Animator animation) {
									locationsList.setVisibility(View.VISIBLE);
									hideKeyboard();
								}

								@Override
								public void onAnimationCancel(Animator animation) {
								}
							});
							animators.start();
						}
					});
				}
			}
		});
	}

	private void closeAddPhotoHolder() {
		View view = findViewById(R.id.add_photo_holder);
		view.setVisibility(View.GONE);
		scrollView.setScrollingEnabled(true);
	}

	private void hideKeyboard() {
		try {
			InputMethodManager inputManager = (InputMethodManager) this
					.getSystemService(Context.INPUT_METHOD_SERVICE);

			View view = this.getCurrentFocus();
			if (view == null) {
				return;
			}
			inputManager.hideSoftInputFromWindow(view.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
			view.clearFocus();
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
	}

	@Override
	public void onBackPressed() {
		if (destListShown) {
			closeList();
		} else if (findViewById(R.id.add_photo_holder).getVisibility() == View.VISIBLE) {
			closeAddPhotoHolder();
		} else {
			super.onBackPressed();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ImageView avatarPic = (ImageView) findViewById(R.id.avatar);
		try {
			if (lastPhoto == null) {
				lastPhoto = ((BitmapDrawable) avatarPic.getDrawable()).getBitmap();
			}
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
		scrollView.setScrollingEnabled(true);
		try {
			Bitmap bitmap = null;
			if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
				bitmap = (Bitmap) data.getExtras().get("data");
				avatarPic.setImageBitmap(bitmap);
			} else if (requestCode == REQ_CODE_PICK_IMAGE
					&& resultCode == RESULT_OK) {
				Uri selectedImage = data.getData();
				bitmap = MediaStore.Images.Media.getBitmap(
						this.getContentResolver(), selectedImage);
				avatarPic.setImageBitmap(bitmap);
			}
			if (bitmap != null) {
				final Bitmap bitmapF = bitmap;
				App.getTaskManager().assignNet(new Callable<Object>() {

					@Override
					public Object call() throws Exception {
						String url = String.format(Locale.US, "%s%s",
								ApiServices.API_URL, "images");
						// upload(url, bitmapF);
						String res = multipartRequest(url, "", bitmapF, "file");
						Log.d(TAG, "bitmap upload result:" + res);
						return null;
					}
				});
			}
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
	}

	private void loadDestinations(final Person p) {
		App.getTaskManager().assignNet(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				return ApiServices.listDestinations(App.app().getAuth()
						.getToken());
			}
		}, new UICallback() {

			@Override
			public void onResult(Object result) {
				ListDestinationsResponse dest = (ListDestinationsResponse) result;
				destinations = dest.getDestinations();
				for (Destination d : destinations) {
					if (d.getDestinationId() == p.getDestinationId()) {
						locationSelection.setText(d.getName());
					}
				}

				locationsList = (ListView) findViewById(R.id.dest_list);
				locationsList.setVisibility(View.GONE);
				adapter = new DestListAdapter();
				locationsList.setAdapter(adapter);
				rootView.bringChildToFront(locationsList);
				locationsList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						selectedPosition = position;
						locationSelection.setText(destinations.get(
								selectedPosition).getName());
						closeList();
					}
				});
			}
		});
	}

	private class DestListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (!destListShown) {
				return 0;
			}
			return destinations.size();
		}

		@Override
		public Object getItem(int position) {
			return destinations.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RelativeLayout ll = (RelativeLayout) convertView;
			if (convertView == null) {
				ll = (RelativeLayout) LayoutInflater.from(parent.getContext())
						.inflate(R.layout.dest_list_item, null);
			}
			boolean handleVisible = position == 0;
			ImageView close = (ImageView) ll.findViewById(R.id.close_btn);
			close.setVisibility(handleVisible ? View.VISIBLE : View.INVISIBLE);
			close.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					closeList();
				}
			});

			TextView txt = (TextView) ll.findViewById(R.id.txt);
			UIUtils.prepareTextView(parent.getContext(), txt);
			txt.setText(destinations.get(position).getName());

			return ll;
		}

	}

	private void closeList() {
		if (destListShown) {
			destListShown = false;
			adapter.notifyDataSetChanged();
			locationsList.setVisibility(View.GONE);
			findViewById(R.id.scroll_assistant).setVisibility(View.GONE);
			scrollView.setScrollingEnabled(true);
		}
	}

	private void updatePerson() {
		App.getTaskManager().assignNet(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				String emailStr = email.getText().toString();
				if (emailStr.length() > 0 && isValidEmail(emailStr)) {
					currentPerson.setEmail(emailStr);
				} else {
					return R.string.my_profile_fill_email;
				}
				String nicknameStr = nickname.getText().toString();
				if (nicknameStr.length() > 0) {
					currentPerson.setNickname(nicknameStr);
				} else {
					return R.string.my_profile_fill_nickname;
				}
				String commentsStr = comments.getText().toString();
				if(commentsStr == null) {
					commentsStr = "";
				}
				currentPerson.setComments(commentsStr);
				String addressStr = address.getText().toString();
				if (addressStr.length() > 0) {
					currentPerson.setAddress(addressStr);
				} else {
					return R.string.my_profile_fill_address;
				}
				currentPerson.setIsAddressVisible(showAddress);
				String location = locationSelection.getText().toString();
				for (Destination d : destinations) {
					if (d.getName().equals(location)) {
						currentPerson.setDestinationId(d.getDestinationId());
					}
				}
				String resp = ApiServices.updatePerson(App.app().getAuth()
						.getToken(), currentPerson);
				Log.d(TAG, "update person response:" + resp);
				return 0;
			}
		}, new UICallback() {
			@Override
			public void onResult(Object result) {
				int resId = (Integer) result;
				if (resId != 0) {
					saveChanges.setEnabled(true);
					showMandatoryFieldWarning(resId);
				} else {
					finish();
					overridePendingTransition(R.anim.slide_out_animation,
							R.anim.slide_out_animation);
				}
			}
		});
	}
	
	public final boolean isValidEmail(CharSequence target) {
		if (TextUtils.isEmpty(target)) {
			return false;
		} else {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target)
					.matches();
		}
	}

	private void showMandatoryFieldWarning(final int resId) {
		NotificationDialog dialog = new NotificationDialog(
				MyProfileActivity.this);
		dialog.setMessage(resId);
		dialog.show();
	}

	public void upload(String url, Bitmap bitmap) throws Exception {
		Log.d(TAG, "upload " + bitmap + " to " + url + " with token "
				+ App.app().getAuth().getToken());
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		post.setHeader("Authorization", App.app().getAuth().getToken());
		post.setHeader("Content-Type",
				"multipart/form-data; boundary=----NextBoundaryxsg77blN4ea8Kcgv");
		MultipartEntity mpEntity = new MultipartEntity();
		// Add the data to the multipart entity
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
		byte[] byteArray = stream.toByteArray();
		mpEntity.addPart("file", new ByteArrayBody(byteArray, "file.jpg"));
		mpEntity.addPart("name",
				new StringBody("file.jpg", Charset.forName("UTF-8")));
		post.setEntity(mpEntity);

		Log.d(TAG, "request prepared");
		// Execute the post request
		HttpResponse response1 = client.execute(post);
		Log.d(TAG, "execute done " + response1.getStatusLine());
		// Get the response from the server
		HttpEntity resEntity = response1.getEntity();
		String Response = EntityUtils.toString(resEntity);
		Log.d(TAG, Response);
		client.getConnectionManager().shutdown();
	}

	// Multipart upload
	public String multipartRequest(String urlTo, String post, Bitmap bitmap,
			String filefield) throws ParseException, IOException {
		//urlTo = "http://scooterlabs.com/echo";
		Log.d(TAG, "upload to:" + urlTo + " with token " + App.app().getAuth()
				.getToken());
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
		InputStream inputStream = null;

		String twoHyphens = "--";
		String boundary = "----NextBoundaryxsg77blN4ea8Kcgv";
		String lineEnd = "\r\n";

		String result = "";

		try {
			URL url = new URL(urlTo);
			connection = (HttpURLConnection) url.openConnection();

//			connection.setRequestProperty("Content-Type",
//					"multipart/form-data; boundary=" + boundary);
			connection.setRequestProperty("User-Agent",
					"Next2You/1.0 (iPhone Simulator; iOS 7.1; Scale/2.00)");
			connection.setRequestProperty("Authorization", App.app().getAuth()
					.getToken());
			connection.setRequestMethod("POST");
			// Map<String, List<String>> headers = connection.getHeaderFields();
			// for (String k : headers.keySet()) {
			// Log.d(TAG, "name=" + k + " value=" + headers.get(k));
			// }

			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);

			StringBuilder sb = new StringBuilder();
			sb.append(twoHyphens + boundary + lineEnd + "Content-Disposition: form-data; name=\""
					+ filefield + "\"; filename=\"file.jpg\"" + lineEnd + "Content-Type:image/jpeg" + lineEnd
					+ lineEnd);
			outputStream = new DataOutputStream(connection.getOutputStream());
			outputStream.writeBytes(sb.toString());
			// outputStream.writeBytes("Content-Transfer-Encoding: binary"
			// + lineEnd);

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
			byte[] byteArray = new byte[0];//stream.toByteArray();
			outputStream.write(byteArray);

			outputStream.writeBytes(lineEnd);

			outputStream.writeBytes(twoHyphens + boundary + twoHyphens
					+ lineEnd);
			outputStream.flush();
			outputStream.close();

			int responsecode = 0;
			try {
				// Will throw IOException if server responds with 401.
				responsecode = connection.getResponseCode();
			} catch (IOException e) {
				// Will return 401, because now connection has the correct
				responsecode = connection.getResponseCode();
			}
			Log.d(TAG, "responsecode=" + responsecode);

			InputStream err = connection.getErrorStream();
			if(err != null) {
				byte[] buff = new byte[100];
				int n;
				sb = new StringBuilder();
				while ((n = err.read(buff)) != -1) {
					sb.append(new String(buff, 0, n));
				}
				if (sb.length() > 0) {
					Log.d(TAG, "ERROR PIC UPLOAD:" + sb);
				}
			}

			inputStream = connection.getInputStream();
			result = this.convertStreamToString(inputStream);

			inputStream.close();
			return result;
		} catch (Exception e) {
			Log.e(TAG, "Multipart Form Upload Error", e);
		}
		return "";
	}

	private String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	// see http://www.androidsnippets.com/multipart-http-requests
}
