package sk.ursus.lokaltv.util;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.SimpleBitmapCache;
import com.android.volley.toolbox.Volley;

public class ImageManager {

	private static ImageManager instance;
	private ImageLoader mImageLoader;

	public static ImageManager getInstance(Context context) {
		if (instance == null) {
			instance = new ImageManager(context.getApplicationContext());
		}
		return instance;
	}

	private ImageManager(Context context) {
		RequestQueue queue = Volley.newRequestQueue(context);
		SimpleBitmapCache cache = SimpleBitmapCache.getInstance(context);
		mImageLoader = new ImageLoader(queue, cache);
	}

	public ImageLoader getImageLoader() {
		return mImageLoader;
	}

}
