package sk.ursus.lokaltv.ui;

import java.util.ArrayList;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.adapter.NewsFeedAdapter;
import sk.ursus.lokaltv.model.RelatedVideo;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.net.RestService;
import sk.ursus.lokaltv.net.lib.Callback;
import sk.ursus.lokaltv.net.processor.NewsFeedProcessor;
import sk.ursus.lokaltv.util.Utils;
import sk.ursus.lokaltv.util.VideosCache;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

public class NewsFeedFragment extends AbsFeedFragment {

	public static final String TAG = "news";

	public static NewsFeedFragment newInstance() {
		return new NewsFeedFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ArrayList<Video> videos = VideosCache.get(VideosCache.NEWS);
		if (videos == null) {
			videos = new ArrayList<Video>();
			if (savedInstanceState == null) {
				refresh();
			}
		}

		setAdapter(new NewsFeedAdapter(mContext, videos));
	}

	private void refresh() {
		RestService.getNewsFeed(mContext, mNewsFeedCallback);
	}

	private void playRandomEpisode() {
		String randomUrl = Utils.getRandomEpisodeUrl();
		RelatedVideo rv = new RelatedVideo(null, randomUrl, null, null);

		Intent intent = new Intent(mContext, VideoActivity.class)
				.setAction(VideoActivity.ACTION_FETCH_AND_PLAY)
				.putExtra(VideoActivity.EXTRA_RELATED_VIDEO, rv);

		startActivity(intent);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		startVideoActivity(mAdapter.getItem(position));
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_video_list, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_refresh:
				refresh();
				return true;

			case R.id.action_random:
				playRandomEpisode();
				return true;

			default:
				return false;
		}
	}

	private Callback mNewsFeedCallback = new Callback() {

		@Override
		public void onStarted() {
			showProgress();
		}

		@Override
		public void onSuccess(Bundle data) {
			hideProgress();

			// Display videos
			ArrayList<Video> newVideos = data.getParcelableArrayList(NewsFeedProcessor.RESULT_VIDEOS);
			mAdapter.clear();
			mAdapter.addAll(newVideos);

			// Cache results
			VideosCache.put(VideosCache.NEWS, newVideos);
		}

		@Override
		public void onException() {
			showError();
		}
	};

}