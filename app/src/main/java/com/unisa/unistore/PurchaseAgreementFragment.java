package com.unisa.unistore;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.unisa.unistore.model.Annuncio;
import com.unisa.unistore.model.ListaAnnunci;
import com.unisa.unistore.utilities.NetworkUtilities;

/**
 * Created by Daniele on 02/07/2015.
 */
public class PurchaseAgreementFragment extends NoticeFragment {

    public static final String NOTICE_AUTHOR_ID_TAG = "PurchaseAgreementIntent";

    private ListaAnnunci LA;

    public PurchaseAgreementFragment() { }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().findViewById(R.id.fab_menu).setVisibility(View.GONE);

        LA = new ListaAnnunci();

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
        query.whereEqualTo("id_acquirente", ParseUser.getCurrentUser().getObjectId());
        query.whereGreaterThanOrEqualTo("stato_transazione", Annuncio.TRANSAZIONE_ACQUISTO_CONCORDATO);
        query.whereLessThanOrEqualTo("stato_transazione", Annuncio.TRANSAZIONE_FINE);
        query.orderByDescending("createdAt");

        super.downloadAnnunci(isRefreshing, query);
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

}
