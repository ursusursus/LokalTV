package sk.ursus.lokaltv.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sk.ursus.lokaltv.model.Video;

import com.awaboom.ursus.agave.LOG;

public class VideosCache {
	
	private static final Map<String, ArrayList<Video>> sCache = new HashMap<String, ArrayList<Video>>();
	
	public static ArrayList<Video> get(String cathegoryUrl) {
		if(!sCache.containsKey(cathegoryUrl)) {
			return null;
		}
		return sCache.get(cathegoryUrl);
	}
	
	public static void put(String cathegoryUrl, ArrayList<Video> videos) {
		sCache.put(cathegoryUrl, videos);
	}
	
	public static void clear() {
		sCache.clear();
	}

}
