package com.unisa.unistore;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ui.ParseOnLoginSuccessListener;
import com.unisa.unistore.model.ListaAnnunci;
import com.unisa.unistore.utilities.ParseUtilities;
import com.unisa.unistore.utilities.Utilities;

import java.util.List;
import java.util.Stack;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        ParseOnLoginSuccessListener {
    public static final int HOME_ID = 0;
    public static final int PERSONAL_NOTICE_ID = 1;
    public static final int PURCHASE_AGREEMENT_ID = 2;
    private static final int CONCLUDE_TRANSACTION_ID = 5;
    public static final int ENTER_WITH_AN_ACCOUNT_ID = 8;
    private static final int LOGOUT_ID = 10;
    private static final int SETTINGS_ID = 11;
    private static final int PROFILE_SETTING = 12;
    private static final int MANAGE_ACCOUNT_ID = 13;
    private static final String HOME_FRAGMENT_TAG = "HomeFragmentTag";
    private static final String PERSONAL_NOTICE_TAG = "PersonalNoticeFragmentTag";
    private static final String SETTINGS_FRAGMENT_TAG = "SettingsFragmentTag";
    private static final String PURCHASE_AGREEMENT_TAG = "PurchaseAgreementFragmentTag";
    private static final String CONCLUDE_TRANSACTION_TAG = "ConcludeTransactionTag";

    private static final String VISITOR_TAG = "visitatore";
    private static final String USER_OBJECT_NAME_FIELD = "name";

    private static final int MANAGE_ACCOUNT_CALL = 1;
    public static final int PUBBLICA_ANNUNCIO_CALL = 2;

    public static final String QUERY_TAG = "annunci";
    private static final String TAG = "MainActivity";

    private Fragment fragment;

    private Toolbar toolbar;
    private Drawer result;

    private AccountHeader headerResult;

    private ParseOnLoginSuccessListener onLoginSuccessListener;
    private ViewGroup mRootView;

    private CardView bookInfoCardView;
    private boolean clickFlag = false;
    private ActionBar supportActionBar;
    private boolean firstVisit = true;
    private ParseUser currentUser;

    private Stack<String> fragmentStack = new Stack();

    private static final String INTENT_ACTION = "com.unisa.unistore.HomeFragment";

    private IntentFilter filter = new IntentFilter(INTENT_ACTION);
    private ConnectivityChangeReceiver receiver = new ConnectivityChangeReceiver();

    private ListaAnnunci LA;

    private PrimaryDrawerItem enterWithAnAccountItem;
    private IProfile profile;
    private IProfile logoutProfile;

    protected void setActionBarIcon(int iconRes) {
        toolbar.setNavigationIcon(iconRes);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LA = new ListaAnnunci();

        if(Utilities.isUserAuthenticated())
            currentUser = ParseUser.getCurrentUser();

        ParseUtilities.connectUser(currentUser);

        mRootView = (ViewGroup) findViewById(R.id.layout_root_view);

        toolbar = (Toolbar) findViewById(R.id.fragment_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            supportActionBar = getSupportActionBar();
            supportActionBar.setDisplayShowTitleEnabled(false);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (this instanceof ParseOnLoginSuccessListener) {
            onLoginSuccessListener = (ParseOnLoginSuccessListener) this;
        } else {
            throw new IllegalArgumentException(
                    "Activity must implemement ParseOnLoginSuccessListener");
        }

        creteDrawer(false, savedInstanceState);

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(HOME_ID);
        }
    }

    private void creteDrawer(boolean compact, Bundle savedInstanceState) {
        // Create a few sample profile
        if(Utilities.isUserAuthenticated())
            profile = new ProfileDrawerItem().withName(currentUser.get("name").toString()).withEmail(currentUser.getEmail()).withTag(currentUser).withIcon(getResources().getDrawable(R.drawable.profile)).withIdentifier(PROFILE_SETTING);

        logoutProfile = new ProfileSettingDrawerItem().withName(getString(R.string.logout)).withDescription("Scollegati dall'app").withIcon(FontAwesome.Icon.faw_sign_out).withIdentifier(LOGOUT_ID);

        enterWithAnAccountItem = new PrimaryDrawerItem().withName(getString(R.string.enter_with_an_account)).withIcon(FontAwesome.Icon.faw_sign_in).withIdentifier(ENTER_WITH_AN_ACCOUNT_ID);

        PrimaryDrawerItem home = new PrimaryDrawerItem()
                .withName(R.string.home).withIcon(FontAwesome.Icon.faw_home).withIdentifier(HOME_ID);
        PrimaryDrawerItem personalNotice = new PrimaryDrawerItem()
                .withName(R.string.personal_notice).withIcon(FontAwesome.Icon.faw_archive).withIdentifier(PERSONAL_NOTICE_ID);

        DividerDrawerItem divider = new DividerDrawerItem();

        SecondaryDrawerItem settings = new SecondaryDrawerItem()
                .withName(R.string.drawer_item_settings).withIcon(FontAwesome.Icon.faw_cog);
        settings.setIdentifier(SETTINGS_ID);

        DrawerBuilder drawerBuilder = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        int identifier = drawerItem.getIdentifier();
                        switch(identifier) {
                            case HOME_ID:
                                displayView(HOME_ID);
                                break;
                            case PERSONAL_NOTICE_ID:
                                displayView(PERSONAL_NOTICE_ID);
                                break;
                            case PURCHASE_AGREEMENT_ID:
                                displayView(PURCHASE_AGREEMENT_ID);
                                //result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
                                break;
                            case ENTER_WITH_AN_ACCOUNT_ID:
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                //TODO probabilmente da aggiustare
                                startActivity(intent);
                                finish();
                                break;
                            case SETTINGS_ID:
                                //displayView(SETTINGS_ID);
                                intent = new Intent(MainActivity.this, AddNoticeViewPagerActivity.class);
                                startActivity(intent);
                                finish();
                                break;
                            default:
                                //Toast.makeText(MainActivity.this, R.string.message_error, Toast.LENGTH_SHORT).show();
                                return false;
                        }
                        return true;
                    }
                })
                .withOnDrawerNavigationListener(new Drawer.OnDrawerNavigationListener() {
                    @Override
                    public boolean onNavigationClickListener(View clickedView) {
                        //this method is only called if the Arrow icon is shown. The hamburger is automatically managed by the MaterialDrawer
                        //if the back arrow is shown. close the activity
                        MainActivity.this.finish();
                        //return true if we have consumed the event
                        return true;
                    }
                })
                .addStickyDrawerItems(
                        settings
                )
                .withAnimateDrawerItems(true)
                .withSavedInstance(savedInstanceState);

        if(!Utilities.isUserAuthenticated()) {
            drawerBuilder.addDrawerItems(
                    home,
                    divider,
                    enterWithAnAccountItem
            );
        } else {
            // Create the AccountHeader
            buildHeader(compact, savedInstanceState);

            drawerBuilder.withAccountHeader(headerResult)
                    .addDrawerItems(
                            home,
                            personalNotice,
                            new PrimaryDrawerItem()
                                    .withName(R.string.purchased).withIcon(FontAwesome.Icon.faw_shopping_cart).withIdentifier(PURCHASE_AGREEMENT_ID)
                    );
        }

        result = drawerBuilder.build();
    }

    /**
     * small helper method to reuse the logic to build the AccountHeader
     * this will be used to replace the header of the drawer with a compact/normal header
     *
     * @param compact
     * @param savedInstanceState
     */
    private void buildHeader(boolean compact, Bundle savedInstanceState) {
        IProfile login_outProfile = new ProfileDrawerItem();

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.drawer_background)
                .withCompactStyle(compact)
                .addProfiles(
                        profile,
                        /*
                        //don't ask but google uses 14dp for the add account icon in gmail but 20dp for the normal icons (like manage account)
                        new ProfileSettingDrawerItem().withName("Add Account").withDescription("Add new GitHub Account").withIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_add).actionBarSize().paddingDp(5).colorRes(R.color.material_drawer_dark_primary_text)).withIdentifier(PROFILE_SETTING),
                        */
                        new ProfileSettingDrawerItem().withName("Manage Account").withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(MANAGE_ACCOUNT_ID)
                )
                .withOnAccountHeaderSelectionViewClickListener(new AccountHeader.OnAccountHeaderSelectionViewClickListener() {
                    @Override
                    public boolean onClick(View view, IProfile iProfile) {
                        return false;
                    }
                })
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        //sample usage of the onProfileChanged listener
                        //if the clicked item has the identifier 1 add a new profile ;)
                        if (profile instanceof IDrawerItem) {

                            if (profile.getIdentifier() == PROFILE_SETTING &&
                                    !((IDrawerItem) profile).getTag().toString().equals(VISITOR_TAG)) {
                                /*
                                //loadingStart(true);
                                ParseFacebookUtils.logIn(
                                        MainActivity.this, new LogInCallback() {
                                            @Override
                                            public void done(ParseUser user, ParseException e) {
                                                if (user == null) {
                                                    if (e != null) {
                                                        Toast.makeText(MainActivity.this, getString(com.parse.ui.R.string.com_parse_ui_facebook_login_failed_toast), Toast.LENGTH_SHORT).show();
                                                        Log.w("MainActivity", getString(com.parse.ui.R.string.com_parse_ui_login_warning_facebook_login_failed) +
                                                                e.toString());
                                                    }
                                                } else if (user.isNew()) {
                                                    Request.newMeRequest(ParseFacebookUtils.getSession(),
                                                            new Request.GraphUserCallback() {
                                                                @Override
                                                                public void onCompleted(GraphUser fbUser,
                                                                                        Response response) {
                                                                    ParseUser parseUser = ParseUser.getCurrentUser();
                                                                    if (fbUser != null && parseUser != null
                                                                            && fbUser.getName().length() > 0) {
                                                                        parseUser.put(USER_OBJECT_NAME_FIELD, fbUser.getName());
                                                                        parseUser.saveInBackground(new SaveCallback() {
                                                                            @Override
                                                                            public void done(ParseException e) {
                                                                                if (e != null) {
                                                                                    Log.w("MainActivity", getString(
                                                                                            com.parse.ui.R.string.com_parse_ui_login_warning_facebook_login_user_update_failed) +
                                                                                            e.toString());
                                                                                }
                                                                                loginSuccess();
                                                                            }
                                                                        });
                                                                    }
                                                                    loginSuccess();
                                                                }
                                                            }
                                                    ).executeAsync();
                                                } else {
                                                    loginSuccess();
                                                }
                                            }
                                        });
    */
                            } else if (profile.getIdentifier() == MANAGE_ACCOUNT_ID) {
                                if (Utilities.isUserAuthenticated()) {
                                    Intent intent = new Intent(MainActivity.this, AccountManagementActivity.class);
                                    startActivityForResult(intent, MANAGE_ACCOUNT_CALL);
                                } else {
                                    Toast toast = Toast.makeText(MainActivity.this, R.string.non_authenticated, Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            } else if (profile.getIdentifier() == ENTER_WITH_AN_ACCOUNT_ID) {
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }

                        //false if you have not consumed the event and it should close the drawer
                        return false;
                    }

                })
                .withSavedInstance(savedInstanceState)
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MANAGE_ACCOUNT_CALL) {
            if (resultCode == RESULT_OK && result != null) {
                creteDrawer(false, null); //non deve essere creato l'header
            }
        } else if (requestCode == PUBBLICA_ANNUNCIO_CALL) {
            refresh();
        }
    }

    /*
    private void loginSuccess() {
        onLoginSuccessListener.onLoginSuccess();

        currentUser = ParseUser.getCurrentUser();
        if(Utilities.isUserAuthenticated()) {
            headerResult.addProfiles(logoutProfile);
            result.addItem(null);

            Context context = getApplicationContext();
            CharSequence text = getString(R.string.welcome) + " " +
                    currentUser.getString("name");
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

    }
*/
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

    //TODO Costringere ad usare solo le costanti opportune
    public void setDrawerSelection(int selection_id) {
        result.setSelection(selection_id);
    }

    @Override
    public void onResume(){
        super.onResume();

        registerReceiver(receiver, filter);

        if(Utilities.isUserAuthenticated()) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Users_Online");
            try {
                List<ParseObject> users_online = query.whereEqualTo("userId", currentUser.getObjectId().toString()).find();
                if (!users_online.isEmpty()) {
                    users_online.get(0).put("online", true);
                    users_online.get(0).saveEventually();
                }
            } catch (ParseException e) {
                Log.d(TAG, "onPause()/Si è verificato un'errore: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if(fragmentStack.empty()) {
            return;
        }

    }

    @Override
    public void onPause() {
        // Unregister the receiver
        unregisterReceiver(receiver);

        ParseUtilities.disconnectUser(this);

        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        final SearchView searchView = (SearchView)menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                //Toast.makeText(MainActivity.this, query, Toast.LENGTH_SHORT).show();
                searchView.clearFocus();
                Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
                intent.putExtra(QUERY_TAG, query);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                return false;
            }
        });

        return super.onPrepareOptionsMenu(menu) | true;
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     * */
    private void displayView(int position) {
        Intent intent = null;

        //TODO da eliminare quasi sicuramente
        String selection = null;
        if(!fragmentStack.empty())
            selection = fragmentStack.lastElement();

        switch (position) {
            case HOME_ID:
                if(selection != null && selection.equals(HOME_FRAGMENT_TAG))
                    return;
                // update the main content by replacing fragments
                fragment = new HomeFragment();
                pushFragment(fragment, HOME_FRAGMENT_TAG);
                //((HomeFragment)fragment).setToolbar(toolbar);
                ((HomeFragment)fragment).setToolbar(supportActionBar);
                break;
            case PERSONAL_NOTICE_ID:
                if(selection != null && selection.equals(PERSONAL_NOTICE_TAG))
                    return;

                fragment = new PersonalNoticeFragment();
                pushFragment(fragment, PERSONAL_NOTICE_TAG);

                ((PersonalNoticeFragment)fragment).setToolbar(supportActionBar);
                break;
            case PURCHASE_AGREEMENT_ID:
                if(selection != null && selection.equals(PURCHASE_AGREEMENT_TAG))
                    return;

                fragment = new PurchaseAgreementFragment();
                pushFragment(fragment, PURCHASE_AGREEMENT_TAG);

                ((PurchaseAgreementFragment)fragment).setToolbar(supportActionBar);
                break;
            case ENTER_WITH_AN_ACCOUNT_ID:
                if(Utilities.isUserAuthenticated()) {
                    ParseUser.logOut();
                    currentUser = null;

                    Context context = getApplicationContext();
                    CharSequence text = getString(R.string.is_logout);
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

                break;
            default:
                break;

        }

        if(position != ENTER_WITH_AN_ACCOUNT_ID)
            result.closeDrawer();

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.layout_root_view, fragment)
                    // Add this t.the front of the card.
                    .addToBackStack(null)
                    .commit();
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    private int pushFragment(Fragment fragment, String tag) {
        int fragmentTopPosition = fragmentStack.size() - 1;

        if(fragmentTopPosition >= 0) {
            if(fragmentStack.get(fragmentTopPosition).equals(tag))
                return -1;
        }
        fragmentStack.push(tag);

        return 0;
    }

    @Override
    public void onBackPressed() {
        if(fragment instanceof NoticeFragment) {
            final NoticeFragment noticeFragment = (NoticeFragment) fragment;
            //Controlla se il menu del fabButton sia aperto: in tal caso questo viene chiuso.
            if(noticeFragment.isFABMenuOpened()) {
                noticeFragment.closeFABMenu();
                return;
            }
        }

        if(result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else if(getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().popBackStack();
            if(fragmentStack.empty()) {
                return;
            } else {
                //Tolgo l'ultimo fragment dallo stack per ottenere il fragment precedente
                //(che corrisponde a quello per cui deve essere selezionata la corrispondente voce
                //nel drawer)
                String lastFragment = fragmentStack.pop();
                //TODO da eliminare quasi sicuramente
                String selection = null;
                if (!fragmentStack.empty())
                    selection = fragmentStack.lastElement();

                if (selection != null) {
                    switch (selection) {
                        case HOME_FRAGMENT_TAG:
                            result.setSelection(HOME_ID, false);
                            break;
                        case PERSONAL_NOTICE_TAG:
                            result.setSelection(PERSONAL_NOTICE_ID, false);
                            break;
                        case PURCHASE_AGREEMENT_TAG:
                            result.setSelection(PURCHASE_AGREEMENT_ID, false);
                            break;
                        case SETTINGS_FRAGMENT_TAG:
                            break;
                        default:
                            break;
                    }
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu) | true;
    }

    @Override
    public void onClick(View v) {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = result.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        if(headerResult != null)
            outState = headerResult.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onLoginSuccess() {

    }

    public void refresh() {
        ((NoticeFragment) fragment).refresh();
    }
}
