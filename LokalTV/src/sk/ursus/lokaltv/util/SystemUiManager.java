package sk.ursus.lokaltv.util;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.View;

public class SystemUiManager {

	private static final int FLAGS = View.SYSTEM_UI_FLAG_FULLSCREEN // Removed status bar
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Prevents resizing after status bar is gone
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Removes nav bar
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION; // Prevents resizing after status nav is gone

	public interface OnVisibilityChangeListener {
		void onVisibilityChanged(boolean isVisible);
	}

	private View mDecorView;
	private OnVisibilityChangeListener mListener;

	public SystemUiManager(Activity activity) {
		mDecorView = activity.getWindow().getDecorView();
	}

	public void hide() {
		mDecorView.setSystemUiVisibility(FLAGS);
	}

	public void show() {
		mDecorView.setSystemUiVisibility(0);
	}

	public void onOrientationChanged(int orientation) {
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			LOG.d("onOrientationChanged: LANDSCAPE");
			mDecorView.setOnSystemUiVisibilityChangeListener(mVisibilityListener);
			
			hide();

		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			LOG.d("onOrientationChanged: PORTRAIT");
			mDecorView.setOnSystemUiVisibilityChangeListener(null);
			
			show();
		}
	}

	public void setOnVisibilityChangeListener(OnVisibilityChangeListener l) {
		mListener = l;
	}

	private View.OnSystemUiVisibilityChangeListener mVisibilityListener =
			new View.OnSystemUiVisibilityChangeListener() {

				@Override
				public void onSystemUiVisibilityChange(int i) {
					if (i == 0) {
						if (mListener != null) {
							mListener.onVisibilityChanged(true);
						}
					} else {
						if (mListener != null) {
							mListener.onVisibilityChanged(false);
						}
					}
				}
			};

}
