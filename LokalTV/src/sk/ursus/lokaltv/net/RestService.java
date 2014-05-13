package sk.ursus.lokaltv.net;

import sk.ursus.lokaltv.net.lib.AbstractRestService;
import sk.ursus.lokaltv.net.lib.Callback;
import sk.ursus.lokaltv.net.lib.ParamBuilder;
import sk.ursus.lokaltv.net.lib.RequestBuilder;
import sk.ursus.lokaltv.net.lib.RestUtils.Methods;
import sk.ursus.lokaltv.net.processor.NewsFeedProcessor;
import sk.ursus.lokaltv.net.processor.RelatedVideoProcessor;
import sk.ursus.lokaltv.net.processor.FeedProcessor;
import android.content.Context;

public class RestService extends AbstractRestService {

	public static final String BASE_URL = "http://www.lokaltv.sk";

	public RestService() {
		super(RestService.class.toString());
	}

	public static void getNewsFeed(Context context, Callback callback) {
		new RequestBuilder()
				.setMethod(Methods.GET)
				.setUrl(BASE_URL)
				.setCallback(callback)
				.setProcessor(new NewsFeedProcessor())
				.execute(context, RestService.class);
	}

	public static void getFeed(Context context, String cathegory, int page, Callback callback) {
		boolean isFromLoadMore = (page > 1);

		String params = new ParamBuilder()
				.addParam("no", String.valueOf(page))
				.build();

		new RequestBuilder()
				.setMethod(Methods.GET)
				.setUrl(BASE_URL + "/" + cathegory)
				.setParams(params)
				.setCallback(callback)
				.setProcessor(new FeedProcessor(isFromLoadMore))
				.execute(context, RestService.class);
	}

	public static void getRelatedVideo(Context context, String url, Callback callback) {
		new RequestBuilder()
				.setMethod(Methods.GET)
				.setUrl(BASE_URL + url)
				.setCallback(callback)
				.setProcessor(new RelatedVideoProcessor(BASE_URL + url))
				.execute(context, RestService.class);
	}
}
