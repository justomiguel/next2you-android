package com.globant.next2you;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
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
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.globant.next2you.async.UICallback;
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

		TextView myProfileTitle = (TextView) findViewById(R.id.my_profile_title);
		TextView changePhoto = (TextView) findViewById(R.id.change_photo_btn);
		TextView showExactAddr = (TextView) findViewById(R.id.show_exact_addr);
		nickname = (EditText) findViewById(R.id.nickname);
		email = (EditText) findViewById(R.id.email);
		locationSelection = (EditText) findViewById(R.id.location);
		scrollView = (LockableScrollView) findViewById(R.id.scroll_view);
		rootView = (RelativeLayout) findViewById(R.id.rootview);

		TextView useLastPhoto = (TextView) findViewById(R.id.use_last_photo);
		TextView takeNewPhoto = (TextView) findViewById(R.id.take_new_photo);
		TextView selectFromLibrary = (TextView) findViewById(R.id.select_from_library);
		TextView cancel = (TextView) findViewById(R.id.cancel_photo);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				View view = findViewById(R.id.add_photo_holder);
				view.setVisibility(View.GONE);
				scrollView.setScrollingEnabled(true);
			}
		});
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
						scrollView.setScrollingEnabled(false);
					}
				});

		locationSelection.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "show locations");
				if (destinations.size() > 0) {
					locationsList.setVisibility(View.VISIBLE);
					destListShown = true;
					adapter.notifyDataSetChanged();

					int height = getWindow().getDecorView().getHeight()
							- (int) getResources().getDimension(
									R.dimen.login_screen_header_height);
					RelativeLayout.LayoutParams params = (LayoutParams) locationsList
							.getLayoutParams();
					params.height = height;
					locationsList.setLayoutParams(params);
					scrollView.scrollTo(0, 0);
					scrollView.setScrollingEnabled(false);
				}
			}
		});

		address = (EditText) findViewById(R.id.address);
		TextView sampleAddress = (TextView) findViewById(R.id.example_address);
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

		UIUtils.prepareTextView(this, cancel);
		UIUtils.prepareTextView(this, useLastPhoto);
		UIUtils.prepareTextView(this, takeNewPhoto);
		UIUtils.prepareTextView(this, selectFromLibrary);
		UIUtils.prepareTextView(this, showExactAddr);
		UIUtils.prepareTextView(this, saveChanges);
		UIUtils.prepareTextView(this, comments);
		UIUtils.prepareTextView(this, mendatoryFields);
		UIUtils.prepareTextView(this, address);
		UIUtils.prepareTextView(this, email);
		UIUtils.prepareTextView(this, sampleAddress);
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

		findViewById(R.id.go_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ImageView avatarPic = (ImageView) findViewById(R.id.avatar);
		if (lastPhoto == null) {
			lastPhoto = ((BitmapDrawable) avatarPic.getDrawable()).getBitmap();
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
						String url = String.format(Locale.US,
								"%s%s", ApiServices.API_URL, "images");
						upload(url, bitmapF);
//						String res = multipartRequest(url , "",
//								bitmapF, "file");
//						Log.d(TAG, "bitmap upload result:" + res);
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
		destListShown = false;
		adapter.notifyDataSetChanged();
		locationsList.setVisibility(View.GONE);
		scrollView.setScrollingEnabled(true);
	}

	private void updatePerson() {
		App.getTaskManager().assignNet(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				String emailStr = email.getText().toString();
				if (emailStr.length() > 0) {
					currentPerson.setEmail(emailStr);
				}
				String nicknameStr = nickname.getText().toString();
				if (nicknameStr.length() > 0) {
					currentPerson.setNickname(nicknameStr);
				}
				String commentsStr = comments.getText().toString();
				if (commentsStr.length() > 0) {
					currentPerson.setComments(commentsStr);
				}
				String addressStr = address.getText().toString();
				if (addressStr.length() > 0) {
					currentPerson.setAddress(addressStr);
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
				return null;
			}
		}, new UICallback() {
			@Override
			public void onResult(Object result) {
				finish();
			}
		});
	}

	public void upload(String url, Bitmap bitmap) throws Exception {
		Log.d(TAG, "upload " + bitmap + " to " + url + " with token " + App.app().getAuth().getToken());
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		post.setHeader("Authorization", App.app().getAuth().getToken());
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
		Log.d(TAG, "upload to:" + urlTo);
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

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + boundary);
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 ( compatible ) ");
			connection.setRequestProperty("Accept", "*/*");
			connection.addRequestProperty("Authorization", App.app().getAuth()
					.getToken());
			Map<String, List<String>> headers = connection.getHeaderFields();
			for (String k : headers.keySet()) {
				Log.d(TAG, "name=" + k + " value=" + headers.get(k));
			}

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			outputStream = new DataOutputStream(connection.getOutputStream());
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\""
					+ filefield + "\"; filename=\"file.jpg\"" + lineEnd);
			outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);
			outputStream.writeBytes("Content-Transfer-Encoding: binary"
					+ lineEnd);
			outputStream.writeBytes(lineEnd);

			// ByteArrayOutputStream stream = new ByteArrayOutputStream();
			// bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
			byte[] byteArray = new byte[1];// stream.toByteArray();
			outputStream.write(byteArray);

			outputStream.writeBytes(lineEnd);

			// // Upload POST Data
			// String[] posts = post.split("&");
			// int max = posts.length;
			// for (int i = 0; i < max; i++) {
			// outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			// String[] kv = posts[i].split("=");
			// outputStream
			// .writeBytes("Content-Disposition: form-data; name=\""
			// + kv[0] + "\"" + lineEnd);
			// outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
			// outputStream.writeBytes(lineEnd);
			// outputStream.writeBytes(kv[1]);
			// outputStream.writeBytes(lineEnd);
			// }

			outputStream.writeBytes(twoHyphens + boundary + twoHyphens
					+ lineEnd);

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
			byte[] buff = new byte[100];
			int n;
			StringBuilder sb = new StringBuilder();
			while ((n = err.read(buff)) != -1) {
				sb.append(new String(buff, 0, n));
			}
			if (sb.length() > 0) {
				Log.d(TAG, "ERROR PIC UPLOAD:" + sb);
			}

			inputStream = connection.getInputStream();
			result = this.convertStreamToString(inputStream);

			inputStream.close();
			outputStream.flush();
			outputStream.close();
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
