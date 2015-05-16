package com.unisa.unistore.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.unisa.unistore.utilities.GetBookThumb;
import com.unisa.unistore.R;
import com.unisa.unistore.model.ListaAnnunci;

import java.util.ArrayList;

/**
 * Created by Daniele on 16/05/2015.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder>{
    ListaAnnunci annunci;

    public RVAdapter(ListaAnnunci annunci){
        this.annunci = annunci;
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_layout, parent, false);
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PersonViewHolder annuncioHolder, int position) {
        annuncioHolder.titoloLibro.setText(annunci.getAnnuncio(position).getLibro().getTitoloLibro());

        ArrayList<String> autori = annunci.getAnnuncio(position).getLibro().getAutoriLibro();
        String strAutori = autori.get(0);
        int size = autori.size();
        for(int i = 1; i < size; i++) {
            strAutori += ", " + autori.get(i);
        }
        annuncioHolder.AutoriLibro.setText(strAutori);

        new GetBookThumb(annuncioHolder.fotoLibro).
                execute(annunci.getAnnuncio(position).getLibro().getUrlImmagineCopertina());
    }

    @Override
    public int getItemCount() {
        return annunci.size();
    }

    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView titoloLibro;
        TextView AutoriLibro;
        ImageView fotoLibro;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.expandable_card_view);
            titoloLibro = (TextView)itemView.findViewById(R.id.book_title);
            AutoriLibro = (TextView)itemView.findViewById(R.id.book_author);
            fotoLibro = (ImageView)itemView.findViewById(R.id.bookPhoto);
        }
    }

}
