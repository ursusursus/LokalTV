package sk.ursus.lokaltv.video;

import java.io.IOException;
import java.util.Map;

import sk.ursus.lokaltv.video.MyVideoController.MyVideoControl;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.DecelerateInterpolator;

import com.awaboom.ursus.agave.LOG;

/**
 * Displays a video file. The VideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 */
// public class MyVideoView extends SurfaceView implements MyMediaPlayerControl
// {
public class MyVideoView extends SurfaceView implements MyVideoControl {
	private String TAG = "VideoView";

	// settable by the client
	private Uri mUri;
	private Map<String, String> mHeaders;

	// all possible internal states
	private static final int STATE_ERROR = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_PREPARING = 1;
	private static final int STATE_PREPARED = 2;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PAUSED = 4;
	private static final int STATE_PLAYBACK_COMPLETED = 5;

	// mCurrentState is a VideoView object's current state.
	// mTargetState is the state that a method caller intends to reach.
	// For instance, regardless the VideoView object's current state,
	// calling pause() intends to bring the object to a target state
	// of STATE_PAUSED.
	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;

	// All the stuff we need for playing and showing a video
	private SurfaceHolder mSurfaceHolder = null;
	private MediaPlayer mMediaPlayer = null;
	private int mAudioSession;
	private int mVideoWidth;
	private int mVideoHeight;
	private int mSurfaceWidth;
	private int mSurfaceHeight;

	private OnCompletionListener mOnCompletionListener;
	private OnPreparedListener mOnPreparedListener;
	private OnErrorListener mOnErrorListener;
	private OnInfoListener mOnInfoListener;
	// private onBufferingListener mOnBufferingListener;

	private Context mContext;
	private int mSeekWhenPrepared;
	private int mCurrentBufferPercentage;

	private MyVideoController mVideoController;
	private SystemUiHider mSystemUiHider;
	private float mPresumedVideoWidth;
	private float mPresumedVideoHeight;

	/* public interface onBufferingListener {
		void onBufferingStarted();

		void onBufferingEnded();
	} */

	public MyVideoView(Context context) {
		super(context);
		initVideoView(context);
	}

	public MyVideoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		initVideoView(context);
	}

	public MyVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initVideoView(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Log.i("@@@@", "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) +
		// ", "
		// + MeasureSpec.toString(heightMeasureSpec) + ")");

		int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
		int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
		if (mVideoWidth > 0 && mVideoHeight > 0) {

			int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
			int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
			int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
			int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

			if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
				// the size is fixed
				width = widthSpecSize;
				height = heightSpecSize;

				// for compatibility, we adjust size based on aspect ratio
				if (mVideoWidth * height < width * mVideoHeight) {
					// Log.i("@@@", "image too wide, correcting");
					width = height * mVideoWidth / mVideoHeight;
				} else if (mVideoWidth * height > width * mVideoHeight) {
					// Log.i("@@@", "image too tall, correcting");
					height = width * mVideoHeight / mVideoWidth;
				}
			} else if (widthSpecMode == MeasureSpec.EXACTLY) {
				// only the width is fixed, adjust the height to match aspect
				// ratio if possible
				width = widthSpecSize;
				height = width * mVideoHeight / mVideoWidth;
				if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
					// couldn't match aspect ratio within the constraints
					height = heightSpecSize;
				}
			} else if (heightSpecMode == MeasureSpec.EXACTLY) {
				// only the height is fixed, adjust the width to match aspect
				// ratio if possible
				height = heightSpecSize;
				width = height * mVideoWidth / mVideoHeight;
				if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
					// couldn't match aspect ratio within the constraints
					width = widthSpecSize;
				}
			} else {
				// neither the width nor the height are fixed, try to use actual
				// video size
				width = mVideoWidth;
				height = mVideoHeight;
				if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
					// too tall, decrease both width and height
					height = heightSpecSize;
					width = height * mVideoWidth / mVideoHeight;
				}
				if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
					// too wide, decrease both width and height
					width = widthSpecSize;
					height = width * mVideoHeight / mVideoWidth;
				}
			}
		} else {
			// no size yet, just adopt the given spec sizes
		}

		setMeasuredDimension(width, height);
	}

	/* public int resolveAdjustedSize(int desiredSize, int measureSpec) {
		return getDefaultSize(desiredSize, measureSpec);
	} */

	private void initVideoView(Context context) {
		mContext = context;
		mVideoWidth = 0;
		mVideoHeight = 0;
		getHolder().addCallback(mSHCallback);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		mCurrentState = STATE_IDLE;
		mTargetState = STATE_IDLE;
	}

	public void setVideoPath(String path) {
		setVideoURI(Uri.parse(path));
	}

	public void setVideoURI(Uri uri) {
		setVideoURI(uri, null);
	}

	public void setMediaController(MyVideoController controller) {
		if (controller != null) {
			controller.setVideoControl(this);
			controller.setVisibilityChangedListener(mVideoControllerVisibilityListener);
		}
		mVideoController = controller;
	}

	public void setSystenUiHider(SystemUiHider hider) {
		if (hider != null) {
			hider.setVisibilityChangedListener(mSysUiVisibilityListener);
		}
		mSystemUiHider = hider;
	}

	public void setVideoDimensions(float width, float height) {
		mPresumedVideoWidth = width;
		mPresumedVideoHeight = height;
	}

	public void setVideoURI(Uri uri, Map<String, String> headers) {
		mUri = uri;
		mHeaders = headers;
		mSeekWhenPrepared = 0;
		openVideo();
		requestLayout();
		invalidate();
	}

	public void stopPlayback() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			mTargetState = STATE_IDLE;
		}
	}

	private void openVideo() {
		LOG.d("openVideo");

		if (mUri == null || mSurfaceHolder == null) {
			// Not ready for playback just yet, will try again later
			return;
		}

		// Tell the music playback service to pause
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		mContext.sendBroadcast(i);

		LOG.i("openVideo");

		// We shouldn't clear the target state, because somebody might have
		// called start() previously
		release(false);
		try {
			mMediaPlayer = new MediaPlayer();

			if (mAudioSession != 0) {
				mMediaPlayer.setAudioSessionId(mAudioSession);
			} else {
				mAudioSession = mMediaPlayer.getAudioSessionId();
			}
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnInfoListener(mInfoListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mCurrentBufferPercentage = 0;
			// mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
			mMediaPlayer.setDataSource(mContext, mUri);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync();

			// JA
			/* if (mOnBufferingListener != null) {
				mOnBufferingListener.onBufferingStarted();
			} */
			if (mVideoController != null) {
				mVideoController.showInit();
				mVideoController.showProgressBar();
			}
			// END JA

			// we don't set the target state here either, but preserve the
			// target state that was there before.
			mCurrentState = STATE_PREPARING;
			// attachMediaController();
		} catch (IOException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} catch (IllegalArgumentException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		}
	}

	MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
			new MediaPlayer.OnVideoSizeChangedListener() {
				public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
					mVideoWidth = mp.getVideoWidth();
					mVideoHeight = mp.getVideoHeight();
					if (mVideoWidth != 0 && mVideoHeight != 0) {
						getHolder().setFixedSize(mVideoWidth, mVideoHeight);
						requestLayout();
					}
				}
			};

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {			
			mCurrentState = STATE_PREPARED;
			
			// Unset black layer
			setBackgroundColor(getResources().getColor(android.R.color.transparent));

			if (mVideoController != null) {
				mVideoController.hideInit();
				mVideoController.hideProgressBar();
			}

			if (mOnPreparedListener != null) {
				mOnPreparedListener.onPrepared(mMediaPlayer);
			}
			/* if (mMediaController != null) {
				mMediaController.setEnabled(true);
			} */
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();

			int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared may be
													// changed after seekTo()
													// call
			if (seekToPosition != 0) {
				seekTo(seekToPosition);
			}

			LOG.d("onPrepared # Target State: " + mTargetState);

			if (mVideoWidth != 0 && mVideoHeight != 0) {
				Log.i("@@@@", "video size: " + mVideoWidth + "/" + mVideoHeight);
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
				if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
					// We didn't actually change the size (it was already at the
					// size
					// we need), so we won't get a "surface changed" callback,
					// so
					// start the video here instead of in the callback.
					if (mTargetState == STATE_PLAYING) {
						play();
						/* if (mMediaController != null) {
							mMediaController.show();
						} */
						LOG.d("THISSS");
						if (mVideoController != null) {
							mVideoController.show();
						}
					} else if (!isPlaying() && (seekToPosition != 0 || getCurrentTime() > 0)) {
						/* if (mMediaController != null) {
							// Show the media controls when we're paused into a video and make 'em stick.
							mMediaController.show(0);
						} */
						LOG.d("THIS");
						if (mVideoController != null) {
							mVideoController.show(0, true);
						}
					}
				}
			} else {
				// We don't know the video size yet, but should start anyway.
				// The video size might be reported to us later.
				if (mTargetState == STATE_PLAYING) {
					play();
				}
			}
		}
	};

	private MediaPlayer.OnInfoListener mInfoListener = new OnInfoListener() {

		@Override
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
				LOG.d("Buffering started");
				// Toto niekedy neni ukazane lebo sa to dajak
				// zajebe s tou visibilitou v MyVideoControlleri,
				// ale tento callback je spravne volany

				if (mVideoController != null) {
					mVideoController.showProgressBar();
				}

			} else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
				LOG.d("Buffering ended");

				if (mVideoController != null) {
					mVideoController.hideProgressBar();
				}
			}

			if (mOnInfoListener != null) {
				mOnInfoListener.onInfo(mp, what, extra);
			}
			return false;
		}
	};

	private MediaPlayer.OnCompletionListener mCompletionListener =
			new MediaPlayer.OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {
					mCurrentState = STATE_PLAYBACK_COMPLETED;
					mTargetState = STATE_PLAYBACK_COMPLETED;
					/* if (mMediaController != null) {
						mMediaController.hide();
					} */
					if (mVideoController != null) {
						mVideoController.show(0, true);
					}
					if (mOnCompletionListener != null) {
						mOnCompletionListener.onCompletion(mMediaPlayer);
					}
				}
			};

	private MediaPlayer.OnErrorListener mErrorListener =
			new MediaPlayer.OnErrorListener() {
				public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
					Log.d(TAG, "Error: " + framework_err + "," + impl_err);
					mCurrentState = STATE_ERROR;
					mTargetState = STATE_ERROR;
					/* if (mMediaController != null) {
						mMediaController.hide();
					} */
					if (mVideoController != null) {
						mVideoController.show(0, true);
					}

					/* If an error handler has been supplied, use it and finish. */
					if (mOnErrorListener != null) {
						if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
							return true;
						}
					}

					/* Otherwise, pop up an error dialog so the user knows that
					 * something bad has happened. Only try and pop up the dialog
					 * if we're attached to a window. When we're going away and no
					 * longer have a window, don't bother showing the user an error.
					 */
					if (getWindowToken() != null) {
						Resources r = mContext.getResources();
						int messageId;

						/*if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
						    messageId = com.android.internal.R.string.VideoView_error_text_invalid_progressive_playback;
						} else {
						    messageId = com.android.internal.R.string.VideoView_error_text_unknown;
						} */

						new AlertDialog.Builder(mContext)
								// .setMessage()
								.setMessage("Popsulo sa to")
								// .setPositiveButton(com.android.internal.R.string.VideoView_error_button,
								.setPositiveButton("Zruöiù",
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int whichButton) {
												/* If we get here, there is no onError listener, so
												 * at least inform them that the video is over.
												 */
												if (mOnCompletionListener != null) {
													mOnCompletionListener.onCompletion(mMediaPlayer);
												}
											}
										})
								.setCancelable(false)
								.show();
					}
					return true;
				}
			};

	private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
			new MediaPlayer.OnBufferingUpdateListener() {
				public void onBufferingUpdate(MediaPlayer mp, int percent) {
					mCurrentBufferPercentage = percent;
				}
			};

	/* public void setOnBufferingStartedListener(onBufferingListener l) {
		mOnBufferingListener = l;
	} */

	/**
	 * Register a callback to be invoked when the media file is loaded and ready
	 * to go.
	 * 
	 * @param l
	 *        The callback that will be run
	 */
	public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	/**
	 * Register a callback to be invoked when the end of a media file has been
	 * reached during playback.
	 * 
	 * @param l
	 *        The callback that will be run
	 */
	public void setOnCompletionListener(OnCompletionListener l) {
		mOnCompletionListener = l;
	}

	/**
	 * Register a callback to be invoked when an error occurs during playback or
	 * setup. If no listener is specified, or if the listener returned false,
	 * VideoView will inform the user of any errors.
	 * 
	 * @param l
	 *        The callback that will be run
	 */
	public void setOnErrorListener(OnErrorListener l) {
		mOnErrorListener = l;
	}

	/**
	 * Register a callback to be invoked when an informational event occurs
	 * during playback or setup.
	 * 
	 * @param l
	 *        The callback that will be run
	 */
	public void setOnInfoListener(OnInfoListener l) {
		mOnInfoListener = l;
	}

	/*
	 * release the media player in any state
	 */
	private void release(boolean clearTargetState) {
		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			if (clearTargetState) {
				mTargetState = STATE_IDLE;
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		/*if (isInPlaybackState() && mMediaController != null) {
			toggleMediaControlsVisiblity();
		} */
		if (mVideoController != null) {
			mVideoController.toggle();
		}
		return false;
	}

	public void wrapHeight() {
		Resources r = getResources();
		int screenWidth = r.getDisplayMetrics().widthPixels;
		int videoViewHeight = (int) ((float) screenWidth / mPresumedVideoWidth * mPresumedVideoHeight);

		ViewGroup container = (ViewGroup) getParent();
		LayoutParams params = container.getLayoutParams();
		params.width = LayoutParams.MATCH_PARENT;
		params.height = videoViewHeight;
		container.requestLayout();

		if (mSystemUiHider != null) {
			mSystemUiHider.cancel();
		}
	}

	public void wrapHeightAndWidth() {
		// For tablets
	}

	public void goFullscreen() {
		ViewGroup container = (ViewGroup) getParent();
		LayoutParams params = container.getLayoutParams();
		params.width = LayoutParams.MATCH_PARENT;
		params.height = LayoutParams.MATCH_PARENT;
		container.requestLayout();

		if (mSystemUiHider != null) {
			mSystemUiHider.init();
		}
	}

	public void showControls(int delay) {
		showVideoController(delay);
		showSystemUi();
	}

	public void showControls() {
		showVideoController();
		showSystemUi();
	}

	public void hideControls() {
		hideVideoController();
		hideSystemUi();
	}

	private void hideVideoController() {
		if (mVideoController != null) {
			mVideoController.hide();
		}
	}

	private void showVideoController() {
		if (mVideoController != null) {
			mVideoController.show();
		}
	}

	private void showVideoController(int delay) {
		if (mVideoController != null) {
			mVideoController.show(delay, true);
		}
	}

	private void showSystemUi() {
		if (mSystemUiHider != null) {
			mSystemUiHider.show();
		}
	}

	private void hideSystemUi() {
		if (mSystemUiHider != null) {
			mSystemUiHider.hide();
		}
	}

	@Override
	public void play() {
		if (isInPlaybackState()) {
			mMediaPlayer.start();
			mCurrentState = STATE_PLAYING;
		}
		mTargetState = STATE_PLAYING;
	}

	@Override
	public void pause() {
		if (isInPlaybackState()) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				mCurrentState = STATE_PAUSED;
			}
		}
		mTargetState = STATE_PAUSED;
	}

	public void suspend() {
		release(false);
	}

	/* public void resume() {
		openVideo();
	} */

	@Override
	public int getTotalTime() {
		if (isInPlaybackState()) {
			return mMediaPlayer.getDuration();
		}

		return -1;
	}

	@Override
	public int getCurrentTime() {
		if (isInPlaybackState()) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	@Override
	public void seekTo(int msec) {
		if (isInPlaybackState()) {
			mMediaPlayer.seekTo(msec);
			mSeekWhenPrepared = 0;
		} else {
			mSeekWhenPrepared = msec;
		}
	}

	@Override
	public boolean isPlaying() {
		return isInPlaybackState() && mMediaPlayer.isPlaying();
	}

	@Override
	public int getBufferPercentage() {
		if (mMediaPlayer != null) {
			return mCurrentBufferPercentage;
		}
		return 0;
	}

	private boolean isInPlaybackState() {
		return (mMediaPlayer != null &&
				mCurrentState != STATE_ERROR &&
				mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
	}

	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			mSurfaceWidth = w;
			mSurfaceHeight = h;
			boolean isValidState = (mTargetState == STATE_PLAYING);
			boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
			if (mMediaPlayer != null && isValidState && hasValidSize) {
				if (mSeekWhenPrepared != 0) {
					seekTo(mSeekWhenPrepared);
				}
				play();
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			mSurfaceHolder = holder;
			openVideo();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			LOG.d("surfaceDestroyed");
			// Set black protective layer so VideoView
			// doesnt go transparent when moving away
			// from activity, etc.
			setBackgroundColor(getResources().getColor(android.R.color.black));

			// after we return from this we can't use the surface any more
			mSurfaceHolder = null;
			/* if (mMediaController != null)
				mMediaController.hide(); */
			release(true);
		}
	};

	VisibilityChangedListener mVideoControllerVisibilityListener =
			new VisibilityChangedListener() {

				@Override
				public void onVisibilityChanged(boolean isVisible) {
					if (mSystemUiHider != null) {
						if (isVisible) {
							mSystemUiHider.show();
						} else {
							mSystemUiHider.hide();
						}
					}
				}
			};

	VisibilityChangedListener mSysUiVisibilityListener =
			new VisibilityChangedListener() {

				@Override
				public void onVisibilityChanged(boolean isVisible) {
					if (mVideoController != null && isVisible) {
						mVideoController.show(false);
					}
				}
			};

}
