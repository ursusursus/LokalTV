package sk.ursus.lokaltv.ui;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.adapter.DrawerAdapter;
import sk.ursus.lokaltv.model.Category;
import sk.ursus.lokaltv.model.DrawerItem;
import sk.ursus.lokaltv.util.SystemBarTintManager;
import sk.ursus.lokaltv.util.Utils;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends FragmentActivity {

	private static final String KEY_POSITION = "position";
	private static final String KEY_TITLE = "title";

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;

	private DrawerItem[] mDrawerItems;
	private DrawerAdapter mAdapter;
	private ListView mListView;

	private int mCurrentPosition = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main_2);

		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setTintColor(getResources().getColor(R.color.blue_transparent));

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		initDrawer();
		initDrawerContent();

		if (savedInstanceState != null) {
			mCurrentPosition = savedInstanceState.getInt(KEY_POSITION);
			setCustomTitle(savedInstanceState.getCharSequence(KEY_TITLE), false);
		} else {
			swapFragments(0);
		}
	}

	private void initDrawer() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		mDrawerToggle = new ActionBarDrawerToggle(
				this,
				mDrawerLayout,
				R.drawable.ic_navigation_drawer,
				R.string.drawer_open,
				R.string.drawer_close
				) {
					@Override
					public void onDrawerClosed(View view) {
						setCustomTitle(mDrawerItems[mCurrentPosition].title, true);
					}

					@Override
					public void onDrawerOpened(View drawerView) {
						setCustomTitle(getTitle(), true);
					}
				};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void initDrawerContent() {
		mDrawerItems = new DrawerItem[] {
				new Category("Èo je nové", ""),
				new DrawerItem("Seriál"),
				new Category("Epizódy", "epizody"),
				new Category("Vystrihnuté scény", "vystrihnute-sceny"),
				new DrawerItem("Lokal kanál"),
				new Category("Trampoty pána Menežerisa", "trampoty-pana-menezerisa"),
				new Category("Napál Rytmausa", "napal-rytmausa"),
				new Category("Lokal Freestyle Battle", "lokal-freestyle-battle"),
				new Category("Dovolenkáris", "dovolenkaris"),
				new Category("Reného talkshow", "reneho-talkshow"),
				new Category("Lokal HotNews", "lokal-hotnews"),
				new Category("Zapál Rytmausa", "zapal-rytmausa"),
				new Category("RoboCoti", "robocoti"),
				new Category("Wilhelm & Bob", "wilhelm-bob"),
				new Category("Pištoviny", "pistoviny"),
				new DrawerItem("Pištoviny"),
				new Category("Bonusy", "bonusy"),
				new Category("Videoklipy", "videoklipy")
		};

		// Adapter
		mAdapter = new DrawerAdapter(this, mDrawerItems);

		// ListView
		mListView = (ListView) findViewById(R.id.listView);
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mListView.setOnItemClickListener(mItemClickListener);
		mListView.setAdapter(mAdapter);
	}

	protected void swapFragments(int position) {
		if (position == mCurrentPosition) {
			return;
		}

		Fragment f;
		if (position == 0) {
			f = FeedFragment.newInstance();
		} else {
			Category category = (Category) mDrawerItems[position];
			f = VideoListFragment.newInstance(category.url);
		}

		getSupportFragmentManager()
				.beginTransaction()
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.replace(R.id.container, f)
				.commit();

		setCustomTitle(mDrawerItems[position].title, false);
		mListView.setItemChecked(position, true);
		mCurrentPosition = position;
	}

	private void setCustomTitle(CharSequence title, boolean invalidate) {
		if (!TextUtils.equals(title, getActionBar().getTitle())) {
			SpannableString customTitle = Utils.makeCustomFontTitle(this, title.toString());
			getActionBar().setTitle(customTitle);
			
			if (invalidate) {
				invalidateOptionsMenu();
			}
		}
	}

	private void toggleDrawer() {
		if (!mDrawerLayout.isDrawerOpen(Gravity.START)) {
			mDrawerLayout.openDrawer(Gravity.START);
		} else {
			mDrawerLayout.closeDrawer(Gravity.START);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(Gravity.START);
		// menu.findItem(R.id.action_search).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_POSITION, mCurrentPosition);
		outState.putCharSequence(KEY_TITLE, getActionBar().getTitle());
	}

	private OnItemClickListener mItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
			swapFragments(position);
			toggleDrawer();
		}
	};

	/* private void swapFragments(int id) {
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
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
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
	}; */

	/* private void initDrawerList() {
	((Button) findViewById(R.id.feedButton)).setOnClickListener(mDrawerClickListener);
	((Button) findViewById(R.id.serialEpisodesButton)).setOnClickListener(mDrawerClickListener);
	((Button) findViewById(R.id.serialSceensButton)).setOnClickListener(mDrawerClickListener);
	((Button) findViewById(R.id.channelMenezerisButton)).setOnClickListener(mDrawerClickListener);
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
	} */

}
