package sk.ursus.lokaltv.net.processor;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.net.lib.StringProcessor;
import sk.ursus.lokaltv.net.lib.RestUtils.Status;
import sk.ursus.lokaltv.util.Utils;
import android.content.Context;
import android.os.Bundle;

public class NewsFeedProcessor extends StringProcessor {

	public static final String RESULT_VIDEOS = "videos";

	@Override
	public int onProcessResponse(Context context, String response, Bundle results) throws Exception {
		Document document = Jsoup.parse(response);
		ArrayList<Video> videos = new ArrayList<Video>();

		// Get list container
		Element list = document.getElementById("slider-home-v2");
		// Extract anchors
		Elements items = list.getElementsByTag("a");

		for (Element element : items) {
			String itemUrl = element.attr("href");

			/* Element img = element.children().get(0);
			String imageUrl = img.attr("src");
			String itemTitle = img.attr("alt"); */

			Video video = Utils.parseDetail(itemUrl);
			if (video != null) {
				videos.add(video);
				// LOG.dumpPojo(feedItem);
			}
		}

		// Post results
		results.putParcelableArrayList(RESULT_VIDEOS, videos);
		return Status.OK;
	}
}
