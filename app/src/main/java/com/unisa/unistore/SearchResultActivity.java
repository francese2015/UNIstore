package com.unisa.unistore;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.unisa.unistore.adapter.RVAdapter;
import com.unisa.unistore.model.ListaAnnunci;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by utente pc on 17/06/2015.
 */
public class SearchResultActivity extends ActionBarActivity {

    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private RVAdapter adapter;
    private ListaAnnunci LA;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.fragment_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar supportActionBar = getSupportActionBar();
            supportActionBar.setDisplayShowTitleEnabled(false);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.annuncio_swipe_refresh_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        LA = new ListaAnnunci();

        adapter = new RVAdapter(this);
        adapter.setListaAnnunci(LA);

        doParseQuery(getIntent().getStringExtra(MainActivity.QUERY_TAG));

        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

    }

    private void doParseQuery(String queryString) {
        final ParseQuery<ParseObject> titleQuery = ParseQuery.getQuery("Libri");
        titleQuery.whereStartsWith("titolo", queryString);

        final ParseQuery<ParseObject> authorsQuery = ParseQuery.getQuery("Libri");
        authorsQuery.whereStartsWith("autori", queryString);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(titleQuery);
        queries.add(authorsQuery);

        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, ParseException e) {
                if (e == null) {
                    if (scoreList.size() > 0) {
                        LA.addAnnunci(scoreList, false);
                        adapter.notifyDataSetChanged();
                    }

                    Log.d("Annunci", "Trovati " + scoreList.size() + " annunci");
                } else {
                    Log.d("Annunci", "Errore: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get item selected and deal with it
        switch (item.getItemId()) {
            case android.R.id.home:
                //called when the up affordance/carat in actionbar is pressed
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
