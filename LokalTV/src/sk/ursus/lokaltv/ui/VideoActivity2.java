package sk.ursus.lokaltv.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.SystemUiHider;
import sk.ursus.lokaltv.SystemUiHider.OnVisibilityChangeListener;
import sk.ursus.lokaltv.model.RelatedVideo;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.util.ImageUtils;
import sk.ursus.lokaltv.util.LOG;
import sk.ursus.lokaltv.util.MyMediaController;
import sk.ursus.lokaltv.util.MyVideoView;
import sk.ursus.lokaltv.util.Utils;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

public class VideoActivity2 extends ActionBarActivity {
	protected static final int AUTO_HIDE_DELAY_MILLIS = 2500;

	private MyVideoView mVideoView;
	private SystemUiHider mUiHider;

	private Handler mHideHandler = new Handler();
	private Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			getSupportActionBar().hide();
			mUiHider.hide();
		}
	};

	private ProgressBar mProgressBar;
	private Video mVideo;
	private boolean mShouldResume = false;

	private TextView mDescTextView;
	private int mVideoViewHeight;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_2);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		mVideo = (Video) intent.getParcelableExtra("feed_item");

		// Init layout
		// mVideoView = (VideoView) findViewById(R.id.videoView);
		mVideoView = (MyVideoView) findViewById(R.id.videoView);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

		TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
		titleTextView.setText(mVideo.title);

		ImageButton toggleButton = (ImageButton) findViewById(R.id.expandButton);
		toggleButton.setOnClickListener(mToggleClickListener);

		mDescTextView = (TextView) findViewById(R.id.descTextView);
		mDescTextView.setText(mVideo.desc);

		TextView timestampTextView = (TextView) findViewById(R.id.addedTextView);
		SimpleDateFormat dateParser = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
		try {
			Date dateAdded = dateParser.parse(mVideo.timestamp);
			CharSequence timestampInWords = DateUtils.getRelativeTimeSpanString(
					dateAdded.getTime(),
					System.currentTimeMillis(),
					DateUtils.SECOND_IN_MILLIS);

			timestampTextView.setText(timestampInWords.toString());
		} catch (ParseException e) {
			timestampTextView.setText(mVideo.timestamp);
		}

		TextView viewCountTextView = (TextView) findViewById(R.id.viewCountTextView);
		viewCountTextView.setText(mVideo.viewCount + " videnÌ");

		ImageLoader imageLoader = ImageUtils.getInstance(this).getImageLoader();
		try {
			initRelatedVideo(mVideo.relatedItems.get(0), R.id.relatedVideo1, imageLoader, dateParser);
			initRelatedVideo(mVideo.relatedItems.get(1), R.id.relatedVideo2, imageLoader, dateParser);
			initRelatedVideo(mVideo.relatedItems.get(2), R.id.relatedVideo3, imageLoader, dateParser);
			initRelatedVideo(mVideo.relatedItems.get(3), R.id.relatedVideo4, imageLoader, dateParser);
		} catch (IndexOutOfBoundsException e) {

		}

		// Init video
		mVideoView.setVideoURI(Uri.parse(mVideo.videoUrl));
		// mVideoView.setMediaController(new MediaController(this));
		mVideoView.setMediaController(new MyMediaController(this));
		mVideoView.requestFocus();
		mVideoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				mProgressBar.setVisibility(View.GONE);
				mVideoView.start();
			}
		});

		/* if (savedInstanceState != null) {
			int savedPosition = savedInstanceState.getInt("position");
			mVideoView.seekTo(savedPosition);
		} */

		// Init UI hider
		/* mUiHider = new SystemUiHider(this, getWindow().getDecorView(), SystemUiHider.FLAG_HIDE_NAVIGATION);
		mUiHider.setOnVisibilityChangeListener(mSystemUiVisiblityListener); */

		Resources r = getResources();
		int screenWidth = r.getDisplayMetrics().widthPixels;
		mVideoViewHeight = (int) ((float) screenWidth / Utils.PRESUMED_VIDEO_WIDTH * Utils.PRESUMED_VIDEO_HEIGHT);

		int orientation = r.getConfiguration().orientation;
		handleOrientationChange(orientation);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mShouldResume) {
			mVideoView.resume();
		}

	}

	@Override
	public void onPause() {
		super.onPause();

		if (mVideoView.isPlaying()) {
			mVideoView.pause();
			mShouldResume = true;
		}
	}

	private void handleOrientationChange(int orientation) {
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// ActionBar
			final ActionBar actionBar = getSupportActionBar();
			actionBar.setTitle(mVideo.title);
			
			// VideoView
			ViewGroup container = (ViewGroup) findViewById(R.id.videoContainer);
			LayoutParams params = container.getLayoutParams();
			params.width = LayoutParams.MATCH_PARENT;
			params.height = LayoutParams.MATCH_PARENT;
			container.requestLayout();

			/* mUiHider.setEnabled(true);
			delayedHideUi(200); */

		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			// ActionBar
			final ActionBar actionBar = getSupportActionBar();
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.show();
			
			// VideoView
			ViewGroup container = (ViewGroup) findViewById(R.id.videoContainer);
			LayoutParams params = container.getLayoutParams();
			params.width = LayoutParams.MATCH_PARENT;
			params.height = mVideoViewHeight;

			/* mHideHandler.removeCallbacks(mHideRunnable);
			mUiHider.setEnabled(false);
			mUiHider.show(); */
		}
	}

	private void initRelatedVideo(RelatedVideo relatedItem, int layoutId, ImageLoader imageLoader,
			SimpleDateFormat dateParser) {
		View view = findViewById(layoutId);

		NetworkImageView imageView = (NetworkImageView) view.findViewById(R.id.imageView);
		imageView.setImageUrl(relatedItem.imageUrl, imageLoader);

		TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
		titleTextView.setText(relatedItem.title);

		TextView timestampTextView = (TextView) view.findViewById(R.id.timestampTextView);
		try {
			Date dateAdded = dateParser.parse(relatedItem.timestamp);
			CharSequence timestampInWords = DateUtils.getRelativeTimeSpanString(
					dateAdded.getTime(),
					System.currentTimeMillis(),
					DateUtils.SECOND_IN_MILLIS);

			timestampTextView.setText(timestampInWords.toString().toUpperCase());
		} catch (ParseException e) {
			timestampTextView.setText(mVideo.timestamp);
		}
	}

	private void delayedHideUi(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	private void share() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, makeSharedText());
		intent.setType("text/plain");

		Intent chooser = Intent.createChooser(intent, "Zdielaù cez...");
		startActivity(chooser);
	}

	private String makeSharedText() {
		return mVideo.url
				+ "\n\n----------------------"
				+ "\nZdieæanÈ cez aplik·ciu LokalTV"
				+ "\nhttps://play.google.com/store/apps/details?id=sk.ursus.lokaltv" +
				"\n----------------------";
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("position", mVideoView.getCurrentPosition());
	}

	/* @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		LOG.d("onConfigurationChanged");

		initActionBar(newConfig.orientation);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			LOG.d("Its landscape");

			LayoutParams landscapeParams = new FrameLayout.LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			landscapeParams.gravity = Gravity.CENTER;
			mVideoView.setLayoutParams(landscapeParams);

			// delayedHide(100);
			// getActivity().setTheme(R.style.Theme_MyStyle_Fullscreen);

		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			LOG.d("Its portrait");
			
			LayoutParams portraitParams = new FrameLayout.LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			mVideoView.setLayoutParams(portraitParams);

			// getActivity().setTheme(R.style.Theme_MyStyle);
		}
	} */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		LOG.d("onConfigurationChanged");

		handleOrientationChange(newConfig.orientation);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.fragment_video, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		case R.id.action_share:
			share();
			return true;

		case R.id.action_favourite:
			//
			return true;

		default:
			return false;
		}
	}

	private OnClickListener mToggleClickListener = new OnClickListener() {

		private boolean mExpanded = false;

		@Override
		public void onClick(View v) {
			ImageButton imageButton = (ImageButton) v;
			if (!mExpanded) {
				imageButton.setImageResource(R.drawable.collapse);
				mDescTextView.setMaxLines(1000);
				mExpanded = true;
			} else {
				imageButton.setImageResource(R.drawable.expand);
				mDescTextView.setMaxLines(2);
				mExpanded = false;
			}
		}
	};

	private OnVisibilityChangeListener mSystemUiVisiblityListener = new OnVisibilityChangeListener() {

		@Override
		public void onVisibilityChanged(boolean isVisible) {
			if (isVisible) {
				delayedHideUi(AUTO_HIDE_DELAY_MILLIS);
			}
		}
	};
}
