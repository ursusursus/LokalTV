package sk.ursus.lokaltv.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MyVideoController implements OnClickListener, OnSeekBarChangeListener {

	private static final long FADE_DURATION = 250;
	private View mRoot;
	private MyVideoControl mControl;
	private ImageButton mPlayPauseButton;
	private boolean mShowing = true;

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
		/* mPlayPauseButton = (ImageButton) view.findViewById(0);
		mPlayPauseButton.setOnClickListener(this);
		
		SeekBar mSeekBar = (SeekBar) view.findViewById(1);
		mSeekBar.setOnSeekBarChangeListener(this); */
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
		} else {
			mControl.play();
		}
	}

	private void updateProgress(int progress) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case 0:
			playPause();
			break;
		}

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// update èas
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// no-op ?
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		int progress = seekBar.getProgress();
		//
	}

}
