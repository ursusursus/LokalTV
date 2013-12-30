package sk.ursus.lokaltv.net;

import sk.ursus.lokaltv.net.ServerUtils.AbstractRestService;
import sk.ursus.lokaltv.net.ServerUtils.Callback;
import sk.ursus.lokaltv.net.ServerUtils.Methods;
import sk.ursus.lokaltv.net.ServerUtils.ParamBuilder;
import sk.ursus.lokaltv.net.ServerUtils.RequestBuilder;
import android.content.Context;
import android.location.Address;

public class RestService extends AbstractRestService {

	private static final String BASE_URL = "http://www.lokaltv.sk/";

	public RestService() {
		super(RestService.class.toString());
	}

	public static void getFeed(Context context, Callback callback) {
		new RequestBuilder()
				.setMethod(Methods.GET)
				.setUrl(BASE_URL)
				.setCallback(callback)
				.setProcessor(new FeedProcessor())
				.execute(context, RestService.class);
	}

	public static void getVideos(Context context, String cathegory, int page, Callback callback) {
		boolean isFromLoadMore = (page != 1);
		
		String params = new ServerUtils.ParamBuilder()
				.addParam("no", String.valueOf(page))
				.build();
		
		new RequestBuilder()
				.setMethod(Methods.GET)
				.setUrl(BASE_URL + cathegory)
				.setParams(params)
				.setCallback(callback)
				.setProcessor(new VideoProcessor(isFromLoadMore))
				.execute(context, RestService.class);
	}
}
