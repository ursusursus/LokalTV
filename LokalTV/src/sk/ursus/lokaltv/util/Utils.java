package sk.ursus.lokaltv.util;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.model.Cathegory;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.model.RelatedVideo;
import sk.ursus.lokaltv.net.RestService;
import android.text.format.DateUtils;
import android.util.SparseArray;
import android.widget.TextView;

public class Utils {

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
	public static final float PRESUMED_VIDEO_WIDTH = 640F;
	public static final float PRESUMED_VIDEO_HEIGHT = 360F;

	public static final SparseArray<Cathegory> CATHEGORIES = new SparseArray<Cathegory>();
	static {
		CATHEGORIES.put(R.id.serialEpisodesButton, new Cathegory("Epizódy", "epizody"));
		CATHEGORIES.put(R.id.serialSceensButton, new Cathegory("Vystrihnuté scény", "vystrihnute-sceny"));
		CATHEGORIES.put(R.id.channelDovolenkarisButton, new Cathegory("Dovolenkáris", "dovolenkaris"));
		CATHEGORIES.put(R.id.channelLFBButton, new Cathegory("Lokal Freestyle Battle", "lokal-freestyle-battle"));
		CATHEGORIES.put(R.id.channelLHNButton, new Cathegory("Lokal HotNews", "lokal-hotnews"));
		CATHEGORIES.put(R.id.channelNapalRytmausaButton, new Cathegory("Napál Rytmausa", "napal-rytmausa"));
		CATHEGORIES.put(R.id.channelPistovinyButton, new Cathegory("Pištoviny", "pistoviny"));
		CATHEGORIES.put(R.id.channelReneTalkshowButton, new Cathegory("Reného talkshow", "reneho-talkshow"));
		CATHEGORIES.put(R.id.channelRobocotiButton, new Cathegory("RoboCoti", "robocoti"));
		CATHEGORIES.put(R.id.channelWilhelmButton, new Cathegory("Wilhelm & Bob", "wilhelm-bob"));
		CATHEGORIES.put(R.id.channelZapalRytmausaButton, new Cathegory("Zapál Rytmausa", "zapal-rytmausa"));
		CATHEGORIES.put(R.id.extraBonusButton, new Cathegory("Bonusy", "bonusy"));
		CATHEGORIES.put(R.id.extraVideoclipButton, new Cathegory("Videoklipy", "videoklipy"));
	}

	private static final String[] DUMMY_ALL_EPISODES_URL = new String[] {
			"lokal-hotnews-7/530/38",
			"wilhelm-bob-17-vojna/507/31",
			"kapurkova-bathorycka/563/3",
			"reneho-talkshow-23-angel-wicky/560/30"
	};

	public static String timeAgoInWords(String timestamp) {
		try {
			Date dateAdded = DATE_FORMATTER.parse(timestamp);
			CharSequence timestampInWords = DateUtils.getRelativeTimeSpanString(
					dateAdded.getTime(),
					System.currentTimeMillis(),
					DateUtils.SECOND_IN_MILLIS);

			return timestampInWords.toString().toUpperCase();
		} catch (ParseException e) {
			return timestamp;
		}

	}

	public static Video parseDetail(Document detail, String url) {
		String title = detail.getElementsByTag("h1").get(0).text();

		Elements breadCrumbs = detail.getElementById("breadcrumb").getElementsByTag("li");
		String cathegory = null;
		try {
			cathegory = breadCrumbs.get(1).text().substring(2) + breadCrumbs.get(2).text();
		} catch (IndexOutOfBoundsException e) {
			cathegory = breadCrumbs.get(1).text().substring(2);
		}

		Element videoTag = detail.getElementsByTag("video").get(0);
		String imageUrl = videoTag.attr("poster");

		Element sourceTag = detail.getElementsByTag("source").get(0);
		String videoUrl = sourceTag.attr("src");

		Elements timestampAndViews = detail.getElementsByClass("video-subtitle").get(0).children();
		String timestamp = timestampAndViews.get(0).text().substring(9);
		String viewCount = null;
		try {
			viewCount = timestampAndViews.get(1).text().substring(14);
		} catch (IndexOutOfBoundsException e) {
			viewCount = "-";
		}

		// V tomto su aj tagy potom
		Elements descAndTags = detail.getElementsByClass("video-info").get(0).children();
		String desc = descAndTags.get(0).text();

		// Podobne videa
		ArrayList<RelatedVideo> relatedItems = new ArrayList<RelatedVideo>();
		Elements videoItems = detail.getElementsByClass("video-item");
		for (Element element : videoItems) {
			Element anchor = element.getElementsByTag("a").get(0);
			// Url
			String relatedUrl = anchor.attr("href");

			Element img = element.getElementsByTag("img").get(0);
			// Title
			String relatedTitle = img.attr("title");
			// Image
			String tmpImageUrl = img.attr("src");
			int index = tmpImageUrl.indexOf("small");
			String relatedImageUrl = tmpImageUrl.substring(0, index) + "medium"
					+ tmpImageUrl.substring(index + 5, tmpImageUrl.length());
			// Timestamp
			String relatedTimestamp = element.getElementsByClass("datum").get(0).text();

			relatedItems.add(new RelatedVideo(relatedTitle, relatedUrl, relatedImageUrl, relatedTimestamp));
			// LOG.d("////////\nTitle:" + relatedTitle + "\nUrl: " + relatedUrl + "\nImageUrl: " + relatedImageUrl +
			// "\nTimestamp: " + relatedTimestamp);
		}

		return new Video(title, desc, cathegory, url, imageUrl, videoUrl, timestamp, viewCount, relatedItems);
	}

	public static Video parseDetail(String url) throws IOException {
		if (!url.startsWith(RestService.BASE_URL)) {
			return null;
		}

		try {
			Document detail = Jsoup.connect(url).get();
			return parseDetail(detail, url);

		} catch (NullPointerException e) {
			LOG.e("Not a video1 - " + url);

		} catch (IndexOutOfBoundsException e) {
			// Na nevidea serem, zbytocne, lebo nemam potom
			// obrazky a title a vsetko...
			// To si potom s nimi dovodnem v APIcku
			// Mozno potom skusit na
			// url begins with http://www.lokaltv.sk/...
			LOG.e("Not a video2 - " + url);
			LOG.e(e);
		}
		return null;
	}

	public static String getRandomEpisodeUrl() {
		Random r = new Random();
		int index = r.nextInt(DUMMY_ALL_EPISODES_URL.length - 1);
		return "/" + DUMMY_ALL_EPISODES_URL[index];
	}
}
