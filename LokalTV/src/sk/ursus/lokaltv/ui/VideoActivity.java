package sk.ursus.lokaltv.ui;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.util.LOG;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class VideoActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
		
		LOG.d("VIDEOACTIVITY 1");

		Intent intent = getIntent();
		Video feedItem = (Video) intent.getParcelableExtra("feed_item");

		/* final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			actionBar.setTitle(feedItem.title);
		} else {
			actionBar.setDisplayShowTitleEnabled(false);
		} */

		if (savedInstanceState == null) {
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.container, VideoFragment.newInstance(feedItem))
					.commit();
		}

	}

	/* @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	} */

}
