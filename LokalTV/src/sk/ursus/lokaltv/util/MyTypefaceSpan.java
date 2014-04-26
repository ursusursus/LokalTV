package sk.ursus.lokaltv.util;

import sk.ursus.lokaltv.ui.TypefaceUtils;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class MyTypefaceSpan extends MetricAffectingSpan {

	private Typeface mTypeface;

	public MyTypefaceSpan(Context context) {
		mTypeface = TypefaceUtils.get(context, TypefaceUtils.ROBOTO_SLAB_BOLD);
		// mTypeface = Typeface.createFromAsset(context.getAssets(), "CEGOLDIN.TTF");
	}

	@Override
	public void updateMeasureState(TextPaint p) {
		p.setTypeface(mTypeface);

		// Note: This flag is required for proper typeface rendering
		p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
	}

	@Override
	public void updateDrawState(TextPaint tp) {
		tp.setTypeface(mTypeface);

		// Note: This flag is required for proper typeface rendering
		tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
	}
}
