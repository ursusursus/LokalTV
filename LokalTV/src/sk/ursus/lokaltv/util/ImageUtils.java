package sk.ursus.lokaltv.util;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.SimpleBitmapCache;
import com.android.volley.toolbox.Volley;

public class ImageUtils {

	private static ImageUtils instance;
	private ImageLoader mImageLoader;

	public static ImageUtils getInstance(Context context) {
		if (instance == null) {
			instance = new ImageUtils(context.getApplicationContext());
		}
		return instance;
	}

	private ImageUtils(Context context) {
		RequestQueue queue = Volley.newRequestQueue(context);
		SimpleBitmapCache cache = SimpleBitmapCache.getInstance(context);
		mImageLoader = new ImageLoader(queue, cache);
	}

	public ImageLoader getImageLoader() {
		return mImageLoader;
	}

}
