package sk.ursus.lokaltv.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.SystemUiHider;
import sk.ursus.lokaltv.SystemUiHider.OnVisibilityChangeListener;
import sk.ursus.lokaltv.model.RelatedVideo;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.net.RelatedVideoProcessor;
import sk.ursus.lokaltv.net.RestService;
import sk.ursus.lokaltv.net.ServerUtils.Callback;
import sk.ursus.lokaltv.net.ServerUtils.Status;
import sk.ursus.lokaltv.util.ImageUtils;
import sk.ursus.lokaltv.util.LOG;
import sk.ursus.lokaltv.util.MyVideoView;
import sk.ursus.lokaltv.util.Utils;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

public class VideoActivity2 extends ActionBarActivity {

	public static final String ACTION_PLAY = "sk.ursus.lokaltv.ACTION_PLAY";
	public static final String ACTION_FETCH_AND_PLAY = "sk.ursus.lokaltv.ACTION_FETCH_AND_PLAY";

	public static final String EXTRA_RELATED_VIDEO = "related_video";
	public static final String EXTRA_VIDEO = "feed_item";

	protected static final int AUTO_HIDE_DELAY_MILLIS = 2500;
	private static final long DELAY = 3000;

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

		Intent intent = getIntent();

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		String action = intent.getAction();
		if (action.equals(ACTION_FETCH_AND_PLAY)) {
			RelatedVideo relatedVideo = intent.getParcelableExtra(EXTRA_RELATED_VIDEO);
			RestService.getRelatedVideo(this, relatedVideo.url, new Callback() {

				@Override
				public void onResult(int status, Bundle data) {
					switch (status) {
					case Status.RUNNING:
						LOG.d("GetRelatedVideo # RUNNING");
						break;

					case Status.OK:
						LOG.d("GetRelatedVideo # OK");
						mVideo = data.getParcelable(RelatedVideoProcessor.RESULT_VIDEO);
						init();
						break;

					case Status.EXCEPTION:
						LOG.d("GetRelatedVideo # EXCEPTION");
						break;
					}
				}
			});
		} else if (action.equals(ACTION_PLAY)) {
			mVideo = (Video) getIntent().getParcelableExtra(EXTRA_VIDEO);
			init();
		}
	}

	private void init() {
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
		// mVideoView.setMediaController(new MyMediaController(this));
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

		//
		Resources r = getResources();
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

	private void initUiHiding() {
		final View decorView = getWindow().getDecorView();
		decorView.setOnSystemUiVisibilityChangeListener(mVisibilityListener);

		hideSystemUi();
		hideAppUi();
	}

	private void cancelUiHiding() {
		final View decorView = getWindow().getDecorView();
		decorView.setOnSystemUiVisibilityChangeListener(null);

		showSystemUi();
		showAppUi();

		mHandler.removeCallbacks(mRunnable);
	}

	public void hideUiDelayed() {
		mHandler.removeCallbacks(mRunnable);
		mHandler.postDelayed(mRunnable, DELAY);
	}

	public void hideAppUi() {
		getSupportActionBar().hide();
	}

	public void showAppUi() {
		getSupportActionBar().show();
	}

	public void hideSystemUi() {
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN // Removed status bar
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Prevents resizing after status bar is gone
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Removes nav bar
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION; // Prevents resizing after nav bar is gone

		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(uiOptions);
	}

	private void showSystemUi() {
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(0);
	}

	private void handleOrientationChange(int orientation) {
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// ActionBar
			final ActionBar actionBar = getSupportActionBar();
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setTitle(mVideo.title);

			// VideoView
			ViewGroup container = (ViewGroup) findViewById(R.id.videoContainer);
			LayoutParams params = container.getLayoutParams();
			params.width = LayoutParams.MATCH_PARENT;
			params.height = LayoutParams.MATCH_PARENT;
			container.requestLayout();

			// Ui
			initUiHiding();

		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			// ActionBar
			final ActionBar actionBar = getSupportActionBar();
			actionBar.setDisplayShowTitleEnabled(false);

			// Calculate VideoView height
			Resources r = getResources();
			int screenWidth = r.getDisplayMetrics().widthPixels;
			mVideoViewHeight = (int) ((float) screenWidth / Utils.PRESUMED_VIDEO_WIDTH * Utils.PRESUMED_VIDEO_HEIGHT);

			// VideoView
			ViewGroup container = (ViewGroup) findViewById(R.id.videoContainer);
			LayoutParams params = container.getLayoutParams();
			params.width = LayoutParams.MATCH_PARENT;
			params.height = mVideoViewHeight;

			// Ui
			cancelUiHiding();
		}
	}

	private void initRelatedVideo(final RelatedVideo relatedItem, int layoutId, ImageLoader imageLoader,
			SimpleDateFormat dateParser) {
		View view = findViewById(layoutId);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(VideoActivity2.this, VideoActivity2.class);
				intent.setAction(VideoActivity2.ACTION_FETCH_AND_PLAY);
				intent.putExtra(VideoActivity2.EXTRA_RELATED_VIDEO, relatedItem);

				startActivity(intent);
			}
		});

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
