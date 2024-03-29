package sk.ursus.lokaltv.ui;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.model.RelatedVideo;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.net.RestService;
import sk.ursus.lokaltv.net.lib.Callback;
import sk.ursus.lokaltv.net.processor.RelatedVideoProcessor;
import sk.ursus.lokaltv.util.TypefaceUtils;
import sk.ursus.lokaltv.util.Utils;
import sk.ursus.lokaltv.video.MyVideoController;
import sk.ursus.lokaltv.video.MyVideoView;
import sk.ursus.lokaltv.video.SystemUiHider;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.awaboom.ursus.agave.LOG;
import com.squareup.picasso.Picasso;

public class VideoActivity extends FragmentActivity {

	public static final String ACTION_PLAY = "sk.ursus.lokaltv.ACTION_PLAY";
	public static final String ACTION_FETCH_AND_PLAY = "sk.ursus.lokaltv.ACTION_FETCH_AND_PLAY";

	public static final String EXTRA_RELATED_VIDEO = "related_video";
	public static final String EXTRA_VIDEO = "feed_item";

	private MyVideoView mVideoView;
	private Video mVideo;

	private int mPausedAt = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_3);

		Intent intent = getIntent();
		String action = intent.getAction();
		if (action.equals(ACTION_FETCH_AND_PLAY)) {
			RelatedVideo relatedVideo = intent.getParcelableExtra(EXTRA_RELATED_VIDEO);
			RestService.getRelatedVideo(this, relatedVideo.url, mRelatedVideoCallback);

		} else if (action.equals(ACTION_PLAY)) {
			mVideo = (Video) getIntent().getParcelableExtra(EXTRA_VIDEO);
			init();
		}

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	private void init() {
		initViews();
		initVideoPlayback();

		if (mVideo != null) {
			getActionBar().setTitle(Utils.makeCustomFontTitle(this, mVideo.title));
		}
		// Init UI orientation
		int o = getResources().getConfiguration().orientation;
		handleOrientationChange(o);
	}

	private void initViews() {
		mVideoView = (MyVideoView) findViewById(R.id.videoView);

		TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
		titleTextView.setTypeface(TypefaceUtils.get(this, TypefaceUtils.ROBOTO_SLAB_REGULAR));
		titleTextView.setText(mVideo.title);

		ImageButton toggleButton = (ImageButton) findViewById(R.id.expandButton);
		toggleButton.setOnClickListener(mToggleClickListener);

		TextView descTextView = (TextView) findViewById(R.id.descTextView);
		descTextView.setText(mVideo.desc);

		TextView timestampTextView = (TextView) findViewById(R.id.addedTextView);
		timestampTextView.setText(Utils.timeAgoInWords(mVideo.timestamp, false));

		TextView viewCountTextView = (TextView) findViewById(R.id.viewCountTextView);
		viewCountTextView.setText(Utils.formatViewCount(mVideo.viewCount));

		/* final View relatedVideosContainer = findViewById(R.id.nextContainer1);
		ViewTreeObserver observer = relatedVideosContainer.getViewTreeObserver();
		observer.addOnPreDrawListener(new OnPreDrawListener() {

			@Override
			public boolean onPreDraw() {
				relatedVideosContainer.getViewTreeObserver().removeOnPreDrawListener(this);

				relatedVideosContainer.setTranslationX(200F);
				// relatedVideosContainer.setVisibility(View.VISIBLE);
				// relatedVideosContainer.setAlpha(0F);
				relatedVideosContainer.animate()
						.translationX(0F)
						// .alpha(1F)
						.setDuration(600L)
						.setInterpolator(new DecelerateInterpolator());

				return true;
			}
		}); */

		try {
			initRelatedVideo(mVideo.relatedItems.get(0), R.id.relatedVideo1);
			initRelatedVideo(mVideo.relatedItems.get(1), R.id.relatedVideo2);
			initRelatedVideo(mVideo.relatedItems.get(2), R.id.relatedVideo3);
			initRelatedVideo(mVideo.relatedItems.get(3), R.id.relatedVideo4);
		} catch (IndexOutOfBoundsException e) {

		}
	}

	private void initVideoPlayback() {
		LOG.i("initVideoPlayback");

		// VideoController
		View videoControls = findViewById(R.id.videoControlsContainer);
		MyVideoController controller = new MyVideoController(videoControls, getActionBar());

		// SystemUiHider
		SystemUiHider uiHider = new SystemUiHider(getWindow().getDecorView());

		// VideoView
		// Pre ten bug priesvitneho pozadia pri vracani sa do aktivity
		// mVideoView.setBackgroundColor(0xFF000000);
		// mVideoView.setBackgroundResource(R.drawable.pistoviny);
		mVideoView.setVideoURI(Uri.parse(mVideo.videoUrl));
		mVideoView.setMediaController(controller);
		mVideoView.setSystenUiHider(uiHider);
		mVideoView.setVideoDimensions(Utils.PRESUMED_VIDEO_WIDTH, Utils.PRESUMED_VIDEO_HEIGHT);
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
		mVideoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				LOG.d("onPrepared");
				// mVideoView.setBackground(null);
				if (mPausedAt == 0) {
					mVideoView.play();
					mVideoView.showControls();
				} else {
					LOG.d("NOPE");
				}
			}
		});
		mVideoView.requestFocus();
	}

	private void initRelatedVideo(final RelatedVideo relatedItem, int layoutId) {
		View view = findViewById(layoutId);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(VideoActivity.this, VideoActivity.class);
				intent.setAction(VideoActivity.ACTION_FETCH_AND_PLAY);
				intent.putExtra(VideoActivity.EXTRA_RELATED_VIDEO, relatedItem);

				startActivity(intent);
			}
		});

		/* NetworkImageView imageView = (NetworkImageView) view.findViewById(R.id.imageView);
		imageView.setShouldAnimate(true);
		imageView.setImageUrl(relatedItem.imageUrl, imageLoader); */
		ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
		Picasso.with(this)
				.load(relatedItem.imageUrl)
				.placeholder(R.drawable.placeholder)
				.into(imageView);

		TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
		titleTextView.setText(relatedItem.title);

		TextView timestampTextView = (TextView) view.findViewById(R.id.timestampTextView);
		timestampTextView.setText(Utils.timeAgoInWords(relatedItem.timestamp, false));
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_right);
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
			mVideoView.showControls(0);
			mPausedAt = mVideoView.getCurrentTime();
		}
	}

	private void handleOrientationChange(int orientation) {
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			getActionBar().setDisplayShowTitleEnabled(true);
			mVideoView.goFullscreen();

		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			getActionBar().setDisplayShowTitleEnabled(false);
			mVideoView.wrapHeight();
		}
	}

	/* private boolean isInLandscape() {
		int o = getResources().getConfiguration().orientation;
		return (o == Configuration.ORIENTATION_LANDSCAPE);
	} */

	/* @Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("position", mVideoView.getCurrentPosition());
	} */

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		handleOrientationChange(newConfig.orientation);
		mVideoView.showControls();
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
				Utils.share(this, mVideo.url);
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

	private Callback mRelatedVideoCallback = new sk.ursus.lokaltv.net.lib.Callback() {

		@Override
		public void onSuccess(Bundle data) {
			mVideo = data.getParcelable(RelatedVideoProcessor.RESULT_VIDEO);
			init();

		}

		@Override
		public void onException() {
			LOG.d("GetRelatedVideo # EXCEPTION");
		}
	};

}
