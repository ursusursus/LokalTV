package sk.ursus.lokaltv.ui;

import java.util.ArrayList;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.adapter.FeedAdapter;
import sk.ursus.lokaltv.adapter.FeedAdapter.OnListNearEndListener;
import sk.ursus.lokaltv.model.Category;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.net.RestService;
import sk.ursus.lokaltv.net.ServerUtils;
import sk.ursus.lokaltv.net.ServerUtils.Status;
import sk.ursus.lokaltv.util.ImageManager;
import sk.ursus.lokaltv.util.SoundManager;
import sk.ursus.lokaltv.util.Utils;
import sk.ursus.lokaltv.util.VideosCache;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.awaboom.ursus.agave.LOG;

public class VideoListFragment extends Fragment implements OnItemClickListener {

	protected static final int FULL_PAGE_SIZE = 10;
	protected static final int FIRST_PAGE = 1;
	private Context mContext;
	private GridView mGridView;
	private TextView mErrorTextView;

	private ArrayList<Video> mFeedItems;
	private FeedAdapter mAdapter;

	private SearchView mSearchView;
	private ProgressBar mProgressBar;

	private int mCurrentPage;
	protected boolean mNearEndListenerDisabled;

	// private Menu mOptionsMenu;

	/* public static VideoListFragment newInstance(Category cathegory) {
		Bundle args = new Bundle();
		args.putString("title", cathegory.title);
		args.putString("url", cathegory.url);

		VideoListFragment fragment = new VideoListFragment();
		fragment.setArguments(args);

		return fragment;
	} */
	
	public static VideoListFragment newInstance(String categoryUrl) {
		Bundle args = new Bundle();
		args.putString("url", categoryUrl);

		VideoListFragment fragment = new VideoListFragment();
		fragment.setArguments(args);

		return fragment;
	}

	public VideoListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mContext = getActivity();

		if (savedInstanceState != null) {
			// mFeedItems = savedInstanceState.getParcelableArrayList("feed");
			mCurrentPage = savedInstanceState.getInt("page");
			mNearEndListenerDisabled = savedInstanceState.getBoolean("near_end_disabled");
			// Este persistovat stav downloadu by sa patrilo
		} else {
			// mFeedItems = new ArrayList<Video>();
			mCurrentPage = FIRST_PAGE;
			mNearEndListenerDisabled = false;

			// fetchVideos();
		}

		ArrayList<Video> videos = VideosCache.get(getArguments().getString("url"));
		if (videos == null) {
			mFeedItems = new ArrayList<Video>();
			fetchVideos();
		} else {
			mFeedItems = videos;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_feed, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mErrorTextView = (TextView) view.findViewById(R.id.errorTextView);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

		mGridView = (GridView) view.findViewById(R.id.gridView);
		mGridView.setOnItemClickListener(this);

		// ImageLoader imageLoader = ImageManager.getInstance(mContext).getImageLoader();

		// mAdapter = new FeedAdapter(mContext, mFeedItems, imageLoader);
		mAdapter = new FeedAdapter(mContext, mFeedItems);
		if (!mNearEndListenerDisabled) {
			mAdapter.setOnListNearEndListener(mOnNearEndListener);
		}
		mGridView.setAdapter(mAdapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// ActionBar actionBar = getActivity().getActionBar();
		// actionBar.setTitle(Utils.makeCustomFontTitle(mContext, getArguments().getString("title")));
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
		Video feedItem = (Video) mAdapter.getItem(position);

		Intent intent = new Intent(mContext, VideoActivity.class);
		intent.setAction(VideoActivity.ACTION_PLAY);
		intent.putExtra(VideoActivity.EXTRA_VIDEO, feedItem);

		startActivity(intent);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList("feed", mFeedItems);
		outState.putInt("page", mCurrentPage);
		outState.putBoolean("near_end_disabled", mNearEndListenerDisabled);
	}

	private void fetchVideos() {
		String cathegoryUrl = getArguments().getString("url");
		RestService.getVideos(mContext, cathegoryUrl, mCurrentPage, mFeedCallback);
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
			mAdapter.setOnListNearEndListener(mOnNearEndListener);
			mNearEndListenerDisabled = false;
		}
	}

	/**
	 * Zobrazí ProgressBar v ActionBare
	 */
	protected void showProgressBar() {
		if (mFeedItems.size() <= 0) {
			mProgressBar.setVisibility(View.VISIBLE);

		} else {
			getView().findViewById(R.id.moreProgressBar).setVisibility(View.VISIBLE);
			/* FragmentActivity activity = getActivity();
			if (activity != null) {
				activity.setProgressBarIndeterminateVisibility(true);
			} */
		}

	}

	/**
	 * Skryje ProgressBar v ActionBare
	 */
	protected void hideProgressBar() {
		if (mFeedItems.size() <= 0) {
			mProgressBar.setVisibility(View.GONE);
			
		} else {
			getView().findViewById(R.id.moreProgressBar).setVisibility(View.GONE);
			/* FragmentActivity activity = getActivity();
			if (activity != null) {
				activity.setProgressBarIndeterminateVisibility(false);
			} */
		}
	}

	private OnListNearEndListener mOnNearEndListener = new OnListNearEndListener() {

		@Override
		public void onListNearEnd() {
			loadMore();
		};
	};

	private OnQueryTextListener mSearchViewListener = new OnQueryTextListener() {

		@Override
		public boolean onQueryTextSubmit(String query) {
			Toast.makeText(mContext, "You have searched for: " + query, Toast.LENGTH_SHORT).show();
			return false;
		}

		@Override
		public boolean onQueryTextChange(String query) {
			return false;
		}
	};

	private ServerUtils.Callback mFeedCallback = new ServerUtils.Callback() {

		@Override
		public void onResult(int status, Bundle data) {
			switch (status) {
				case Status.RUNNING:
					showProgressBar();
					break;

				case Status.OK:
					hideProgressBar();

					// Get new feed items from results
					ArrayList<Video> newFeedItems = data.getParcelableArrayList("feed");
					LOG.d("new items : " + newFeedItems.size());
					if (newFeedItems.size() < FULL_PAGE_SIZE) {
						// Remove onLoadMore listener
						// so we don't load more when on list's end
						mNearEndListenerDisabled = true;
						mAdapter.setOnListNearEndListener(null);
					}

					boolean isFromLoadMore = data.getBoolean("from_load_more", false);
					if (isFromLoadMore) {
						// If this is from second page etc
						// just add them
						mFeedItems.addAll(newFeedItems);
					} else {
						// This is first page
						mFeedItems = newFeedItems;
						mAdapter.clear();
					}
					// Put new stuff to adapter
					mAdapter.addAll(newFeedItems);

					// Cache results
					VideosCache.put(getArguments().getString("url"), mFeedItems);
					break;

				case Status.EXCEPTION:
					hideProgressBar();
					mErrorTextView.setVisibility(View.VISIBLE);
					break;
			}
		}

	};
}
