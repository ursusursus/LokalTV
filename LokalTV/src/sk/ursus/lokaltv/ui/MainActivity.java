package sk.ursus.lokaltv.ui;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.util.Utils;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

public class MainActivity extends ActionBarActivity {

	private static final int INIT_ID = -1;
	// private ArrayAdapter<Cathegory> mAdapter;
	private DrawerLayout mDrawerLayout;
	private ScrollView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private int mCurrentId = INIT_ID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		
		// Hello, github!
		
		// Init drawer stuff
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		mDrawerList = (ScrollView) findViewById(R.id.drawer);
		initDrawerList();

		mDrawerToggle = new ActionBarDrawerToggle(
				this,
				mDrawerLayout,
				R.drawable.ic_navigation_drawer,
				R.string.drawer_open,
				R.string.drawer_close
				) {
					@SuppressLint("NewApi")
					public void onDrawerClosed(View view) {
						// getSupportActionBar().setTitle("");
						// MainActivity.this.invalidateOptionsMenu();
					}

					@SuppressLint("NewApi")
					public void onDrawerOpened(View drawerView) {
						// getSupportActionBar().setTitle("Menu");
						// MainActivity.this.invalidateOptionsMenu();
					}
				};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState != null) {
			mCurrentId = savedInstanceState.getInt("current_id");
			setSelected(mCurrentId);
		} else {
			swapFragments(R.id.feedButton);
		}

	}

	private void initDrawerList() {
		((Button) findViewById(R.id.feedButton)).setOnClickListener(mDrawerClickListener);
		((Button) findViewById(R.id.serialEpisodesButton)).setOnClickListener(mDrawerClickListener);
		((Button) findViewById(R.id.serialSceensButton)).setOnClickListener(mDrawerClickListener);
		((Button) findViewById(R.id.channelDovolenkarisButton)).setOnClickListener(mDrawerClickListener);
		((Button) findViewById(R.id.channelLFBButton)).setOnClickListener(mDrawerClickListener);
		((Button) findViewById(R.id.channelLHNButton)).setOnClickListener(mDrawerClickListener);
		((Button) findViewById(R.id.channelNapalRytmausaButton)).setOnClickListener(mDrawerClickListener);
		((Button) findViewById(R.id.channelPistovinyButton)).setOnClickListener(mDrawerClickListener);
		((Button) findViewById(R.id.channelReneTalkshowButton)).setOnClickListener(mDrawerClickListener);
		((Button) findViewById(R.id.channelRobocotiButton)).setOnClickListener(mDrawerClickListener);
		((Button) findViewById(R.id.channelWilhelmButton)).setOnClickListener(mDrawerClickListener);
		((Button) findViewById(R.id.channelZapalRytmausaButton)).setOnClickListener(mDrawerClickListener);
		((Button) findViewById(R.id.extraBonusButton)).setOnClickListener(mDrawerClickListener);
		((Button) findViewById(R.id.extraVideoclipButton)).setOnClickListener(mDrawerClickListener);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mDrawerToggle.onOptionsItemSelected(item);
		/* if (item.getItemId() == android.R.id.home) {
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
		} */

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("current_id", mCurrentId);
	}

	private void swapFragments(int id) {
		if (id == mCurrentId) {
			mDrawerLayout.closeDrawer(mDrawerList);
			return;
		}

		setSelected(id);

		Fragment fragment;
		if(id == R.id.feedButton) {
			fragment = FeedFragment.newInstance();
		} else {
			fragment = VideoListFragment.newInstance(Utils.CATHEGORIES.get(id));
		}
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.container, fragment)
				.commit();

		mDrawerLayout.closeDrawer(mDrawerList);
		mCurrentId = id;
	}

	@SuppressLint("NewApi")
	private void setSelected(int id) {
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			return;
		}
		
		if (mCurrentId != INIT_ID) {
			findViewById(mCurrentId).setActivated(false);
		}
		findViewById(id).setActivated(true);
	}

	private View.OnClickListener mDrawerClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			swapFragments(v.getId());
		}
	};

}
