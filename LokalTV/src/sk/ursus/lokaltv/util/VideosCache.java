package sk.ursus.lokaltv.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sk.ursus.lokaltv.model.Video;

public class VideosCache {
	
	public static final String NEWS = "news";

	private static final Map<String, ArrayList<Video>> sCache = new HashMap<String, ArrayList<Video>>();

	public static ArrayList<Video> get(String cathegoryUrl) {
		synchronized (sCache) {
			if (!sCache.containsKey(cathegoryUrl)) {
				return null;
			}
			return sCache.get(cathegoryUrl);
		}
	}

	public static void put(String cathegoryUrl, ArrayList<Video> videos) {
		sCache.put(cathegoryUrl, videos);
	}

	public static void clear() {
		sCache.clear();
	}

}
