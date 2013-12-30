package sk.ursus.lokaltv.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.net.ServerUtils.Processor;
import sk.ursus.lokaltv.util.LOG;
import sk.ursus.lokaltv.util.Utils;
import android.content.Context;
import android.os.Bundle;

public class FeedProcessor extends Processor {

	private static final long serialVersionUID = 1L;

	@Override
	public void onProcessResponse(Context context, String contentType, InputStream stream, Bundle results) throws Exception {
		Document document = Jsoup.parse(ServerUtils.inputStreamToString(stream));
		ArrayList<Video> feedItems = new ArrayList<Video>();

		// Get list container
		Element list = document.getElementById("slider-home-v2");
		// Extract anchors
		Elements items = list.getElementsByTag("a");

		for (Element element : items) {
			String itemUrl = element.attr("href");

			/* Element img = element.children().get(0);
			String imageUrl = img.attr("src");
			String itemTitle = img.attr("alt"); */

			Video feedItem = Utils.parseDetail(itemUrl);
			if (feedItem != null) {
				feedItems.add(feedItem);
			}
			// LOG.d("////////////////\nItemTitle: " + itemTitle + "\nItemUrl: " + itemUrl + "\nImageUrl: " + imageUrl);
		}

		// Post results
		results.putParcelableArrayList("feed", feedItems);
	}
}
