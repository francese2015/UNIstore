package com.unisa.unistore;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.unisa.unistore.model.Annuncio;
import com.unisa.unistore.model.ListaAnnunci;
import com.unisa.unistore.utilities.NetworkUtilities;

public class HomeFragment extends NoticeFragment {

    public static final String INPUT_TYPE_MESSAGE = "input type";
    private static final String TAG = "HomeFragment";

    private boolean firstLaunch = true;

    public HomeFragment() { }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LA = new ListaAnnunci();

        setFAB();
        setNoticeListLayout(LA, false);

        //TODO Questo if va inserito in qualche altro metodo
        if(NetworkUtilities.checkConnection(activity)) {
            no_connection_message.setVisibility(View.INVISIBLE);
            downloadAnnunci(false);
        } else {
            no_connection_message.setVisibility(View.VISIBLE);
        }
    }

    public void downloadAnnunci(boolean isRefreshing) {
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Libri");
        query.whereLessThan("stato_transazione", Annuncio.TRANSAZIONE_ACQUISTO_CONCORDATO);
        query.orderByDescending("createdAt");
        //query.orderByDescending("updatedAt");

        super.downloadAnnunci(isRefreshing, query);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get item selected and deal with it
        switch (item.getItemId()) {
            case android.R.id.home:
                //called when the up affordance/carat in actionbar is pressed
                super.activity.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(firstLaunch) {
            firstLaunch = false;
        }
    }

}
