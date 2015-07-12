package com.unisa.unistore;

import android.os.Bundle;
import android.view.View;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.unisa.unistore.model.ListaAnnunci;
import com.unisa.unistore.utilities.NetworkUtilities;

//TODO Questa classe e HomeFragment devono essere reingegnerizzate (i metodi in comune vanno inseriti in una classe di utilities, oppure pensare se conviene inserire qualche metodo in MainActivity, tipo la gestione dei FAB)
public class PersonalNoticeFragment extends NoticeFragment {

    private ListaAnnunci LA;

    public PersonalNoticeFragment() { }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LA = new ListaAnnunci();

        setFAB();
        setNoticeListLayout(LA, true);

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
        query.whereEqualTo("autore_annuncio", ParseUser.getCurrentUser().getObjectId());
        query.orderByDescending("createdAt");

        super.downloadAnnunci(isRefreshing, query);
    }

}
