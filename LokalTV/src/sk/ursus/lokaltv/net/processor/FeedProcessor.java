package sk.ursus.lokaltv.net.processor;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.net.RestService;
import sk.ursus.lokaltv.net.lib.StringProcessor;
import sk.ursus.lokaltv.net.lib.RestUtils.Status;
import sk.ursus.lokaltv.util.Utils;
import android.content.Context;
import android.os.Bundle;

public class FeedProcessor extends StringProcessor {

	public static final String RESULT_FROM_LOAD_MORE = "from_load_more";
	public static final String RESULT_VIDEOS = "feed";
	
	private boolean mFromLoadMore = false;

	public FeedProcessor(boolean isFromLoadMore) {
		mFromLoadMore = isFromLoadMore;
	}

	@Override
	public int onProcessResponse(Context context, String response, Bundle results) throws Exception {
		Document document = Jsoup.parse(response);
		ArrayList<Video> feedItems = new ArrayList<Video>();

		Elements videoItems = document.getElementsByClass("video-item");

		for (Element element : videoItems) {
			Element a = element.getElementsByTag("a").get(1);
			String url = RestService.BASE_URL + a.attr("href");

			Video feedItem = Utils.parseDetail(url);
			if (feedItem != null) {
				feedItems.add(feedItem);
			}
		}

		// Post results
		results.putParcelableArrayList(RESULT_VIDEOS, feedItems);
		// Post from_load_more flag
		results.putBoolean(RESULT_FROM_LOAD_MORE, mFromLoadMore);

		return Status.OK;
	}

}
