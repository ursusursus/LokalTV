package sk.ursus.lokaltv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;

@SuppressLint("NewApi")
public class SystemUiHider {

	protected OnVisibilityChangeListener mListener;

	/**
	 * When this flag is set, {@link #show()} and {@link #hide()} will toggle the visibility of the status bar. If there
	 * is a navigation bar, show and hide will toggle low profile mode.
	 */
	public static final int FLAG_FULLSCREEN = 0x2;

	/**
	 * When this flag is set, {@link #show()} and {@link #hide()} will toggle the visibility of the navigation bar, if
	 * it's present on the device and the device allows hiding it. In cases where the navigation bar is present but
	 * cannot be hidden, show and hide will toggle low profile mode.
	 */
	public static final int FLAG_HIDE_NAVIGATION = FLAG_FULLSCREEN | 0x4;

	/**
	 * Flags for {@link View#setSystemUiVisibility(int)} to use when showing the system UI.
	 */
	private int mShowFlags;

	/**
	 * Flags for {@link View#setSystemUiVisibility(int)} to use when hiding the system UI.
	 */
	private int mHideFlags;

	/**
	 * Whether or not the system UI is currently visible. This is cached from
	 * {@link android.view.View.OnSystemUiVisibilityChangeListener}.
	 */
	// private boolean mVisible = true;
	private View mAnchorView;

	/**
	 * Constructor not intended to be called by clients. Use {@link SystemUiHider#getInstance} to obtain an instance.
	 */
	@SuppressLint("NewApi")
	public SystemUiHider(Activity activity, View anchorView, int flags) {
		mAnchorView = anchorView;

		mShowFlags = View.SYSTEM_UI_FLAG_VISIBLE;
		mHideFlags = View.SYSTEM_UI_FLAG_LOW_PROFILE;

		if ((flags & FLAG_FULLSCREEN) != 0) {
			mShowFlags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			mHideFlags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;
		}

		if ((flags & FLAG_HIDE_NAVIGATION) != 0) {
			mShowFlags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
			mHideFlags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
		}

		// anchorView.setOnSystemUiVisibilityChangeListener(mSystemUiVisibilityChangeListener);
	}

	public void setEnabled(boolean enabled) {
		if (enabled) {
			mAnchorView.setOnSystemUiVisibilityChangeListener(mSystemUiVisibilityChangeListener);
		} else {
			mAnchorView.setOnSystemUiVisibilityChangeListener(null);
		}
	}

	public void hide() {
		mAnchorView.setSystemUiVisibility(mHideFlags);
	}

	@SuppressLint("NewApi")
	public void show() {
		mAnchorView.setSystemUiVisibility(mShowFlags);
	}

	/* public boolean isVisible() {
		return mVisible;
	} */

	public void setOnVisibilityChangeListener(OnVisibilityChangeListener listener) {
		mListener = listener;
	}

	/**
	 * A callback interface used to listen for system UI visibility changes.
	 */
	public interface OnVisibilityChangeListener {
		public void onVisibilityChanged(boolean visible);
	}

	private View.OnSystemUiVisibilityChangeListener mSystemUiVisibilityChangeListener = new View.OnSystemUiVisibilityChangeListener() {
		@Override
		public void onSystemUiVisibilityChange(int vis) {
			// Test against mTestFlags to see if the system UI is visible.
			if (vis != 0) {
				mListener.onVisibilityChanged(false);
				// mVisible = false;

			} else {
				mAnchorView.setSystemUiVisibility(mShowFlags);
				mListener.onVisibilityChanged(true);
				// mVisible = true;
			}
		}
	};
}
