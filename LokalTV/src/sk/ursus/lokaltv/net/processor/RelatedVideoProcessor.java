package sk.ursus.lokaltv.net.processor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.net.lib.StringProcessor;
import sk.ursus.lokaltv.net.lib.RestUtils.Status;
import sk.ursus.lokaltv.util.Utils;
import android.content.Context;
import android.os.Bundle;

public class RelatedVideoProcessor extends StringProcessor {

	public static final String RESULT_VIDEO = "result_video";
	private static final long serialVersionUID = 1L;
	private String mUrl;

	public RelatedVideoProcessor(String url) {
		mUrl = url;
	}

	@Override
	public int onProcessResponse(Context context, String response, Bundle results) throws Exception {
		Document document = Jsoup.parse(response);
		Video video = Utils.parseDetail(document, mUrl);

		// Post results
		results.putParcelable(RESULT_VIDEO, video);
		return Status.OK;
	}
}
