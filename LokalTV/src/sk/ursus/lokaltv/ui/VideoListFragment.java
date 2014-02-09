package sk.ursus.lokaltv.ui;

import java.util.ArrayList;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.adapter.FeedAdapter;
import sk.ursus.lokaltv.adapter.FeedAdapter.OnListNearEndListener;
import sk.ursus.lokaltv.model.Cathegory;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.net.RestService;
import sk.ursus.lokaltv.net.ServerUtils;
import sk.ursus.lokaltv.net.ServerUtils.Status;
import sk.ursus.lokaltv.util.ImageUtils;
import sk.ursus.lokaltv.util.LOG;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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

public class VideoListFragment extends Fragment implements OnItemClickListener {

	protected static final int FULL_PAGE_SIZE = 10;
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

	public static VideoListFragment newInstance(Cathegory cathegory) {
		Bundle args = new Bundle();
		args.putString("title", cathegory.title);
		args.putString("url", cathegory.url);

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
			mFeedItems = savedInstanceState.getParcelableArrayList("feed");
			mCurrentPage = savedInstanceState.getInt("page");
			mNearEndListenerDisabled = savedInstanceState.getBoolean("near_end_disabled");
			// Este persistovat stav downloadu by sa patrilo
		} else {
			mFeedItems = new ArrayList<Video>();
			mCurrentPage = 1;
			mNearEndListenerDisabled = false;
			
			refresh();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_feed, container, false);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_video_list, menu);

		/* mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		mSearchView.setQueryHint("Vyhæad·vaù vo vide·ch...");
		mSearchView.setOnQueryTextListener(mSearchViewListener); */
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

	private void refresh() {
		String cathegoryUrl = getArguments().getString("url");
		RestService.getVideos(mContext, cathegoryUrl, mCurrentPage, mFeedCallback);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mErrorTextView = (TextView) view.findViewById(R.id.errorTextView);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

		mGridView = (GridView) view.findViewById(R.id.gridView);
		mGridView.setOnItemClickListener(this);

		ImageLoader imageLoader = ImageUtils.getInstance(mContext).getImageLoader();

		mAdapter = new FeedAdapter(mContext, mFeedItems, imageLoader);
		if(!mNearEndListenerDisabled) {
			mAdapter.setOnListNearEndListener(mOnNearEndListener);			
		}
		mGridView.setAdapter(mAdapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ActionBar actionBar = ((ActionBarActivity) mContext).getSupportActionBar();
		actionBar.setTitle(getArguments().getString("title"));
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		Video feedItem = (Video) mAdapter.getItem(position);

		Intent intent = new Intent(mContext, VideoActivity2.class);
		intent.putExtra("feed_item", feedItem);
		
		startActivity(intent);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList("feed", mFeedItems);
		outState.putInt("page", mCurrentPage);
		outState.putBoolean("near_end_disabled", mNearEndListenerDisabled);
	}
	
	private OnListNearEndListener mOnNearEndListener = new OnListNearEndListener() {
		
		@Override
		public void onListNearEnd() {
			mCurrentPage++;
			refresh();
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
					mProgressBar.setVisibility(View.VISIBLE);
					// setRefreshButtonState(true);
					break;

				case Status.OK:
					// setRefreshButtonState(false);
					mProgressBar.setVisibility(View.GONE);

					// Get new feed items from results
					ArrayList<Video> newFeedItems = data.getParcelableArrayList("feed");
					
					if(newFeedItems.size() < FULL_PAGE_SIZE) {
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
					}
					// Put new stuff to adapter
					mAdapter.addAll(newFeedItems);
					// mAdapter.notifyDataSetInvalidated(); ???
					break;

				case Status.EXCEPTION:
					// setRefreshButtonState(false);
					mProgressBar.setVisibility(View.GONE);
					mErrorTextView.setVisibility(View.VISIBLE);
					break;
			}
		}

	};
}
