package sk.ursus.lokaltv.util;

import java.util.Formatter;
import java.util.Locale;

import sk.ursus.lokaltv.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MyVideoController implements OnClickListener, OnSeekBarChangeListener {

	private static final long FADE_DURATION = 250;
	private View mRoot;
	private MyVideoControl mControl;
	private ImageButton mPlayPauseButton;
	private boolean mShowing = true;
	private SeekBar mSeekBar;
	private TextView mCurrentTimeTextView;
	private TextView mTotalTimeTextView;
	private StringBuilder mFormatBuilder;
	private Formatter mFormatter;

	public interface MyVideoControl {
		void play();

		void pause();

		int getDuration();

		int getCurrentPosition();

		void seekTo(int pos);

		boolean isPlaying();

		int getBufferPercentage();
	}

	public MyVideoController(View view) {
		mRoot = view;
		mPlayPauseButton = (ImageButton) view.findViewById(R.id.playPauseButton);
		mPlayPauseButton.setOnClickListener(this);

		mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
		mSeekBar.setOnSeekBarChangeListener(this);
		// mSeekBar.setMax(1000);

		mTotalTimeTextView = (TextView) view.findViewById(R.id.totalTimeTextView);
		mCurrentTimeTextView = (TextView) view.findViewById(R.id.currentTimeTextView);
		// Casy updatnut az v onPrepared ...
		
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
						mShowing = true;
					}

				});

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
						mShowing = false;
					}

				});

	}

	private void playPause() {
		if (mControl.isPlaying()) {
			mControl.pause();
			mPlayPauseButton.setImageResource(R.drawable.ic_action_play_over_video);
		} else {
			mControl.play();
			mPlayPauseButton.setImageResource(R.drawable.ic_action_pause_over_video);
		}
	}

	private int timeToProgress(int time) {
		return time * 100 / mControl.getDuration();
	}

	private int progressToTime(int progress) {
		return mControl.getDuration() * progress / 100;
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

	public void updateProgress() {
		int progress = timeToProgress(mControl.getCurrentPosition());
		mSeekBar.setProgress(progress);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.playPauseButton:
			playPause();
			break;
		}

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		LOG.d("onProgressChanged");
		if (!fromUser) {
			//
		}
		// update èas
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		LOG.d("onStartTrackingTouch");
		// no-op ?
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		LOG.d("onStopTrackingTouch");

		int progress = seekBar.getProgress();
		mControl.seekTo(progressToTime(progress));
		//
	}

}
