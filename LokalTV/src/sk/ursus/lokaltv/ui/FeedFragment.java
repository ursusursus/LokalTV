package sk.ursus.lokaltv.ui;

import java.util.ArrayList;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.adapter.FeedAdapter;
import sk.ursus.lokaltv.model.RelatedVideo;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.net.RestService;
import sk.ursus.lokaltv.net.ServerUtils;
import sk.ursus.lokaltv.net.ServerUtils.Status;
import sk.ursus.lokaltv.util.ImageUtils;
import sk.ursus.lokaltv.util.Utils;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
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

import com.android.volley.toolbox.ImageLoader;

public class FeedFragment extends Fragment implements OnItemClickListener {

	private Context mContext;
	private GridView mGridView;
	private TextView mErrorTextView;

	private ArrayList<Video> mFeedItems;
	private FeedAdapter mAdapter;

	private SearchView mSearchView;
	private ProgressBar mProgressBar;

	// private Menu mOptionsMenu;

	public static FeedFragment newInstance() {
		FeedFragment fragment = new FeedFragment();
		return fragment;
	}

	public FeedFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mContext = getActivity();

		if (savedInstanceState != null) {
			mFeedItems = savedInstanceState.getParcelableArrayList("feed");

		} else {
			mFeedItems = new ArrayList<Video>();
			refresh();
		}
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_feed, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		/* ListViewSwipeRefreshLayout layout = (ListViewSwipeRefreshLayout) view.findViewById(R.id.swipeLayout);
		layout.setColorScheme(R.color.blue, R.color.blue2, R.color.accent_blue, R.color.blue);
		layout.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				
			}
		}); */

		mErrorTextView = (TextView) view.findViewById(R.id.errorTextView);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

		mGridView = (GridView) view.findViewById(R.id.gridView);
		// mGridView.setEmptyView(dsadsa);
		mGridView.setOnItemClickListener(this);

		ImageLoader imageLoader = ImageUtils.getInstance(mContext).getImageLoader();

		mAdapter = new FeedAdapter(mContext, mFeedItems, imageLoader);
		mGridView.setAdapter(mAdapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		ActionBar actionBar = ((ActionBarActivity) mContext).getSupportActionBar();
		actionBar.setTitle(Utils.makeCustomFontTitle(mContext, getString(R.string.app_name)));
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		Video feedItem = (Video) mAdapter.getItem(position);

		Intent intent = new Intent(mContext, VideoActivity2.class);
		intent.setAction(VideoActivity2.ACTION_PLAY);
		intent.putExtra(VideoActivity2.EXTRA_VIDEO, feedItem);

		startActivity(intent);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList("feed", mFeedItems);
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

	private void playRandomEpisode() {
		String randomUrl = Utils.getRandomEpisodeUrl();
		RelatedVideo rv = new RelatedVideo(null, randomUrl, null, null);

		Intent intent = new Intent(mContext, VideoActivity2.class);
		intent.setAction(VideoActivity2.ACTION_FETCH_AND_PLAY);
		intent.putExtra(VideoActivity2.EXTRA_RELATED_VIDEO, rv);

		startActivity(intent);
	}

	private void refresh() {
		RestService.getFeed(mContext, mFeedCallback);
	}

	private ServerUtils.Callback mFeedCallback = new ServerUtils.Callback() {

		@Override
		public void onResult(int status, Bundle data) {
			switch (status) {
				case Status.RUNNING:
					mProgressBar.setVisibility(View.VISIBLE);
					// setRefreshButtonState(true);
					break;

				case Status.OK:
					// setRefreshButtonState(false);
					mProgressBar.setVisibility(View.GONE);

					mFeedItems = data.getParcelableArrayList("feed");
					mAdapter.clear();
					mAdapter.addAll(mFeedItems);
					// ???
					// mAdapter.notifyDataSetInvalidated();
					break;

				case Status.EXCEPTION:
					// setRefreshButtonState(false);
					mProgressBar.setVisibility(View.GONE);
					mErrorTextView.setVisibility(View.VISIBLE);
					break;
			}
		}

	};
	
	/**
     * Utility method to check whether a {@link ListView} can scroll up from it's current position.
     * Handles platform version differences, providing backwards compatible functionality where
     * needed.
     */
    private static boolean canListViewScrollUp(GridView gridView) {
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            // For ICS and above we can call canScrollVertically() to determine this
            return ViewCompat.canScrollVertically(gridView, -1);
        } else {
            // Pre-ICS we need to manually check the first visible item and the child view's top
            // value
            return gridView.getChildCount() > 0 &&
                    (gridView.getFirstVisiblePosition() > 0
                            || gridView.getChildAt(0).getTop() < gridView.getPaddingTop());
        }
    }
	
	private class ListViewSwipeRefreshLayout extends SwipeRefreshLayout {
		 
        public ListViewSwipeRefreshLayout(Context context) {
            super(context);
        }
 
        /**
         * As mentioned above, we need to override this method to properly signal when a
         * 'swipe-to-refresh' is possible.
         *
         * @return true if the {@link android.widget.ListView} is visible and can scroll up.
         */
        @Override
        public boolean canChildScrollUp() {
            if (mGridView.getVisibility() == View.VISIBLE) {
                return canListViewScrollUp(mGridView);
            } else {
                return false;
            }
        }
 
    }
 
}
