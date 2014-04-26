package sk.ursus.lokaltv.ui;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.model.RelatedVideo;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.net.RelatedVideoProcessor;
import sk.ursus.lokaltv.net.RestService;
import sk.ursus.lokaltv.net.ServerUtils.Callback;
import sk.ursus.lokaltv.net.ServerUtils.Status;
import sk.ursus.lokaltv.util.ImageUtils;
import sk.ursus.lokaltv.util.MyVideoController;
import sk.ursus.lokaltv.util.MyVideoView;
import sk.ursus.lokaltv.util.Utils;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.awaboom.ursus.agave.LOG;

public class VideoActivity2 extends ActionBarActivity {

	public static final String ACTION_PLAY = "sk.ursus.lokaltv.ACTION_PLAY";
	public static final String ACTION_FETCH_AND_PLAY = "sk.ursus.lokaltv.ACTION_FETCH_AND_PLAY";

	public static final String EXTRA_RELATED_VIDEO = "related_video";
	public static final String EXTRA_VIDEO = "feed_item";

	private MyVideoView mVideoView;

	// private UiHider mUiHider;
	// private UiHider2 mUiHider2;
	private Video mVideo;

	private int mVideoViewHeight;
	private int mPausedAt = 0;
	// private MyVideoController mVideoController;
	// private SystemUiManager mSystemUiManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_2);
		LOG.d("onCreate");

		Intent intent = getIntent();

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		String action = intent.getAction();
		if (action.equals(ACTION_FETCH_AND_PLAY)) {
			RelatedVideo relatedVideo = intent.getParcelableExtra(EXTRA_RELATED_VIDEO);
			RestService.getRelatedVideo(this, relatedVideo.url, mRelatedVideoCallback);

		} else if (action.equals(ACTION_PLAY)) {
			mVideo = (Video) getIntent().getParcelableExtra(EXTRA_VIDEO);
			init();
		}
	}

	private void init() {
		initViews();
		initVideoPlayback();

		// Init UI hider
		// mUiHider = new UiHider(this, getSupportActionBar(), mVideoController);
		// mUiHider2 = new UiHider2(this, getSupportActionBar(), mVideoController);
		// mUiHider2 = new UiHider2(this, mVideoController);
		/* mSystemUiManager = new SystemUiManager(this);
		mSystemUiManager.setOnVisibilityChangeListener(new OnVisibilityChangeListener() {
			
			@Override
			public void onVisibilityChanged(boolean isVisible) {
				if(isVisible) {
					getActionBar().show();
				} else {
					getActionBar().hide();
				}
				LOG.d("onVisibilityChanged: " + isVisible);
			}
		}); */

		// Init UI orientation
		int o = getResources().getConfiguration().orientation;
		handleOrientationChange(o);
		
		// mUiHider2.show(false);
	}

	private void initViews() {
		// mVideoView = (VideoView) findViewById(R.id.videoView);
		mVideoView = (MyVideoView) findViewById(R.id.videoView);

		TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
		titleTextView.setText(mVideo.title);

		ImageButton toggleButton = (ImageButton) findViewById(R.id.expandButton);
		toggleButton.setOnClickListener(mToggleClickListener);

		TextView descTextView = (TextView) findViewById(R.id.descTextView);
		descTextView.setText(mVideo.desc);

		TextView timestampTextView = (TextView) findViewById(R.id.addedTextView);
		timestampTextView.setText(Utils.timeAgoInWords(mVideo.timestamp, false));

		TextView viewCountTextView = (TextView) findViewById(R.id.viewCountTextView);
		viewCountTextView.setText(Utils.formatViewCount(mVideo.viewCount));

		/* View videoControls = findViewById(R.id.videoControlsContainer);
		mVideoController = new MyVideoController(videoControls, getSupportActionBar()); */

		ImageLoader imageLoader = ImageUtils.getInstance(this).getImageLoader();
		try {
			initRelatedVideo(mVideo.relatedItems.get(0), R.id.relatedVideo1, imageLoader);
			initRelatedVideo(mVideo.relatedItems.get(1), R.id.relatedVideo2, imageLoader);
			initRelatedVideo(mVideo.relatedItems.get(2), R.id.relatedVideo3, imageLoader);
			initRelatedVideo(mVideo.relatedItems.get(3), R.id.relatedVideo4, imageLoader);
		} catch (IndexOutOfBoundsException e) {

		}
	}

	private void initVideoPlayback() {
		LOG.i("initVideoPlayback");
		
		View videoControls = findViewById(R.id.videoControlsContainer);
		MyVideoController controller = new MyVideoController(
				videoControls, 
				getSupportActionBar());
		
		mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				/* // Set portrait
				if (isInLandscape()) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				} */

				//
				// Tu dat asi nejaky sleep nech sa clovek spamata
				//

				ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
				ObjectAnimator animator = ObjectAnimator.ofInt(scrollView, "scrollY", 470);
				animator.setDuration(1000);
				animator.start();

				// Restore
				// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
		});
		mVideoView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// mVideoController.toggle();
				// mUiHider2.toggle();
				/* mUiHider.toggleAppUi();
				if (isInLandscape()) {
					mUiHider.hideSystemUi();
				} */
				return false;
			}
		});
		mVideoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				LOG.d("onPrepared");
				
				if(mPausedAt == 0) {
					mVideoView.play();
					mVideoView.showMediaController();
				} else {
					LOG.d("NOPE");
				}
				
				// mVideoView.showMediaController();
				// mVideoController.show();
				// mUiHider2.hide();
			}
		});
		
		mVideoView.setVideoURI(Uri.parse(mVideo.videoUrl));
		mVideoView.setMediaController(controller);
		mVideoView.requestFocus();

		// mVideoController.setVideoControl(mVideoView);
	}

	private void initRelatedVideo(final RelatedVideo relatedItem, int layoutId, ImageLoader imageLoader) {
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
		timestampTextView.setText(Utils.timeAgoInWords(relatedItem.timestamp, false));
	}

	@Override
	public void onResume() {
		super.onResume();
		LOG.d("onResume: " + mPausedAt);

		if (mPausedAt != 0) {
			mVideoView.pause();
			mVideoView.seekTo(mPausedAt);
			// mVideoView.showMediaController(0);
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		LOG.d("onPause");

		if (mVideoView.isPlaying()) {
			mVideoView.pause();
			mVideoView.showMediaController(0);
			mPausedAt = mVideoView.getCurrentTime();
		}
	}

	private void handleOrientationChange(int orientation) {
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// ActionBar
			final ActionBar actionBar = getSupportActionBar();
			if (actionBar != null) {
				actionBar.setDisplayShowTitleEnabled(true);
				actionBar.setTitle(mVideo.title);
			}

			// VideoView
			ViewGroup container = (ViewGroup) findViewById(R.id.videoContainer);
			LayoutParams params = container.getLayoutParams();
			params.width = LayoutParams.MATCH_PARENT;
			params.height = LayoutParams.MATCH_PARENT;
			container.requestLayout();

			// Hiding
			// mUiHider.init();

		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			// ActionBar
			final ActionBar actionBar = getSupportActionBar();
			if (actionBar != null) {
				actionBar.setDisplayShowTitleEnabled(false);
			}

			// Calculate VideoView height
			Resources r = getResources();
			int screenWidth = r.getDisplayMetrics().widthPixels;
			mVideoViewHeight = (int) ((float) screenWidth / Utils.PRESUMED_VIDEO_WIDTH * Utils.PRESUMED_VIDEO_HEIGHT);

			// VideoView
			ViewGroup container = (ViewGroup) findViewById(R.id.videoContainer);
			LayoutParams params = container.getLayoutParams();
			params.width = LayoutParams.MATCH_PARENT;
			params.height = mVideoViewHeight;
			container.requestLayout();

			// Hiding
			// mUiHider.cancel();
		}
		
		// mUiHider2.onOrientationChanged(orientation);
		// mSystemUiManager.onOrientationChanged(orientation);
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

	private boolean isInLandscape() {
		int o = getResources().getConfiguration().orientation;
		return (o == Configuration.ORIENTATION_LANDSCAPE);
	}

	/* @Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("position", mVideoView.getCurrentPosition());
	} */

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		handleOrientationChange(newConfig.orientation);
		
		// Kvoli tomu bugu posuvaciemu glitchu grafickemu
		// mVideoView.showMediaController(750);
		mVideoView.showMediaController();
		
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
			TextView descTextView = (TextView) findViewById(R.id.descTextView);
			
			if (!mExpanded) {
				imageButton.setImageResource(R.drawable.collapse);
				descTextView.setMaxLines(1000);
				mExpanded = true;
			} else {
				imageButton.setImageResource(R.drawable.expand);
				descTextView.setMaxLines(2);
				mExpanded = false;
			}
		}
	};

	private Callback mRelatedVideoCallback = new Callback() {

		@Override
		public void onResult(int status, Bundle data) {
			switch (status) {
				case Status.RUNNING:
					break;

				case Status.OK:
					mVideo = data.getParcelable(RelatedVideoProcessor.RESULT_VIDEO);
					init();
					break;

				case Status.EXCEPTION:
					LOG.d("GetRelatedVideo # EXCEPTION");
					break;
			}
		}
	};

}
