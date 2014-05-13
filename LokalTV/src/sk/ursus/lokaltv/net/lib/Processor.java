package sk.ursus.lokaltv.net.lib;

import java.io.Serializable;
import java.net.URLConnection;

import sk.ursus.lokaltv.net.lib.RestUtils.Status;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;

public abstract class Processor implements Serializable {

	private boolean mCanceled;
	private ResultReceiver mReceiver;
	private Bundle mBundle;
	
	public void cancel() {
		mCanceled = true;
	}
	
	protected boolean isCanceled() {
		return mCanceled;
	}
	
	public void setReceiver(ResultReceiver receiver) {
		mReceiver = receiver;
		mBundle = new Bundle();
	}
	
	public void notifyProgress(int bytesProgress, int bytesTotal) {
		mBundle.putInt(RestUtils.RESULT_PROGRESS, bytesProgress);
		mBundle.putInt(RestUtils.RESULT_TOTAL, bytesTotal);
		mReceiver.send(Status.PROGRESS, mBundle);
	}
	
	public abstract int onProcessResponse(Context context, URLConnection connection, Bundle results) throws Exception;

}
