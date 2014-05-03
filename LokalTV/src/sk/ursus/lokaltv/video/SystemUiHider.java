package sk.ursus.lokaltv.video;

import android.view.View;

import com.awaboom.ursus.agave.LOG;

public class SystemUiHider {

	/* private static final int FLAGS = View.SYSTEM_UI_FLAG_FULLSCREEN // Removed status bar
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Prevents resizing after status bar is gone
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Removes nav bar
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION; // Prevents resizing after status nav is gone */

	private static final int FLAGS = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

	private VisibilityChangedListener mVisibilityListener;
	private View mDecorView;
	private boolean mFullscreen;

	public SystemUiHider(View decorView) {
		mDecorView = decorView;
	}

	public void show() {
		if (!mFullscreen) {
			return;
		}

		LOG.i("Showing SysUi...");
		mDecorView.setSystemUiVisibility(FLAGS
				& ~View.SYSTEM_UI_FLAG_FULLSCREEN
				& ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}

	public void hide() {
		if (!mFullscreen) {
			return;
		}

		LOG.i("Hiding SysUi...");
		mDecorView.setSystemUiVisibility(FLAGS
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}

	public void init() {
		mDecorView.setOnSystemUiVisibilityChangeListener(mVisibilityChangeListener);
		mDecorView.setVisibility(FLAGS);

		mFullscreen = true;
	}

	public void cancel() {
		mDecorView.setOnSystemUiVisibilityChangeListener(null);
		mDecorView.setSystemUiVisibility(0);

		mFullscreen = false;
	}

	public void setVisibilityChangedListener(VisibilityChangedListener l) {
		mVisibilityListener = l;
	}

	public boolean isFullscreen() {
		return mFullscreen;
	}

	private View.OnSystemUiVisibilityChangeListener mVisibilityChangeListener =
			new View.OnSystemUiVisibilityChangeListener() {

				@Override
				public void onSystemUiVisibilityChange(int i) {
					if (mVisibilityListener != null) {
						mVisibilityListener.onVisibilityChanged(i == 0);
					}
				}
			};

}
