package sk.ursus.lokaltv.net.lib;

import sk.ursus.lokaltv.net.lib.RestUtils.Methods;
import sk.ursus.lokaltv.net.lib.RestUtils.Status;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.awaboom.ursus.agave.LOG;

public class AbstractRestService extends IntentService {

	public static final String ACTION = "com.awaboom.ursus.agave.AbstractRestService";
	
	public static final String EXTRA_PARAMS = "params";
	public static final String EXTRA_CALLBACK = "receiver";
	public static final String EXTRA_METHOD = "method";
	public static final String EXTRA_URL = "url";
	public static final String EXTRA_PROCESSOR = "processor";
	
	private Processor mProcessor;
	private boolean mCancel = true;

	public AbstractRestService(String name) {
		super(name);
	}

	/**
	 * Background
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		if (!ACTION.equals(action)) {
			return;
		}

		ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra(EXTRA_CALLBACK);
		mProcessor = (Processor) intent.getSerializableExtra(EXTRA_PROCESSOR);

		if(receiver != null && mProcessor != null) {
			mProcessor.setReceiver(receiver);
		}

		int method = intent.getIntExtra(EXTRA_METHOD, Methods.GET);
		String url = intent.getStringExtra(EXTRA_URL);
		String params = intent.getStringExtra(EXTRA_PARAMS);

		if (receiver != null) {
			receiver.send(Status.STARTED, Bundle.EMPTY);
		}

		try {
			Bundle result = new Bundle();
			//
			int resultCode = RestUtils.execute(this, method, url, params, mProcessor, result);
			//
			if (receiver != null) {
				receiver.send(resultCode, result);
			}

		} catch (Exception e) {
			LOG.e(e);

			if (receiver != null) {
				receiver.send(Status.EXCEPTION, Bundle.EMPTY);
			}
		}
		
		mCancel = false;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mCancel) {
			mProcessor.cancel();			
		}
	}

}
