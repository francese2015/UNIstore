package com.unisa.unistore;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.unisa.unistore.adapter.RVAdapter;
import com.unisa.unistore.model.ListaAnnunci;
import com.unisa.unistore.utilities.Utilities;

import java.util.List;

public class HomeFragment extends Fragment {

    private static final int QUERY_LIMIT = 5;
    private static boolean clicked;
    private static boolean isFABMenuOpened;

    private Activity activity;
    private Toolbar toolbar;
    private ActionBar supportActionBar;

    private ListaAnnunci LA;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RVAdapter adapter;

    private boolean loading = true;
    private static int queryCnt = 0;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;

    private int lastNoticesCnt = 0;
    private boolean noConnection = false;
    private boolean firstLaunch = true;
    private View fab_background;
    private FloatingActionMenu fab_menu;
    private FloatingActionButton fab_camera, fab_form;
    //private FloatingActionButton fab_line;
    private FrameLayout wheel;
    private RecyclerView recyclerView;

    public HomeFragment(){ }

    public void setToolbar(Toolbar t) {
        toolbar = t;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("ResourceAsColor")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LA = new ListaAnnunci();

        mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.annuncio_swipe_refresh_layout);
        recyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        queryCnt = 0;

        adapter = new RVAdapter(getActivity());
        adapter.setListaAnnunci(LA);
        recyclerView.setAdapter(adapter);

        fab_menu = (FloatingActionMenu) activity.findViewById(R.id.fab_menu);

        fab_camera = (FloatingActionButton) activity.findViewById(R.id.fab_camera);

        fab_form = (FloatingActionButton) activity.findViewById(R.id.fab_form);

        setFABListener(fab_form, fab_camera);

        /*
        fab_line = (FloatingActionButton) activity.findViewById(R.id.fab_line);
        fab_line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_line.setLineMorphingState((fab_line.getLineMorphingState() + 1) % 2, true);


                if (Utilities.isUserAuthenticated()) {
                    Intent intent = new Intent(activity, PubblicaAnnuncioActivity.class);
                    //activity.startActivityForResult(intent, 12345);
                } else {
                    Context context = activity.getApplicationContext();
                    CharSequence text = getString(R.string.profile_title_logged_out);
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });
*/
        wheel = (FrameLayout) getActivity().findViewById(R.id.progress_wheel_layout);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                Log.d("onScrolled", "visibleItemCount (" + visibleItemCount +
                        ") + pastVisiblesItems (" + pastVisiblesItems + ") = " +
                        (visibleItemCount + pastVisiblesItems) +
                        "\ntotalItemCount = " + totalItemCount);

                if (dy != 0 && loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = false;
                        Log.d("onScrolled-last", "Ultimo annuncio caricato!");
                        Utilities.slideUP(activity, wheel);
                        downloadAnnunci(false);
                    }
                } else {
                    if ((visibleItemCount + pastVisiblesItems) == totalItemCount - 1) {
                        if (!loading)
                            loading = true;
                    }
                }
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_dark,
                R.color.primary,
                R.color.accent
        );

        recyclerView.setHasFixedSize(true);

        if(checkConnection()) {
            getActivity().findViewById(R.id.no_connection_message).setVisibility(View.INVISIBLE);
            downloadAnnunci(false);
        } else {
            getActivity().findViewById(R.id.no_connection_message).setVisibility(View.VISIBLE);
            noConnection = true;
        }
    }

    private void setFABListener(final FloatingActionButton... fab_form) {
        int len = fab_form.length;
        FloatingActionButton tmp = null;

        for(int i = 0; i < len; i++) {
            tmp = fab_form[i];
            tmp.setLabelVisibility(View.INVISIBLE);

            tmp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utilities.isUserAuthenticated()) {
                        Intent intent = new Intent(activity, PubblicaAnnuncioActivity.class);
                        activity.startActivityForResult(intent, 12345);
                    } else {
                        Context context = activity.getApplicationContext();
                        CharSequence text = getString(R.string.profile_title_logged_out);
                        int duration = Toast.LENGTH_LONG;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                }
            });

            final FloatingActionButton finalTmp = tmp;
            tmp.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    finalTmp.setLabelVisibility(View.VISIBLE);

                    return false;
                }
            });
        }
    }

    private boolean checkConnection() {
        String DEBUG_TAG = "NetworkStatus";

        ConnectivityManager connMgr = (ConnectivityManager)
                activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();
        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = networkInfo.isConnected();

        Log.d(DEBUG_TAG, "Wifi connected: " + isWifiConn);
        Log.d(DEBUG_TAG, "Mobile connected: " + isMobileConn);

        return isMobileConn || isWifiConn;
    }

    private void refreshContent() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(checkConnection()) {
                    downloadAnnunci(true);
                    mSwipeRefreshLayout.setRefreshing(false);
                    if(noConnection)
                        activity.findViewById(R.id.no_connection_message).setVisibility(View.INVISIBLE);
                } else {
                    Toast.makeText(activity,
                            "Attivare la connessione per scaricare la lista degli annunci",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }, 5000);
    }

    private void downloadAnnunci(boolean isRefreshing) {
        final boolean refresh = isRefreshing;
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Libri");
        query.orderByDescending("updatedAt");

        query.countInBackground(new CountCallback() {
            @Override
            public void done(int i, ParseException e) {
                Log.d("Libri", "i = " + i);

                if(i < lastNoticesCnt || !refresh) {
                    if(i < lastNoticesCnt) { // sono stati eliminati uno o più annunci
                        query.setLimit(i);
                        LA.clear();
                    }
                    else {
                        query.setLimit(QUERY_LIMIT);
                        query.setSkip(queryCnt);
                    }
                    lastNoticesCnt = i;

                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> scoreList, ParseException e) {
                            if (e == null) {
                                if(scoreList.size() > 0) {
                                    LA.addAnnunci(scoreList, false);
                                    Utilities.slideDown(activity, wheel);
                                    adapter.notifyDataSetChanged();
                                    queryCnt += scoreList.size();
                                }

                                Log.d("Libri", "Trovati " + scoreList.size() + " libri" +
                                        "\nlastNoticesCnt = " + lastNoticesCnt +
                                        "\nqueryCnt = " + queryCnt);
                            } else {
                                Log.d("Libri", "Errore: " + e.getMessage());
                            }
                        }
                    });
                } else {
                    int count = Math.abs(lastNoticesCnt - i);

                    if(count <= 0)
                        return;

                    query.setLimit(count);
                    lastNoticesCnt += count;
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> scoreList, ParseException e) {
                            if (e == null) {
                                if(scoreList.size() > 0) {
                                    LA.addAnnunci(scoreList, true);
                                    adapter.notifyDataSetChanged();
                                    queryCnt += scoreList.size();
                                }

                                Log.d("Libri-refresh", "Trovati " + scoreList.size() + " libri" +
                                        "\nlastNoticesCnt = " + lastNoticesCnt +
                                        "\nqueryCnt = " + queryCnt);
                            } else {
                                Log.d("Libri", "Errore: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        activity = getActivity();

        return rootView;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);
        // update the actionbar to show the up carat/affordance
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        /*
        ImageButton fab_aggiungiAnnuncio = (ImageButton) activity.findViewById(R.id.fab_image_button);
        fab_aggiungiAnnuncio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(Utilities.isUserAuthenticated()) {
                    Intent intent = new Intent(activity, PubblicaAnnuncioActivity.class);
                    activity.startActivityForResult(intent, 12345);
                } else {
                    Context context = activity.getApplicationContext();
                    CharSequence text = getString(R.string.profile_title_logged_out);
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });
*/
        /*
        Utilities utilities = new Utilities();
        utilities.scaleImage((ImageView) activity.findViewById(R.id.bookPhoto));
        utilities.scaleImage((ImageView) activity.findViewById(R.id.bookPhoto2));
        utilities.scaleImage((ImageView) activity.findViewById(R.id.bookPhoto3));
        utilities.scaleImage((ImageView) activity.findViewById(R.id.bookPhoto4));
        */
    }

    @Override
    public void onResume() {
        super.onResume();

        if(firstLaunch) {
            firstLaunch = false;
            return;
        }

//        fab_line.setLineMorphingState(0, true);
        fab_menu.close(true);

        setClicked(false);
        refreshContent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get item selected and deal with it
        switch (item.getItemId()) {
            case android.R.id.home:
                //called when the up affordance/carat in actionbar is pressed
                activity.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setToolbar(ActionBar supportActionBar) {
        this.supportActionBar = supportActionBar;
    }

    public static boolean isClicked() {
        return clicked;
    }

    public static void setClicked(boolean isClicked) {
        clicked = isClicked;
    }
}
