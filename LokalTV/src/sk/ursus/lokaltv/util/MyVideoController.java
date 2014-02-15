package sk.ursus.lokaltv.util;

import java.util.Formatter;
import java.util.Locale;

import sk.ursus.lokaltv.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

// public class MyVideoController implements OnClickListener, OnSeekBarChangeListener {
public class MyVideoController {

	private static final long FADE_DURATION = 250;
	protected static final int SHOW_PROGRESS = 1;

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

	public interface MyVideoControl {
		void play();

		void pause();

		int getTotalTime();

		int getCurrentTime();

		void seekTo(int pos);

		boolean isPlaying();

		int getBufferPercentage();
	}

	public MyVideoController(View view) {
		mRoot = view;
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

	public void setVideoControl(MyVideoControl control) {
		mControl = control;
	}

	public boolean isShowing() {
		return mShowing;
	}

	public void show() {
		mRoot.setAlpha(0f);
		mRoot.setVisibility(View.VISIBLE);
		mRoot.animate()
				.alpha(1F)
				.setDuration(FADE_DURATION)
				.setListener(new AnimatorListenerAdapter() {

					@Override
					public void onAnimationEnd(Animator animation) {
						// mShowing = true;
					}
				});

		initHandler();
		mShowing = true;
	}

	public void hide() {
		mRoot.animate()
				.alpha(0F)
				.setDuration(FADE_DURATION)
				.setListener(new AnimatorListenerAdapter() {

					@Override
					public void onAnimationEnd(Animator animation) {
						mRoot.setAlpha(1f);
						mRoot.setVisibility(View.INVISIBLE);
						// mShowing = false;
					}

				});

		cancelHandler();
		mShowing = false;
	}

	private void playPause() {
		if (mControl.isPlaying()) {
			mControl.pause();
			mPlayPauseButton.setImageResource(R.drawable.ic_action_play_over_video);
			cancelHandler();

		} else {
			mControl.play();
			mPlayPauseButton.setImageResource(R.drawable.ic_action_pause_over_video);
			initHandler();
		}
	}

	private void updatePausePlay() {
		if (mControl.isPlaying()) {
			mPlayPauseButton.setImageResource(R.drawable.ic_action_play_over_video);

		} else {
			mPlayPauseButton.setImageResource(R.drawable.ic_action_pause_over_video);
		}
	}

	private void initHandler() {
		displayProgress();
		mHandler.sendEmptyMessage(SHOW_PROGRESS);
	}

	private void cancelHandler() {
		mHandler.removeMessages(SHOW_PROGRESS);
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
				mPlayPauseButton.setImageResource(R.drawable.ic_action_play_over_video);
			}

			cancelHandler();
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
			
			mPlayPauseButton.setImageResource(R.drawable.ic_action_pause_over_video);
			mHandler.sendEmptyMessage(SHOW_PROGRESS);
		}
	};

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == SHOW_PROGRESS) {
				// LOG.d("SHOW_PROGRESS");
				if (!mDragging && mShowing && mControl.isPlaying()) {
					int pos = displayProgress();

					msg = obtainMessage(SHOW_PROGRESS);
					sendMessageDelayed(msg, 1000 - (pos % 1000));

				}
			}
		}
	};

	public void showProgressBar() {
		mPlayPauseButton.setVisibility(View.INVISIBLE);
		mProgressBar.setVisibility(View.VISIBLE);
	}

	public void hideProgressBar() {
		mProgressBar.setVisibility(View.INVISIBLE);
		mPlayPauseButton.setVisibility(View.VISIBLE);
	}

}
