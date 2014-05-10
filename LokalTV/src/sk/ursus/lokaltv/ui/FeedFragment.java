package sk.ursus.lokaltv.ui;

import java.util.ArrayList;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.adapter.FeedAdapter;
import sk.ursus.lokaltv.model.RelatedVideo;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.net.RestService;
import sk.ursus.lokaltv.net.ServerUtils;
import sk.ursus.lokaltv.net.ServerUtils.Status;
import sk.ursus.lokaltv.util.ImageManager;
import sk.ursus.lokaltv.util.Utils;
import android.app.ActionBar;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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

		mErrorTextView = (TextView) view.findViewById(R.id.errorTextView);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

		mGridView = (GridView) view.findViewById(R.id.gridView);
		// mGridView.setEmptyView(dsadsa);
		mGridView.setOnItemClickListener(this);

		ImageLoader imageLoader = ImageManager.getInstance(mContext).getImageLoader();

		mAdapter = new FeedAdapter(mContext, mFeedItems, imageLoader);
		mGridView.setAdapter(mAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		Video feedItem = (Video) mAdapter.getItem(position);

		Intent intent = new Intent(mContext, VideoActivity.class);
		intent.setAction(VideoActivity.ACTION_PLAY);
		intent.putExtra(VideoActivity.EXTRA_VIDEO, feedItem);

		Bundle translationBundle = ActivityOptions.makeCustomAnimation(mContext, R.anim.slide_in_left,
				R.anim.slide_out_left).toBundle();

		ActivityCompat.startActivity(getActivity(), intent, translationBundle);
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

		Intent intent = new Intent(mContext, VideoActivity.class);
		intent.setAction(VideoActivity.ACTION_FETCH_AND_PLAY);
		intent.putExtra(VideoActivity.EXTRA_RELATED_VIDEO, rv);

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

}