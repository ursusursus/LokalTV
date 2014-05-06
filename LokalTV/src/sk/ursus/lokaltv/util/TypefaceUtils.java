package sk.ursus.lokaltv.util;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

import com.awaboom.ursus.agave.LOG;

public class TypefaceUtils {

	public static final String ROBOTO_SLAB_REGULAR = "RobotoSlab-Regular.ttf";
	public static final String ROBOTO_SLAB_BOLD = "RobotoSlab-Bold.ttf";
	public static final String ROBOTO_SLAB_LIGHT = "RobotoSlab-Light.ttf";
	public static final String ROBOTO_SLAB_THIN = "RobotoSlab-Thin.ttf";

	private static final Map<String, Typeface> sCache = new HashMap<String, Typeface>();

	public static Typeface get(Context context, String font) {
		synchronized (sCache) {
			if (!sCache.containsKey(font)) {
				AssetManager assets = context.getApplicationContext().getAssets();
				Typeface tf = Typeface.createFromAsset(assets, font);
				sCache.put(font, tf);
			}
			return sCache.get(font);
		}
	}
}
