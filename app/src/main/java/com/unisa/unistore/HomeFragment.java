package com.unisa.unistore;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.ImageButton;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.unisa.unistore.adapter.RVAdapter;
import com.unisa.unistore.model.ListaAnnunci;

import java.util.List;

public class HomeFragment extends Fragment {

    private static final int QUERY_LIMIT = 10;
    private Toolbar toolbar;
    private ActionBar supportActionBar;

    private ListaAnnunci LA;
    private RecyclerView rv;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RVAdapter adapter;

    private boolean loading = true;
    static int queryCnt = 0;
    int pastVisiblesItems, visibleItemCount, totalItemCount;

    public HomeFragment(){}

    public void setToolbar(Toolbar t) {
        toolbar = t;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LA = new ListaAnnunci();


        mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.annuncio_swipe_refresh_layout);
        rv = (RecyclerView) getActivity().findViewById(R.id.recycler_view);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(mLayoutManager);

        queryCnt = 0;

        adapter = new RVAdapter(getActivity());
        adapter.setListaAnnunci(LA);
        rv.setAdapter(adapter);

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                Log.d("onScrolled", "visibleItemCount + pastVisiblesItems = " + (visibleItemCount + pastVisiblesItems) + "\ntotalItemCount = " + totalItemCount);
                if (loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = false;
                        Log.d("onScrolled", "Last Item Wow !");
                        downloadAnnunci();
                    }
                }
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryCnt = 0;
                refreshContent();
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_dark,
                R.color.primary,
                R.color.accent
        );

        rv.setHasFixedSize(true);

        if(checkConnection()) {
            getActivity().findViewById(R.id.no_connection_message).setVisibility(View.INVISIBLE);
            downloadAnnunci();
        } else {
            getActivity().findViewById(R.id.no_connection_message).setVisibility(View.VISIBLE);
        }
    }

    private boolean checkConnection() {
        String DEBUG_TAG = "NetworkStatus";

        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

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
                    downloadAnnunci();
                    mSwipeRefreshLayout.setRefreshing(false);
                } else {
                    Toast.makeText(getActivity(),
                            "Attivare la connessione per scaricare la lista degli annunci",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }, 5000);
    }

    private void downloadAnnunci() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Libri");
        query.orderByDescending("updatedAt");
        query.setLimit(QUERY_LIMIT);
        query.setSkip(queryCnt);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, ParseException e) {
                if (e == null) {
                    Log.d("Libri", "Trovati " + scoreList.size() + " libri");
                    LA.addAnnuncio(scoreList);
                    adapter.notifyDataSetChanged();
                    queryCnt += 10;
                } else {
                    Log.d("Libri", "Errore: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);
        // update the actionbar to show the up carat/affordance
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        ImageButton fab_aggiungiAnnuncio = (ImageButton) getActivity().findViewById(R.id.fab_image_button);
        fab_aggiungiAnnuncio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(ParseUser.getCurrentUser().isAuthenticated()) {
                    Intent intent = new Intent(getActivity(), PubblicaAnnuncioActivity.class);
                    getActivity().startActivityForResult(intent, 12345);
                } else {
                    Context context = getActivity().getApplicationContext();
                    CharSequence text = getString(R.string.profile_title_logged_out);
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });

        /*
        Utilities utilities = new Utilities();
        utilities.scaleImage((ImageView) activity.findViewById(R.id.bookPhoto));
        utilities.scaleImage((ImageView) activity.findViewById(R.id.bookPhoto2));
        utilities.scaleImage((ImageView) activity.findViewById(R.id.bookPhoto3));
        utilities.scaleImage((ImageView) activity.findViewById(R.id.bookPhoto4));
        */
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get item selected and deal with it
        switch (item.getItemId()) {
            case android.R.id.home:
                //called when the up affordance/carat in actionbar is pressed
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setToolbar(ActionBar supportActionBar) {
        this.supportActionBar = supportActionBar;
    }
}
