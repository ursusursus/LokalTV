package sk.ursus.lokaltv.ui;

import java.util.ArrayList;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.adapter.NewsFeedAdapter;
import sk.ursus.lokaltv.model.RelatedVideo;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.net.RestService;
import sk.ursus.lokaltv.net.ServerUtils;
import sk.ursus.lokaltv.net.ServerUtils.Status;
import sk.ursus.lokaltv.util.Utils;
import sk.ursus.lokaltv.util.VideosCache;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

public class NewsFragment extends AbsFeedFragment {

	private static final String KEY_VIDEOS = "feed";

	public static NewsFragment newInstance() {
		return new NewsFragment();
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
			refresh();
		}

		setAdapter(new NewsFeedAdapter(mContext, videos));
	}

	private void refresh() {
		RestService.getFeed(mContext, mFeedCallback);
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

	private ServerUtils.Callback mFeedCallback = new ServerUtils.Callback() {

		@Override
		public void onResult(int status, Bundle data) {
			switch (status) {
				case Status.RUNNING:
					showProgress();
					break;

				case Status.OK:
					hideProgress();

					// Display videos
					ArrayList<Video> newVideos = data.getParcelableArrayList(KEY_VIDEOS);
					// mVideos = newVideos;
					mAdapter.clear();
					mAdapter.addAll(newVideos);

					// Cache results
					VideosCache.put(VideosCache.NEWS, newVideos);
					break;

				case Status.EXCEPTION:
					showError();
					break;
			}
		}

	};

}