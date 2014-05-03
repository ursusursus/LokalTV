package sk.ursus.lokaltv.util;

import sk.ursus.lokaltv.video.MyVideoController;
import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.view.View;

public class UiHider {

	private static final long AUTO_HIDE_DELAY = 5000;
	private static final int FLAGS = View.SYSTEM_UI_FLAG_FULLSCREEN // Removed status bar
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Prevents resizing after status bar is gone
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Removes nav bar
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION; // Prevents resizing after status nav is gone

	private Activity mActivity;
	private ActionBar mActionBar;
	private MyVideoController mVideoController;

	public UiHider(Activity activity, ActionBar actionBar, MyVideoController videoController) {
		mActivity = activity;
		mActionBar = actionBar;
		mVideoController = videoController;
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

	public void toggleAppUi() {
		if (mActionBar.isShowing() && mVideoController.isShowing()) {
			hideAppUi();
		} else {
			showAppUi();
			// hideDelayed
		}
	}

	public void hideAppUi() {
		mActionBar.hide();
		mVideoController.hide();
		//
	}

	public void showAppUi() {
		mActionBar.show();
		mVideoController.show();
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
			hideAppUi();
			hideSystemUi();
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
