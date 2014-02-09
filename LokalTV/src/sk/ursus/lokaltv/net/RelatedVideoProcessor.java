package sk.ursus.lokaltv.net;

import java.io.InputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.net.ServerUtils.Processor;
import sk.ursus.lokaltv.util.Utils;
import android.content.Context;
import android.os.Bundle;

public class RelatedVideoProcessor extends Processor {

	public static final String RESULT_VIDEO = "result_video";
	private static final long serialVersionUID = 1L;

	@Override
	public void onProcessResponse(Context context, String contentType, InputStream stream, Bundle results)
			throws Exception {
		Document document = Jsoup.parse(ServerUtils.inputStreamToString(stream));
		Video video = Utils.parseDetail(document);

		// Post results
		results.putParcelable(RESULT_VIDEO, video);
	}
}
