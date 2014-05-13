package sk.ursus.lokaltv.net.lib;

import java.io.InputStream;
import java.net.URLConnection;


import android.content.Context;
import android.os.Bundle;

public abstract class StringProcessor extends Processor {

	@Override
	public int onProcessResponse(Context context, URLConnection connection, Bundle results) throws Exception {
		InputStream is = connection.getInputStream();
		String response = RestUtils.inputStreamToString(is);
		return onProcessResponse(context, response, results);
	}

	public abstract int onProcessResponse(Context context, String response, Bundle results) throws Exception;

}
