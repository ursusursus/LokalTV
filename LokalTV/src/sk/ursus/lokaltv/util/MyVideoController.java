package sk.ursus.lokaltv.util;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MyVideoController implements OnClickListener, OnSeekBarChangeListener {

	private View mRoot;
	private MyVideoControl mControl;
	private ImageButton mPlayPauseButton;

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
		mPlayPauseButton = (ImageButton) view.findViewById(0);
		mPlayPauseButton.setOnClickListener(this);
		
		SeekBar mSeekBar = (SeekBar) view.findViewById(1);
		mSeekBar.setOnSeekBarChangeListener(this);
	}

	public void setVideoControl(MyVideoControl control) {
		mControl = control;
	}

	public void show() {
		// Iba animovany fade do View.INVISIBLE,
		// ziadne pridavacky views a neviem co
		mRoot.setVisibility(View.VISIBLE);
	}

	public void hide() {
		//
		mRoot.setVisibility(View.INVISIBLE);
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
