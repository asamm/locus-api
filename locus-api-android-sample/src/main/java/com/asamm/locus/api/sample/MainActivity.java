package com.asamm.locus.api.sample;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.asamm.locus.api.sample.pages.PagePointsFragment;
import com.asamm.locus.api.sample.pages.PageTracksFragment;
import com.asamm.locus.api.sample.pages.PageUtilsFragment;
import com.asamm.locus.api.sample.pages.PageWelcomeFragment;
import com.asamm.locus.api.sample.receivers.MainActivityIntentHandler;
import com.asamm.locus.api.sample.utils.BasicAdapter;
import com.asamm.locus.api.sample.utils.BasicAdapterItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import locus.api.utils.Logger;

public class MainActivity extends AppCompatActivity {

	// tag for logger
	private static final String TAG = "MainActivity";

	public static final int ITEM_ID_WELCOME	 									= 0;
	public static final int ITEM_ID_POINTS                              		= 1;
	public static final int ITEM_ID_TRACKS                                   	= 2;
	public static final int ITEM_ID_UTILS	 									= 3;

	// ID of fragment container
	private static final int FRAGMENT_CONTAINER_ID = R.id.frame_layout_main_container;

	// remember the position of the selected item.
	private static final String KEY_I_SELECTED_ITEM_ID = "KEY_I_SELECTED_ITEM_ID";

	// top toolbar
	private ActionBar mToolbar;
	// drawer itself
	private DrawerLayout mDrawerLayout;
	// toggle drawer
	private ActionBarDrawerToggle mToggle;
	// ID of currently selected item
	private int mCurrentSelectedItemId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// set content
		setContentView(R.layout.activity_main);

		// setup toolbar
		if (!initializeToolbar()) {
			finish();
			return;
		}

		// initialize drawer menu
		enableDrawer();

		// restore state
		mCurrentSelectedItemId = PreferenceManager.getDefaultSharedPreferences(this).
				getInt(KEY_I_SELECTED_ITEM_ID, ITEM_ID_WELCOME);
		setFragment(mCurrentSelectedItemId);

        // finally check intent that started this sample
        MainActivityIntentHandler.handleStartIntent(this, getIntent());
    }

	/**
	 * Initialize top toolbar.
	 * @return {@code true} if toolbar is correctly initialized
	 */
	private boolean initializeToolbar() {
		setSupportActionBar((Toolbar)
				findViewById(R.id.toolbar_top));
		mToolbar = getSupportActionBar();
		if (mToolbar == null) {
			Logger.logD(TAG, "initializeToolbar(), " +
					"problem with initializing of toolbar");
			return false;
		}

		// set basics
		mToolbar.setTitle("Sample");
		mToolbar.setDisplayOptions(
				ActionBar.DISPLAY_SHOW_HOME|
						ActionBar.DISPLAY_HOME_AS_UP|
						ActionBar.DISPLAY_SHOW_TITLE);
		mToolbar.setDisplayHomeAsUpEnabled(true);
		mToolbar.setHomeButtonEnabled(true);

		// add went well
		return true;
	}

	/**
	 * Enable whole drawer system for current activity. This method should be called in end of
	 * onCreate method to have drawer ready as soon as possible.
	 */
	private void enableDrawer() {
		// initialize drawer and drawer toggle button
		mDrawerLayout = (DrawerLayout)
				findViewById(R.id.drawer_layout);
		FrameLayout drawerContent = (FrameLayout)
				findViewById(R.id.frame_layout_drawer);

		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);

		// set change listener
		mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {

			@Override
			public void onDrawerStateChanged(int arg0) {}

			@Override
			public void onDrawerSlide(View arg0, float arg1) {}

			@Override
			public void onDrawerOpened(View drawerView) {
				// refresh action bar
				supportInvalidateOptionsMenu();
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				// refresh action bar
				supportInvalidateOptionsMenu();
			}
		});

		// setup drawer icon
		mToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.string.app_name, R.string.app_name);
		mDrawerLayout.addDrawerListener(mToggle);

		// refresh content
		setContentDrawer(drawerContent);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mToggle.onOptionsItemSelected(item)) {
			return true;
		} else if (item.getItemId() == R.id.menu_settings) {
			Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
			return true;
		}

		// Handle your other action bar items...
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Set content of drawer itself.
	 */
	private void setContentDrawer(final FrameLayout drawerContainer) {
		final List<BasicAdapterItem> items = new ArrayList<>();
		items.add(new BasicAdapterItem(ITEM_ID_WELCOME,
				getTitleById(ITEM_ID_WELCOME)));
		items.add(new BasicAdapterItem(ITEM_ID_POINTS,
				getTitleById(ITEM_ID_POINTS)));
		items.add(new BasicAdapterItem(ITEM_ID_TRACKS,
				getTitleById(ITEM_ID_TRACKS)));
		items.add(new BasicAdapterItem(ITEM_ID_UTILS,
				getTitleById(ITEM_ID_UTILS)));

		ListView lv = new ListView(this);
		BasicAdapter adapter = new BasicAdapter(this, items);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mCurrentSelectedItemId = items.get(position).id;

				// refresh content
				setFragment(mCurrentSelectedItemId);
				mDrawerLayout.closeDrawers();

				// store value
				PreferenceManager.getDefaultSharedPreferences(MainActivity.this).
						edit().putInt(KEY_I_SELECTED_ITEM_ID, mCurrentSelectedItemId).apply();

			}
		});
		drawerContainer.addView(lv);
	}

	/**
	 * Display certain item.
	 * @param itemId ID of selected item
	 */
	private void setFragment(int itemId) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().
				replace(FRAGMENT_CONTAINER_ID, getContentById(itemId)).
				commit();

		// set page title
		mToolbar.setTitle(getTitleById(itemId));

		// refresh toolbar
		supportInvalidateOptionsMenu();
	}

	/**
	 * Get new content for certain selected item.
	 * @param itemId selected item ID
	 * @return prepared fragment to display
	 */
	private Fragment getContentById(int itemId) {
		switch (itemId) {
			case ITEM_ID_WELCOME:
				return new PageWelcomeFragment();
			case ITEM_ID_POINTS:
				return new PagePointsFragment();
			case ITEM_ID_TRACKS:
				return new PageTracksFragment();
			case ITEM_ID_UTILS:
				return new PageUtilsFragment();
			default:
				// should not happen
				return null;
		}
	}

	/**
	 * Get title for certain page.
	 * @param itemId ID of item
	 * @return title for certain page
	 */
	private CharSequence getTitleById(int itemId) {
		switch (itemId) {
			case ITEM_ID_WELCOME:
				return "Welcome page";
			case ITEM_ID_POINTS:
				return "Points";
			case ITEM_ID_TRACKS:
				return "Tracks";
			case ITEM_ID_UTILS:
				return "Utils";
			default:
				return "";
		}
	}

    /**************************************************/
    // BASIC TOOLS
    /**************************************************/
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
    	if (requestCode == 0) { 
    		// pick file
    		if (resultCode == RESULT_OK && data != null) {
    			File file = new File(data.getData().getPath());
    			Toast.makeText(this, "Process successful\n\nFile:" + file.getAbsolutePath() + 
    					", exists:" + file.exists(), Toast.LENGTH_LONG).show();
    		} else {
    			Toast.makeText(this, "Process unsuccessful", Toast.LENGTH_SHORT).show();
    		}
    	}
    	
    	else if (requestCode == 1) { 
    		// pick directory
    		if (resultCode == RESULT_OK && data != null) {
    			File dir = new File(data.getData().getPath());
    			Toast.makeText(this, "Process successful\n\nDir:" + dir.getName() + 
    					", exists:" + dir.exists(), Toast.LENGTH_LONG).show();
    		} else {
    			Toast.makeText(this, "Process unsuccessful", Toast.LENGTH_SHORT).show();
    		}
    	}
    }
}
