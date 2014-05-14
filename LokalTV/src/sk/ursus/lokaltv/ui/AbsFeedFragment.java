package sk.ursus.lokaltv.ui;

import java.util.ArrayList;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.model.Video;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class AbsFeedFragment extends Fragment implements OnItemClickListener {

	protected Context mContext;

	protected TextView mErrorTextView;
	protected ProgressBar mProgressBar;
	
	protected ArrayList<Video> mVideos;
	protected ArrayAdapter<Video> mAdapter;
	protected GridView mGridView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_feed, container, false);
		mErrorTextView = (TextView) view.findViewById(R.id.errorTextView);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

		mGridView = (GridView) view.findViewById(R.id.gridView);
		mGridView.setOnItemClickListener(this);

		return view;
	}

	protected void setAdapter(ArrayAdapter<Video> adapter) {
		mAdapter = adapter;
		mGridView.setAdapter(adapter);
	}

	protected void startVideoActivity(Video video) {
		Intent intent = new Intent(mContext, VideoActivity.class)
				.setAction(VideoActivity.ACTION_PLAY)
				.putExtra(VideoActivity.EXTRA_VIDEO, video);

		Bundle translationBundle = ActivityOptions.makeCustomAnimation(
				mContext,
				R.anim.slide_in_left,
				R.anim.slide_out_left).toBundle();

		ActivityCompat.startActivity(getActivity(), intent, translationBundle);
	}
	
	protected void showProgress() {
		mErrorTextView.setVisibility(View.GONE);
		if (mAdapter.isEmpty()) {
			mProgressBar.setVisibility(View.VISIBLE);
		} else {
			getView().findViewById(R.id.moreProgressBar).setVisibility(View.VISIBLE);
			/* FragmentActivity activity = getActivity();
			if (activity != null) {
				activity.setProgressBarIndeterminateVisibility(true);
			} */
		}
	}

	protected void hideProgress() {
		mErrorTextView.setVisibility(View.GONE);
		if (mAdapter.isEmpty()) {
			mProgressBar.setVisibility(View.GONE);
		} else {
			getView().findViewById(R.id.moreProgressBar).setVisibility(View.GONE);
		}
	}

	protected void showError() {
		mProgressBar.setVisibility(View.GONE);
		mErrorTextView.setVisibility(View.VISIBLE);
	}
}
