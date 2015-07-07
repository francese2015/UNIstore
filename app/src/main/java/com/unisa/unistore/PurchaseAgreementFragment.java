package com.unisa.unistore;

import android.app.Activity;
import android.app.Fragment;
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

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.unisa.unistore.adapter.RVAdapter;
import com.unisa.unistore.model.Annuncio;
import com.unisa.unistore.model.ListaAnnunci;
import com.unisa.unistore.utilities.NetworkUtilities;
import com.unisa.unistore.utilities.Utilities;

import java.util.List;

/**
 * Created by Daniele on 02/07/2015.
 */
public class PurchaseAgreementFragment extends Fragment {

    private static final int QUERY_LIMIT = 6;
    public static final String NOTICE_AUTHOR_ID_TAG = "PurchaseAgreementIntent";

    private Activity activity;
    private Toolbar toolbar;
    private ActionBar supportActionBar;

    private ListaAnnunci LA;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RVAdapter adapter;

    private boolean loading = true;
    private boolean lastNoticeReached = false;
    private static int queryCnt = 0;

    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private int lastNoticesCnt = 0;
    private boolean noConnection = false;

    private FrameLayout wheel;
    private RecyclerView recyclerView;

    public PurchaseAgreementFragment() { }

    public void setToolbar(Toolbar t) {
        toolbar = t;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //((MainActivity) activity).setDrawerSelection(MainActivity.PURCHASE_AGREEMENT_ID);

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

                if (dy != 0 && loading && !lastNoticeReached) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = false;
                        Log.d("onScrolled-last", "Ultimo annuncio caricato!");
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

        if(NetworkUtilities.checkConnection(activity)) {
            getActivity().findViewById(R.id.no_connection_message).setVisibility(View.INVISIBLE);
            downloadAnnunci(false);
        } else {
            getActivity().findViewById(R.id.no_connection_message).setVisibility(View.VISIBLE);
            noConnection = true;
        }
    }

    private void refreshContent() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(NetworkUtilities.checkConnection(activity)) {
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
        if(ParseUser.getCurrentUser() == null)
            return;
        query.whereEqualTo("id_acquirente", ParseUser.getCurrentUser().getObjectId());
        query.whereGreaterThanOrEqualTo("stato_transazione", Annuncio.TRANSAZIONE_ACQUISTO_CONCORDATO);
        query.whereLessThanOrEqualTo("stato_transazione", Annuncio.TRANSAZIONE_FINE);
        query.orderByDescending("createdAt");

        query.countInBackground(new CountCallback() {
            @Override
            public void done(int i, ParseException e) {
                Log.d("Libri", "i = " + i);

                if(i <= 0) {
                    return;
                }

                if(i < lastNoticesCnt || !refresh) {
                    if(i < lastNoticesCnt) { // sono stati eliminati uno o più annunci
                        query.setLimit(i);
                        LA.clear();
                    } else {
                        query.setLimit(QUERY_LIMIT);
                        query.setSkip(queryCnt);
                    }

                    if(i == adapter.getItemCount()) {
                        lastNoticeReached = true;
                        return;
                    }

                    lastNoticesCnt = i;

                    Utilities.slideUP(activity, wheel);

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
        if(supportActionBar != null)
            supportActionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onResume() {
        super.onResume();

        Utilities.setClicked(false);
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
}
