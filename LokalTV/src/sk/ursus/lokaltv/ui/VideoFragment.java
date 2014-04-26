package sk.ursus.lokaltv.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.SystemUiHider;
import sk.ursus.lokaltv.model.RelatedVideo;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.util.ImageUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.awaboom.ursus.agave.LOG;

public class VideoFragment extends Fragment {

	protected static final int AUTO_HIDE_DELAY_MILLIS = 2500;

	private Context mContext;
	private VideoView mVideoView;
	private SystemUiHider mUiHider;

	private Handler mHideHandler = new Handler();
	private Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mUiHider.hide();
		}
	};

	private ProgressBar mProgressBar;
	private Video mFeedItem;
	private boolean mShouldResume = false;

	private TextView mDescTextView;

	private ActionBar mActionBar;

	public static VideoFragment newInstance(Video feedItem) {
		Bundle args = new Bundle();
		args.putParcelable("feed_item", feedItem);

		VideoFragment fragment = new VideoFragment();
		fragment.setArguments(args);

		return fragment;
	}

	public VideoFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LOG.d("onCreate");
		setHasOptionsMenu(true);
		// Porozmyslat co s tym v tabletoch Lebo,
		// ked som v telefone, wrapper je VideoActivity,
		// a ta ked je destroynuta tak mi ani setRetainInstance(true)
		// nepomoze, ale musia sa ignorovat configChanges v manifeste
		// Ale v tablete, wrapper bude ina aktivita, a tam asi bude treba
		// setRetainInstance(true);

		// setRetainInstance(true);
		mContext = getActivity();
		mFeedItem = (Video) getArguments().getParcelable("feed_item");

		mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		/* for(RelatedItem item : mFeedItem.relatedItems) {
			LOG.d("--- RelatedItem ---\n" + item.toString());
		} */
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_video, container, false);
	}

	/**
	 * Uz sa to neonaci pri rotacii, ale ked odidem a vratim sa naspat, tak restartne sa video
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		LOG.d("onActivityCreated");

		mVideoView.setVideoURI(Uri.parse(mFeedItem.videoUrl));
		mVideoView.setMediaController(new MediaController(mContext));
		mVideoView.requestFocus();
		mVideoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				LOG.d("onPrepared");

				mProgressBar.setVisibility(View.GONE);
				mVideoView.start();
			}
		});

		if (savedInstanceState != null) {
			int savedPosition = savedInstanceState.getInt("position");
			mVideoView.seekTo(savedPosition);
		}

		mUiHider = new SystemUiHider(getActivity(), getView(), SystemUiHider.FLAG_HIDE_NAVIGATION);
		mUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {

			@Override
			public void onVisibilityChanged(boolean isVisible) {
				if (isVisible) {
					delayedHide(AUTO_HIDE_DELAY_MILLIS);
				}
			}
		});


		int orientation = getResources().getConfiguration().orientation;
		initActionBar(orientation);

	}

	@Override
	public void onResume() {
		super.onResume();
		LOG.d("onResume");

		if (mShouldResume) {
			LOG.d("Resuming...");
			mVideoView.resume();
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		LOG.d("onPause");

		if (mVideoView.isPlaying()) {
			LOG.d("Pausing...");
			mVideoView.pause();
			mShouldResume = true;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LOG.d("onDestroy");
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		LOG.d("onAttach");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		LOG.d("onDetach");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		LOG.d("onDestroyView");
	}

	private void initActionBar(int orientation) {
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mActionBar.setTitle(mFeedItem.title);
			delayedHide(200);
		} else {
			mActionBar.setDisplayShowTitleEnabled(false);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mVideoView = (VideoView) view.findViewById(R.id.videoView);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

		TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
		titleTextView.setText(mFeedItem.title);

		ImageButton toggleButton = (ImageButton) view.findViewById(R.id.expandButton);
		toggleButton.setOnClickListener(mToggleClickListener);

		mDescTextView = (TextView) view.findViewById(R.id.descTextView);
		mDescTextView.setText(mFeedItem.desc);

		TextView timestampTextView = (TextView) view.findViewById(R.id.addedTextView);
		SimpleDateFormat dateParser = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
		try {
			Date dateAdded = dateParser.parse(mFeedItem.timestamp);
			CharSequence timestampInWords = DateUtils.getRelativeTimeSpanString(
					dateAdded.getTime(),
					System.currentTimeMillis(),
					DateUtils.SECOND_IN_MILLIS);

			timestampTextView.setText(timestampInWords.toString());
		} catch (ParseException e) {
			timestampTextView.setText(mFeedItem.timestamp);
		}

		TextView viewCountTextView = (TextView) view.findViewById(R.id.viewCountTextView);
		viewCountTextView.setText(mFeedItem.viewCount + " videnÌ");

		ImageLoader imageLoader = ImageUtils.getInstance(mContext).getImageLoader();
		try {
			initRelatedVideo(mFeedItem.relatedItems.get(0), view, R.id.relatedVideo1, imageLoader, dateParser);
			initRelatedVideo(mFeedItem.relatedItems.get(1), view, R.id.relatedVideo2, imageLoader, dateParser);
			initRelatedVideo(mFeedItem.relatedItems.get(2), view, R.id.relatedVideo3, imageLoader, dateParser);
			initRelatedVideo(mFeedItem.relatedItems.get(3), view, R.id.relatedVideo4, imageLoader, dateParser);
		} catch (IndexOutOfBoundsException e) {

		}
	}

	private void initRelatedVideo(RelatedVideo relatedItem, View parent, int layoutId, ImageLoader imageLoader,
			SimpleDateFormat dateParser) {
		View view = parent.findViewById(layoutId);

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
			timestampTextView.setText(mFeedItem.timestamp);
		}
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
		
		initActionBar(newConfig.orientation);
	}

	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_video, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().finish();
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

	private void share() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, mFeedItem.url);
		intent.setType("text/plain");

		Intent chooser = Intent.createChooser(intent, "Zdielaù cez...");
		startActivity(chooser);
	}

	private OnClickListener mToggleClickListener = new OnClickListener() {

		private boolean isExpanded = false;

		@Override
		public void onClick(View v) {
			ImageButton imageButton = (ImageButton) v;
			if (!isExpanded) {
				imageButton.setImageResource(R.drawable.collapse);
				mDescTextView.setMaxLines(1000);
				isExpanded = true;
			} else {
				imageButton.setImageResource(R.drawable.expand);
				mDescTextView.setMaxLines(2);
				isExpanded = false;
			}
		}
	};

}
