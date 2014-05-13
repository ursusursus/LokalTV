package sk.ursus.lokaltv.net.lib;

import android.content.Context;
import android.content.Intent;

import com.awaboom.ursus.agave.LOG;

public class RequestBuilder {
	private int mMethod;
	private String mUrl;
	private String mParams;
	private Processor mProcessor;
	private Callback mCallback;

	public RequestBuilder setMethod(int method) {
		mMethod = method;
		return this;
	}

	public RequestBuilder setUrl(String url) {
		mUrl = url;
		return this;
	}

	public RequestBuilder setParams(String params) {
		mParams = params;
		return this;
	}

	public RequestBuilder setProcessor(Processor processor) {
		mProcessor = processor;
		return this;
	}

	public RequestBuilder setCallback(Callback callback) {
		mCallback = callback;
		return this;
	}

	public void execute(Context context, Class<? extends AbstractRestService> serviceClass) {
		if (mUrl == null) {
			LOG.e("Url is missing!");
			return;
		}

		Intent intent = new Intent(context, serviceClass)
				.setAction(AbstractRestService.ACTION)
				.putExtra(AbstractRestService.EXTRA_METHOD, mMethod)
				.putExtra(AbstractRestService.EXTRA_URL, mUrl)
				.putExtra(AbstractRestService.EXTRA_PARAMS, mParams)
				.putExtra(AbstractRestService.EXTRA_PROCESSOR, mProcessor)
				.putExtra(AbstractRestService.EXTRA_CALLBACK, mCallback);

		context.startService(intent);
	}
}
