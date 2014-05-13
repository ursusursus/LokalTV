package sk.ursus.lokaltv.ui;

import java.util.ArrayList;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.adapter.FeedAdapter;
import sk.ursus.lokaltv.adapter.FeedAdapter.OnListNearEndListener;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.net.RestService;
import sk.ursus.lokaltv.net.lib.Callback;
import sk.ursus.lokaltv.net.processor.FeedProcessor;
import sk.ursus.lokaltv.util.VideosCache;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

public class FeedFragment extends AbsFeedFragment {

	private static final String KEY_LISTENER_DISABLED = "near_end";
	private static final String KEY_PAGE = "page";

	private static final String ARG_URL = "url";

	protected static final int FULL_PAGE_SIZE = 10;
	protected static final int FIRST_PAGE = 1;

	private int mCurrentPage = FIRST_PAGE;
	protected boolean mNearEndListenerDisabled = false;

	public static FeedFragment newInstance(String categoryUrl) {
		Bundle args = new Bundle();
		args.putString(ARG_URL, categoryUrl);

		FeedFragment fragment = new FeedFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if (savedInstanceState != null) {
			mNearEndListenerDisabled = savedInstanceState.getBoolean(KEY_LISTENER_DISABLED);
			mCurrentPage = savedInstanceState.getInt(KEY_PAGE);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ArrayList<Video> videos = VideosCache.get(getArguments().getString(ARG_URL));
		if (videos == null) {
			videos = new ArrayList<Video>();
			if (savedInstanceState == null) {
				// Pozor ked pocas downloadu urobim rotaciu
				// tak sa mi nerefreshne adapter, aj ked onSuccess
				// sa zavola a vsetko fajn
				fetchVideos();
			}
		}
		mVideos = videos;

		FeedAdapter adapter = new FeedAdapter(mContext, videos);
		if (!mNearEndListenerDisabled) {
			adapter.setOnListNearEndListener(mOnNearEndListener);
		}
		setAdapter(adapter);
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

			default:
				return false;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		startVideoActivity(mAdapter.getItem(position));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_LISTENER_DISABLED, mNearEndListenerDisabled);
		outState.putInt(KEY_PAGE, mCurrentPage);
	}

	private void fetchVideos() {
		String cathegoryUrl = getArguments().getString(ARG_URL);
		RestService.getFeed(mContext, cathegoryUrl, mCurrentPage, mFeedCallback);
	}

	private void loadMore() {
		mCurrentPage++;
		fetchVideos();
	}

	private void refresh() {
		// SoundManager.getInstance(mContext).playGodJePan();
		mCurrentPage = FIRST_PAGE;
		fetchVideos();

		if (mNearEndListenerDisabled) {
			((FeedAdapter) mAdapter).setOnListNearEndListener(mOnNearEndListener);
			mNearEndListenerDisabled = false;
		}
	}

	private OnListNearEndListener mOnNearEndListener = new OnListNearEndListener() {

		@Override
		public void onListNearEnd() {
			loadMore();
		};
	};

	private Callback mFeedCallback = new Callback() {

		@Override
		public void onStarted() {
			showProgress();
		}

		@Override
		public void onSuccess(Bundle data) {
			hideProgress();

			// Get new videos from results
			ArrayList<Video> newVideos = data.getParcelableArrayList(FeedProcessor.RESULT_VIDEOS);
			if (newVideos.size() < FULL_PAGE_SIZE) {
				// Remove onLoadMore listener
				((FeedAdapter) mAdapter).setOnListNearEndListener(null);
				mNearEndListenerDisabled = true;
			}

			boolean isFromLoadMore = data.getBoolean(FeedProcessor.RESULT_VIDEOS, false);
			if (isFromLoadMore) {
				// Append videos
				mVideos.addAll(newVideos);
				mAdapter.addAll(newVideos);
			} else {
				// Display new
				mVideos = newVideos;
				mAdapter.clear();
				mAdapter.addAll(mVideos);
			}

			// Cache results
			VideosCache.put(getArguments().getString(ARG_URL), mVideos);
		}

		@Override
		public void onException() {
			showError();
		}

	};

}
