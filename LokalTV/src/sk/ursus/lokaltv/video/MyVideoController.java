package sk.ursus.lokaltv.video;

import java.util.Formatter;
import java.util.Locale;

import sk.ursus.lokaltv.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.ActionBar;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.awaboom.ursus.agave.LOG;

// public class MyVideoController implements OnClickListener, OnSeekBarChangeListener {
public class MyVideoController {

	private static final long FADE_DURATION = 300;
	private static final int FADE_OUT_DELAY = 2000;
	protected static final int SHOW_PROGRESS = 1;
	protected static final int FADE_OUT = 2;

	private View mRoot;
	private SeekBar mSeekBar;
	private ImageButton mPlayPauseButton;
	private TextView mCurrentTimeTextView;
	private TextView mTotalTimeTextView;

	private MyVideoControl mControl;
	private StringBuilder mFormatBuilder;
	private Formatter mFormatter;

	private boolean mShowing;
	private boolean mDragging;
	private ProgressBar mProgressBar;
	private ActionBar mActionBar;
	private VisibilityChangedListener mListener;

	public interface MyVideoControl {
		void play();

		void pause();

		int getTotalTime();

		int getCurrentTime();

		void seekTo(int pos);

		boolean isPlaying();

		int getBufferPercentage();
	}

	public MyVideoController(View view, ActionBar actionBar) {
		mRoot = view;
		mActionBar = actionBar;

		mPlayPauseButton = (ImageButton) view.findViewById(R.id.playPauseButton);
		mPlayPauseButton.setOnClickListener(mPlayPauseClickListener);

		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

		mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
		mSeekBar.setOnSeekBarChangeListener(mSeekListener);
		mSeekBar.setMax(1000);

		mTotalTimeTextView = (TextView) view.findViewById(R.id.totalTimeTextView);
		mCurrentTimeTextView = (TextView) view.findViewById(R.id.currentTimeTextView);

		mFormatBuilder = new StringBuilder();
		mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
	}

	public void setVisibilityChangedListener(VisibilityChangedListener l) {
		mListener = l;
	}

	public void setVideoControl(MyVideoControl control) {
		mControl = control;
	}

	public boolean isShowing() {
		return mShowing;
	}

	public void showInit() {
		mActionBar.show();

		mSeekBar.setVisibility(View.INVISIBLE);
		mTotalTimeTextView.setVisibility(View.INVISIBLE);
		mCurrentTimeTextView.setVisibility(View.INVISIBLE);
		mProgressBar.setVisibility(View.INVISIBLE);
		mPlayPauseButton.setVisibility(View.INVISIBLE);
	}

	public void hideInit() {
		mSeekBar.setVisibility(View.VISIBLE);
		mTotalTimeTextView.setVisibility(View.VISIBLE);
		mCurrentTimeTextView.setVisibility(View.VISIBLE);
	}

	public void toggle() {
		if (mShowing) {
			hide();
		} else {
			if (mControl.isPlaying()) {
				// LOG.d("NOT STICKING");
				show();
			} else {
				// LOG.d("STICKING");
				show(0, true);
			}
		}
	}

	public void show() {
		show(FADE_OUT_DELAY, true);
	}

	public void show(boolean triggerListener) {
		show(FADE_OUT_DELAY, triggerListener);
	}

	public void show(int fadeOutDuration, final boolean triggerListener) {
		if (mShowing) {
			return;
		}
		LOG.i("Showing VideoController...");

		mActionBar.show();
		// Este potrebujem aj bez animacie, boolean immediate
		mRoot.setAlpha(0f);
		mRoot.setVisibility(View.VISIBLE);
		mRoot.animate()
				.alpha(1F)
				.setDuration(FADE_DURATION)
				.setListener(new AnimatorListenerAdapter() {

					@Override
					public void onAnimationEnd(Animator animation) {
						mShowing = true;

						if (mListener != null && triggerListener) {
							mListener.onVisibilityChanged(true);
						}
					}
				});

		updatePausePlay();

		initShowProgress();
		initAutoHide(fadeOutDuration);
	}

	public void hide() {
		if (!mShowing) {
			return;
		}
		LOG.i("Hiding VideoController...");

		mActionBar.hide();
		mRoot.animate()
				.alpha(0F)
				.setDuration(FADE_DURATION)
				.setListener(new AnimatorListenerAdapter() {

					@Override
					public void onAnimationEnd(Animator animation) {
						mRoot.setAlpha(1f);
						mRoot.setVisibility(View.INVISIBLE);
						mShowing = false;

						if (mListener != null) {
							mListener.onVisibilityChanged(false);
						}
					}

				});

		cancelShowProgress();
	}

	private void playPause() {
		if (mControl.isPlaying()) {
			mControl.pause();

			cancelShowProgress();
			cancelAutoHide();

		} else {
			mControl.play();

			initShowProgress();
			initAutoHide();
		}

		updatePausePlay();
	}

	private void updatePausePlay() {
		if (mControl.isPlaying()) {
			mPlayPauseButton.setImageResource(R.drawable.ic_pause);
		} else {
			mPlayPauseButton.setImageResource(R.drawable.ic_play);
		}

		/* mPlayPauseButton.animate()
				.alpha(0.0F)
				.scaleX(5F)
				.scaleY(5F)
				.setDuration(200L)
				.withEndAction(new Runnable() {

					@Override
					public void run() {
						mPlayPauseButton.setAlpha(1F);
						mPlayPauseButton.setScaleX(1F);
						mPlayPauseButton.setScaleY(1F);
						if (mControl.isPlaying()) {
							mPlayPauseButton.setImageResource(R.drawable.ic_pause);

						} else {
							mPlayPauseButton.setImageResource(R.drawable.ic_play);
						}
					}

				}); */
	}

	private int timeToProgress(int time) {
		return (int) (time * 1000L / mControl.getTotalTime());
	}

	private int progressToTime(int progress) {
		return (int) (mControl.getTotalTime() * progress / 1000L);
	}

	private String formatTime(int timeMs) {
		int totalSeconds = timeMs / 1000;

		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		mFormatBuilder.setLength(0);
		if (hours > 0) {
			return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
		} else {
			return mFormatter.format("%02d:%02d", minutes, seconds).toString();
		}
	}

	public int displayProgress() {
		if (mControl == null || mDragging) {
			return 0;
		}

		int currentTime = mControl.getCurrentTime();
		int totalTime = mControl.getTotalTime();
		if (totalTime > 0) {
			int progress = timeToProgress(currentTime);
			mSeekBar.setProgress(progress);
		}

		int percent = mControl.getBufferPercentage();
		mSeekBar.setSecondaryProgress(percent * 10);

		mTotalTimeTextView.setText(formatTime(totalTime));
		mCurrentTimeTextView.setText(formatTime(currentTime));

		return currentTime;
	}

	private OnClickListener mPlayPauseClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			playPause();
		}
	};

	private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {

		@Override
		public void onStartTrackingTouch(SeekBar bar) {
			mDragging = true;

			if (mControl.isPlaying()) {
				mControl.pause();
				mPlayPauseButton.setImageResource(R.drawable.ic_play);
			}

			cancelShowProgress();
			cancelAutoHide();
		}

		@Override
		public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
			if (!fromUser) {
				return;
			}

			long duration = mControl.getTotalTime();
			long newposition = (duration * progress) / 1000L;

			mCurrentTimeTextView.setText(formatTime((int) newposition));
		}

		@Override
		public void onStopTrackingTouch(SeekBar bar) {
			mDragging = false;

			int newTime = progressToTime(bar.getProgress());
			mControl.seekTo(newTime);
			mControl.play();

			mPlayPauseButton.setImageResource(R.drawable.ic_pause);

			initShowProgress();
			initAutoHide();

		}
	};

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == SHOW_PROGRESS) {
				if (!mDragging && mShowing && mControl.isPlaying()) {
					int pos = displayProgress();

					// Resend
					msg = obtainMessage(SHOW_PROGRESS);
					sendMessageDelayed(msg, 1000 - (pos % 1000));

				}
			} else if (msg.what == FADE_OUT) {
				hide();
			}
		}
	};

	public void showProgressBar() {
		mPlayPauseButton.setVisibility(View.INVISIBLE);
		mProgressBar.setVisibility(View.VISIBLE);
		
		/* ImageView imageView = (ImageView) getView().findViewById(R.id.imageView);
		
		PropertyValuesHolder rotation = PropertyValuesHolder.ofKeyframe(View.ROTATION,
				Keyframe.ofFloat(0F, 0F),
				Keyframe.ofFloat(0.5F, 180F),
				Keyframe.ofFloat(1F, 360F));

		PropertyValuesHolder scaleX = PropertyValuesHolder.ofKeyframe(View.SCALE_X,
				Keyframe.ofFloat(0F, 1F),
				Keyframe.ofFloat(0.5F, 0.8F),
				Keyframe.ofFloat(1F, 1F));

		PropertyValuesHolder scaleY = PropertyValuesHolder.ofKeyframe(View.SCALE_Y,
				Keyframe.ofFloat(0F, 1F),
				Keyframe.ofFloat(0.5F, 0.8F),
				Keyframe.ofFloat(1F, 1F));

		ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(imageView, scaleX, scaleY, rotation);
		animator.setDuration(2000L);
		animator.setRepeatCount(Animation.INFINITE);
		animator.setRepeatMode(Animation.RESTART);
		animator.start(); */
		
		cancelAutoHide();
	}

	public void hideProgressBar() {
		mProgressBar.setVisibility(View.INVISIBLE);
		mPlayPauseButton.setVisibility(View.VISIBLE);

		initAutoHide();
	}

	private void initShowProgress() {
		displayProgress();
		mHandler.sendEmptyMessage(SHOW_PROGRESS);
	}

	private void cancelShowProgress() {
		mHandler.removeMessages(SHOW_PROGRESS);
	}

	protected void initAutoHide() {
		initAutoHide(FADE_OUT_DELAY);
	}

	protected void initAutoHide(int fadeOutDelay) {
		Message msg = mHandler.obtainMessage(FADE_OUT);
		mHandler.removeMessages(FADE_OUT);
		if (fadeOutDelay != 0) {
			mHandler.sendMessageDelayed(msg, fadeOutDelay);
		}
	}

	protected void cancelAutoHide() {
		mHandler.removeMessages(FADE_OUT);
	}

}
