package sk.ursus.lokaltv.util;

import sk.ursus.lokaltv.video.MyVideoController;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Handler;
import android.view.View;

import com.awaboom.ursus.agave.LOG;

public class UiHider2 {

	private static final long AUTO_HIDE_DELAY = 3000;
	private static final int FLAGS = View.SYSTEM_UI_FLAG_FULLSCREEN // Removed status bar
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Prevents resizing after status bar is gone
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Removes nav bar
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION; // Prevents resizing after status nav is gone

	private Activity mActivity;
	// private ActionBar mActionBar;
	private MyVideoController mVideoController;

	private boolean mPlaying;
	private boolean mDragging;
	private boolean mLandscape;
	private boolean mShowing;

	// public UiHider2(Activity activity, ActionBar actionBar, MyVideoController videoController) {
	public UiHider2(Activity activity, MyVideoController videoController) {
		mActivity = activity;
		// mActionBar = actionBar;
		mVideoController = videoController;

		mShowing = false;
		// mPlaying = false;
		mPlaying = true;
		mDragging = false;
		mLandscape = false;
	}

	public void toggle() {
		if (mShowing) {
			hide();
		} else {
			show();
		}
	}

	public void show() {
		show(true);
	}

	public void show(boolean delayedHide) {
		if (!mShowing) {
			showAppUi();
			if (mLandscape) {
				showSystemUi();
			}

			if (delayedHide && mPlaying && !mDragging) {
				delayedHide();
			}

			mShowing = true;
		}
	}

	public void hide() {
		if (mShowing) {
			hideAppUi();
			if (mLandscape) {
				hideSystemUi();
			}

			mShowing = false;
		}
	}

	private void delayedHide() {
		LOG.d("delayedHide");

		mHandler.removeCallbacks(mRunnable);
		mHandler.postDelayed(mRunnable, AUTO_HIDE_DELAY);
	}

	private void cancelDelayedHide() {
		mHandler.removeCallbacks(mRunnable);
	}

	public void hideAppUi() {
		// mActionBar.hide();
		mVideoController.hide();
		//
	}

	public void showAppUi() {
		// mActionBar.show();
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

	public void setDragging(boolean dragging) {
		mDragging = dragging;
	}

	public void onOrientationChanged(int orientation) {
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			LOG.d("onOrientationChanged: LANDSCAPE");
			final View decorView = mActivity.getWindow().getDecorView();
			decorView.setOnSystemUiVisibilityChangeListener(mVisibilityListener);

			hideSystemUi();
			mLandscape = true;

		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			LOG.d("onOrientationChanged: PORTRAIT");
			final View decorView = mActivity.getWindow().getDecorView();
			decorView.setOnSystemUiVisibilityChangeListener(null);

			showSystemUi();
			mLandscape = false;
		}

	}

	public void onVideoPlayedPaused(boolean playing) {
		if (playing) {
			delayedHide();
		} else {
			cancelDelayedHide();
		}
	}

	public void onDragged() {
		// toto asi nebude treba lebo ked dragujem tak pauzujem video
		// remove handler callbacks
	}

	public void setVideoPlaying(boolean playing) {
		mPlaying = playing;
	}

	private Handler mHandler = new Handler();
	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			LOG.d("AUTO_HIDE");
			hide();
		}
	};

	private View.OnSystemUiVisibilityChangeListener mVisibilityListener =
			new View.OnSystemUiVisibilityChangeListener() {

				@Override
				public void onSystemUiVisibilityChange(int i) {
					if (i == 0) {
						show();
					}
				}
			};

}
