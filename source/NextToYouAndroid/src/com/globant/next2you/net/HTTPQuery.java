package com.globant.next2you.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

public class HTTPQuery {
	@SuppressWarnings("unused")
	private static final String TAG = "HTTPQuery";
	private String url;
	private ArrayList<NameValuePair> qData;
	private String contentType;
	private AbstractHttpEntity entity;
	public String basicAuth;
	private static DefaultHttpClient httpclient = null;
	public HttpResponse response = null;

	// public Callback handleResponseStream;

	public HTTPQuery(String url) {
		this.url = url;
		qData = new ArrayList<NameValuePair>(2);
		if (httpclient == null) {
			httpclient = setupHttpClient();
		}
	}

	public void addParam(String k, String v) {
		qData.add(new BasicNameValuePair(k, v));
	}

	public void setAuthorizationToken(String token) {
		basicAuth = token;
	}

	public void setUserAgent(String userAgent) {
		httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
				userAgent);
	}

	public String send(String method) throws ClientProtocolException,
			IOException {
		method = method.toUpperCase(Locale.US);
		InputStream is = null;
		HttpUriRequest httpUriRequest = null;
		try {

			if (method.equals("GET")) {
				String paramString = URLEncodedUtils.format(qData, "utf-8");
				String req = url + "?" + paramString;
				Log.d(TAG, "get request:" + req);
				httpUriRequest = new HttpGet(req);
			} else if (method.equals("POST")) {
				HttpPost post = new HttpPost(url);
				httpUriRequest = post;
				post.setEntity(entity != null ? entity
						: new UrlEncodedFormEntity(qData));
			} else if (method.equals("PUT")) {
				HttpPut put = new HttpPut(url);
				httpUriRequest = put;
				put.setEntity(entity != null ? entity
						: new UrlEncodedFormEntity(qData));
			} else if (method.equals("DELETE")) {
				httpUriRequest = new HttpDelete(url);
			} else if (method.equals("HEAD")) {
				httpUriRequest = new HttpHead(url);
			}
			if (getContentType() != null && getContentType().length() > 0) {
				httpUriRequest.setHeader("Content-Type", getContentType());
			}
			if (basicAuth != null && basicAuth.length() > 0) {
				// Log.d(TAG, "use basic auth:" + basicAuth);
				httpUriRequest.setHeader("Authorization", basicAuth);
			}
			response = httpclient.execute(httpUriRequest);
			HttpEntity respEnt = response.getEntity();
			if (respEnt != null) {
				is = respEnt.getContent();
				// if(handleResponseStream != null) {
				// handleResponseStream.onResult(is);
				// return "";
				// }
				return inputStreamToString(is);
			}
			return null;
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Throwable e) {
			}
			try {
				if (response != null && response.getEntity() != null) {
					response.getEntity().consumeContent();
				}
			} catch (Throwable e) {
			}
			try {
				if (httpUriRequest != null) {
					httpUriRequest.abort();
				}
			} catch (Exception e) {
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static DefaultHttpClient setupHttpClient() {
		DefaultHttpClient httpclient = getNewHttpClient();

		HttpProtocolParams.setUseExpectContinue(httpclient.getParams(), true);
		HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {

			public boolean retryRequest(IOException exception,
					int executionCount, HttpContext context) {
				// retry a max of 5 times
				if (executionCount >= 5) {
					return false;
				}
				if (exception instanceof NoHttpResponseException) {
					return true;
				} else if (exception instanceof ClientProtocolException) {
					return true;
				}
				return false;
			}
		};
		httpclient.setHttpRequestRetryHandler(retryHandler);
		String proxyHost = android.net.Proxy.getDefaultHost();
		int proxyPort = android.net.Proxy.getDefaultPort();
		// Set Proxy params of client, if they are not the standard
		if (proxyHost != null && proxyPort > 0) {
			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					proxy);
		}
		return httpclient;
	}

	public static DefaultHttpClient getNewHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	public static class MySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public MySSLSocketFactory(KeyStore truststore)
				throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,
					port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}

	private String inputStreamToString(InputStream is) throws IOException {
		StringBuffer s = new StringBuffer();
		String line = "";
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		while ((line = rd.readLine()) != null) {
			s.append(line);
		}
		return s.toString();
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public AbstractHttpEntity getEntity() {
		return entity;
	}

	public void setEntity(AbstractHttpEntity entity) {
		this.entity = entity;
	}

	public int getResponseStatusCode() {
		if (response != null) {
			return response.getStatusLine().getStatusCode();
		}
		return -1;
	}

}
