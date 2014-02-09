package sk.ursus.lokaltv;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.view.View;

public class UiHider {

	private static final long AUTO_HIDE_DELAY = 3000;
	private static final int FLAGS = View.SYSTEM_UI_FLAG_FULLSCREEN // Removed status bar
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Prevents resizing after status bar is gone
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Removes nav bar
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

	private Activity mActivity;
	private ActionBar mActionBar;

	public UiHider(Activity activity, ActionBar actionBar) {
		mActivity = activity;
		mActionBar = actionBar;
	}

	public void init() {
		final View decorView = mActivity.getWindow().getDecorView();
		decorView.setOnSystemUiVisibilityChangeListener(mVisibilityListener);

		hideSystemUi();
		hideAppUi();
	}

	public void cancel() {
		final View decorView = mActivity.getWindow().getDecorView();
		decorView.setOnSystemUiVisibilityChangeListener(null);

		showSystemUi();
		showAppUi();

		mHandler.removeCallbacks(mRunnable);
	}

	public void hideUiDelayed() {
		mHandler.removeCallbacks(mRunnable);
		mHandler.postDelayed(mRunnable, AUTO_HIDE_DELAY);
	}

	public void hideAppUi() {
		mActionBar.hide();
		//
	}

	public void showAppUi() {
		mActionBar.show();
		//
	}

	public void hideSystemUi() {
		View decorView = mActivity.getWindow().getDecorView();
		decorView.setSystemUiVisibility(FLAGS);
	}

	private void showSystemUi() {
		View decorView = mActivity.getWindow().getDecorView();
		decorView.setSystemUiVisibility(0);
	}

	private Handler mHandler = new Handler();
	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			hideSystemUi();
			hideAppUi();
		}
	};

	private View.OnSystemUiVisibilityChangeListener mVisibilityListener =
			new View.OnSystemUiVisibilityChangeListener() {

				@Override
				public void onSystemUiVisibilityChange(int i) {
					if (i == 0) {
						showAppUi();
						hideUiDelayed();
					}
				}
			};

}
