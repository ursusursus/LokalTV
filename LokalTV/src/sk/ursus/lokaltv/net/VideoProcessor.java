package sk.ursus.lokaltv.net;

import java.io.InputStream;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.os.Bundle;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.net.ServerUtils.Processor;
import sk.ursus.lokaltv.util.Utils;

public class VideoProcessor extends Processor {

	private static final long serialVersionUID = 1L;
	private boolean mFromLoadMore = false;
	
	public VideoProcessor(boolean isFromLoadMore) {
		mFromLoadMore = isFromLoadMore;
	}

	@Override
	public void onProcessResponse(Context context, String contentType, InputStream stream, Bundle results) throws Exception {
		Document document = Jsoup.parse(ServerUtils.inputStreamToString(stream));
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
		results.putParcelableArrayList("feed", feedItems);
		
		// Post from_load_more flag
		results.putBoolean("from_load_more", mFromLoadMore);
	}

}
