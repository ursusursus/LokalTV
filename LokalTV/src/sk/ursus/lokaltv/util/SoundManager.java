package sk.ursus.lokaltv.util;

import sk.ursus.lokaltv.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.preference.PreferenceManager;

import com.awaboom.ursus.agave.LOG;

public class SoundManager {

	private static SoundManager sInstance;

	private SoundPool mSoundPool;
	private int[] mSounds;
	private boolean mLoaded;

	private AudioManager mAudioManager;

	private int mIndex;

	private Context mContext;

	public static SoundManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new SoundManager(context.getApplicationContext());
		}
		return sInstance;
	}

	private SoundManager(Context context) {
		mContext = context;
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		/* mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {

			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				mLoaded = true;
			}
		});
		mSounds = new int[] {
				mSoundPool.load(context, R.raw.gdojepan, 1),
				mSoundPool.load(context, R.raw.jasompan, 1),
		}; */

	}

	public void playRandomSound() {
		if (mIndex >= mSounds.length) {
			mIndex = 0;
		}
		playSound(mSounds[mIndex]);
		mIndex++;
	}

	public void playGodJePan() {
		playSound(R.raw.gdojepan);
	}

	public void playJaSomPan() {
		playSound(mSounds[1]);
	}

	/* private void playSound(int sound) {

		// Getting the user sound settings
		float actualVolume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		float maxVolume = (float) mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volume = actualVolume / maxVolume;

		// Is the sound loaded already?
		if (mLoaded) {
			mSoundPool.play(sound, volume, volume, 1, 0, 1f);
		}
	} */
	private void playSound(int sound) {
		MediaPlayer.create(mContext, sound).start();
		
		/* final AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		SoundPool soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundPool.load(mContext, sound, 1);
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {

			@Override
			public void onLoadComplete(SoundPool soundPool, int soundId, int arg2) {
				float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				float volume = actualVolume / maxVolume;

				soundPool.play(soundId, 1F, 1F, 1, 0, 1f);
				// soundPool.release();
			}
		}); */
	}

	/**
	 * KEDY TOTO RELEASNEM?
	 */
	public void release() {
		mLoaded = false;

		if (mSoundPool != null) {
			mSoundPool.release();
			mSoundPool = null;
		}
	}

}
