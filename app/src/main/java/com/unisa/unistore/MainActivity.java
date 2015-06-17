package com.unisa.unistore;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.ParseUser;
import com.unisa.unistore.adapter.NavDrawerListAdapter;
import com.unisa.unistore.model.NavDrawerItem;
import com.unisa.unistore.utilities.Utilities;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    private static final String EXTRA_IMAGE = "DetailActivity:image";
    private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;
    private Toolbar toolbar;
    private ViewGroup mRootView;
    private View card_view;


    private CardView bookInfoCardView;
    private boolean clickFlag = false;
    private ActionBar supportActionBar;

    private boolean firstVisit = true;
    private ParseUser currentUser;


    protected void setActionBarIcon(int iconRes) {
        toolbar.setNavigationIcon(iconRes);
    }

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentUser = ParseUser.getCurrentUser();

        mRootView = (ViewGroup) findViewById(R.id.layout_root_view);

        toolbar = (Toolbar) findViewById(R.id.fragment_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            supportActionBar = getSupportActionBar();
            supportActionBar.setDisplayShowTitleEnabled(false);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_menu);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		// Home
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
		// Find People
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        // I miei annunci
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        // Agenda
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1), true, "22"));

        if(Utilities.isUserAuthenticated())
            // Logout
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
        else
            // Login
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));


		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);

		mDrawerToggle = new CustomActionBarDrawerToggle(this, mDrawerLayout);
		mDrawerLayout.setDrawerListener(mDrawerToggle);

        mRootView = (ViewGroup) findViewById(R.id.layout_root_view);
        mRootView.setOnClickListener(this);

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(0);
        }

	}

    @Override
    protected void onStart() {
        super.onStart();

        if(firstVisit && Utilities.isUserAuthenticated()) {
            Context context = getApplicationContext();
            CharSequence text = getString(R.string.welcome) + " " +
                    currentUser.getString("name");
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            firstVisit = false;
        }
    }

    /**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		switch (position) {
		case 0:
			fragment = new HomeFragment();
            //((HomeFragment)fragment).setToolbar(toolbar);
            ((HomeFragment)fragment).setToolbar(supportActionBar);
			break;
		case 1:
			fragment = new FindPeopleFragment();
			break;
        case 2:
            fragment = new VisualizzaFragment();
            break;
        case 3:
            fragment = new AgendaFragment();
			break;
        case 4:
            if(Utilities.isUserAuthenticated()) {
                ParseUser.logOut();
                currentUser = null;

                navDrawerItems.remove(4);
                // Login
                navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));

                // setting the nav drawer list adapter
                adapter = new NavDrawerListAdapter(getApplicationContext(),
                        navDrawerItems);
                mDrawerList.setAdapter(adapter);

                Context context = getApplicationContext();
                CharSequence text = getString(R.string.logout);
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } else {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

			break;
		default:
			break;
		}

		if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.layout_root_view, fragment)
                    // Add this t.the front of the card.
                    .addToBackStack(null)
                    .commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

    @Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		if(getActionBar() != null)
            getActionBar().setTitle(mTitle);
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);


        // When using the support library, the setOnActionExpandListener() method is
        // static and accepts the MenuItem object as an argument
        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Do something when collapsed
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do something when expanded
                return true;  // Return true to expand action view
            }
        });
        return false;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        /*
        if(!clickFlag) {
            bookInfoCardView = (CardView) findViewById(R.id.card_view_book_info);

            TransitionManager.beginDelayedTransition(mRootView, new Fade());

            Fragment fragment = new VisualizzaFragment();
            if (fragment != null) {
    /*
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity, transitionView, DetailActivity.EXTRA_IMAGE);
                ActivityCompat.startActivity(activity, new Intent(activity, DetailActivity.class),
                        options.toBundle());
                */
        /*
                mDrawerToggle.setDrawerIndicatorEnabled(false);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.layout_root_view, fragment)
                                // Add this t.the front of the card.
                        .addToBackStack(null)
                        .commit();

                if(bookInfoCardView != null)
                    toggleVisibility(bookInfoCardView);
                clickFlag = true;
            } else {
                // error in creating fragment
                Log.e("MainActivity", "Error in creating fragment");
            }
        }
        */
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getFragmentManager().popBackStack();
        // code here to show dialog
    }

    private static void toggleVisibility(View... views) {
        for (View view : views) {
            boolean isVisible = view.getVisibility() == View.VISIBLE;
            view.setVisibility(isVisible ? View.INVISIBLE : View.VISIBLE);
        }
    }

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

    private class CustomActionBarDrawerToggle extends ActionBarDrawerToggle {

        public CustomActionBarDrawerToggle(Activity mActivity,
                                           DrawerLayout mDrawerLayout) {
            super(mActivity, mDrawerLayout,
                    R.string.menu_open, R.string.menu_close);
        }

        @Override
        public void onDrawerClosed(View view) {
            toolbar.setTitle(getString(R.string.menu_close));
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            toolbar.setTitle(getString(R.string.menu_open));
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

    }


}
