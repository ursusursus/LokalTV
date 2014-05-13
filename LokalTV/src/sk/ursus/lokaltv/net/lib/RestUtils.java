package sk.ursus.lokaltv.net.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;


import android.content.Context;
import android.os.Bundle;

/**
 * 
 * @author ursus
 * 
 */
public class RestUtils {

	/**
	 * 
	 */
	private static final int TIMEOUT_MS = 5000;
	public static final String ERROR_CODE = "error_code";
	public static final String ERROR_MESSAGE = "error_message";
	public static final String RESULT_PROGRESS = "result_progress";
	public static final String RESULT_TOTAL = "result_total";

	public static class Status {

		public static final int OK = 0;
		public static final int EXCEPTION = 1;
		public static final int ERROR = 2;
		public static final int STARTED = 3;
		public static final int CANCELED = 4;
		public static final int PROGRESS = 5;
	}

	public static class Methods {

		public static final int GET = 0;
		public static final int POST = 1;
		public static final int GET_WITH_SSL = 2;
		public static final int POST_WITH_SSL = 3;
	}

	public static class ErrorCodes {

		/**
		 * 
		 */
		public static final int NOT_LOGGED_IN = -32001;
	}

	public static int post(Context context, String endpoint, String params, Processor processor, Bundle results) throws Exception {
		URL url;
		try {
			url = new URL(endpoint);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid url: " + endpoint);
		}

		int resultCode = Status.OK;
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(TIMEOUT_MS);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

			if (params != null) {
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Length", Integer.toString(params.length()));

				// Post the request
				OutputStream out = conn.getOutputStream();
				out.write(params.getBytes());
				out.close();
			}

			// Handle response
			if (processor != null) {
				resultCode = processor.onProcessResponse(context, conn, results);
			}

		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		return resultCode;

	}

	public static int postWithSsl(Context context, String endpoint, String params, Processor processor, Bundle results) throws Exception {
		URL url;
		try {
			url = new URL(endpoint);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid url: " + endpoint);
		}

		int resultCode = Status.OK;
		HttpsURLConnection conn = null;
		try {
			// SSL stuff
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new TrustManager[] { new DummyTrustedManager() }, null);
			SSLSocketFactory sslFactory = sslContext.getSocketFactory();

			HttpsURLConnection.setDefaultHostnameVerifier(new DummyHostnameVerifier());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslFactory);

			// Make connection
			conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(TIMEOUT_MS);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

			if (params != null) {
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Length", Integer.toString(params.length()));

				// Post the request
				OutputStream out = conn.getOutputStream();
				out.write(params.getBytes());
				out.close();
			}

			// Handle response
			if (processor != null) {
				resultCode = processor.onProcessResponse(context, conn, results);
			}

		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		return resultCode;

	}

	public static int get(Context context, String endpoint, String params, Processor processor, Bundle results) throws Exception {
		URL url;
		try {
			url = params != null ? new URL(endpoint + "?" + params) : new URL(endpoint);

		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid url: " + endpoint);
		}

		int resultCode = Status.OK;
		HttpURLConnection conn = null;
		try {
			// Make connection
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(TIMEOUT_MS);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

			// Handle response
			if (processor != null) {
				resultCode = processor.onProcessResponse(context, conn, results);
			}

		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return resultCode;
	}

	public static int getWithSsl(Context context, String endpoint, String params, Processor processor, Bundle results) throws Exception {
		URL url;
		try {
			url = params != null ? new URL(endpoint + "?" + params) : new URL(endpoint);

		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid url: " + endpoint);
		}

		int resultCode = Status.OK;
		HttpsURLConnection conn = null;
		try {
			// SSL stuff
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new TrustManager[] { new DummyTrustedManager() }, null);
			SSLSocketFactory sslFactory = sslContext.getSocketFactory();

			HttpsURLConnection.setDefaultHostnameVerifier(new DummyHostnameVerifier());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslFactory);

			// Make connection
			conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(TIMEOUT_MS);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

			// Handle response
			if (processor != null) {
				resultCode = processor.onProcessResponse(context, conn, results);
			}

		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return resultCode;
	}

	public static int execute(Context c, int method, String url, String params, Processor processor, Bundle results) throws Exception {
		switch (method) {
			case Methods.GET:
				return get(c, url, params, processor, results);

			case Methods.GET_WITH_SSL:
				return getWithSsl(c, url, params, processor, results);

			case Methods.POST:
				return post(c, url, params, processor, results);

			case Methods.POST_WITH_SSL:
				return postWithSsl(c, url, params, processor, results);

			default:
				return -1;
		}
	}

	public static String inputStreamToString(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder stringBuilder = new StringBuilder();

		String responseLine;
		while ((responseLine = bufferedReader.readLine()) != null) {
			stringBuilder.append(responseLine);
		}

		return stringBuilder.toString();
	}

	public static boolean isTokenValid(String token) {
		if (token != null) {
			boolean isValid = true;
			if (isValid) {
				// Tu potom dorobit aj tie timestamp pre valid?
			}
			return true;
		} else {
			return false;
		}
	}

}
