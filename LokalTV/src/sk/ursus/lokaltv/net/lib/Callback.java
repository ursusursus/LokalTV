package sk.ursus.lokaltv.net.lib;

import sk.ursus.lokaltv.net.lib.RestUtils.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;

public abstract class Callback extends ResultReceiver {

	public Callback() {
		super(putHandlerIfNeccessary());
	}

	private static Handler putHandlerIfNeccessary() {
		if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
			return new Handler();
		}
		return null;
	}

	public int getId() {
		return 0;
	}

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {
		super.onReceiveResult(resultCode, resultData);
		switch (resultCode) {
			case Status.STARTED: {
				onStarted();
				break;
			}
			case Status.OK: {
				onSuccess(resultData);
				break;
			}
			case Status.ERROR: {
				int errorCode = resultData.getInt(RestUtils.ERROR_CODE);
				String errorMessage = resultData.getString(RestUtils.ERROR_MESSAGE);
				onError(errorCode, errorMessage);
				break;
			}
			case Status.EXCEPTION: {
				onException();
				break;
			}
			case Status.PROGRESS: {
				int progress = resultData.getInt(RestUtils.RESULT_PROGRESS);
				int total = resultData.getInt(RestUtils.RESULT_TOTAL);
				onProgress(progress, total);
				break;
			}
			case Status.CANCELED: {
				onCanceled();
				break;
			}
		}
	}

	public abstract void onSuccess(Bundle data);
	public void onError(int code, String message) {}
	public abstract void onException();
	public void onStarted() {}
	public void onProgress(int bytesProgress, int bytesTotal) {}
	public void onCanceled() {}

}
