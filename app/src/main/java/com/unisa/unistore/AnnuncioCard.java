package com.unisa.unistore;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.unisa.unistore.utilities.GetBookThumb;
import com.unisa.unistore.utilities.Utilities;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by Daniele on 19/05/2015.
 */
public class AnnuncioCard extends Card {

    private final Activity activity;
    private String titolo;
    private TextView titoloLibro;
    private String autori;
    private TextView autoriLibro;
    private String prezzo;
    private TextView prezzoLibro;
    private String URLImmagineCopertina;
    private ImageView fotoLibro;
    private CardHeader header;


    public AnnuncioCard(Activity activity, String titolo, String autori, String prezzo, String URLImmagineCopertina) {
        super(activity, R.layout.card_view_inner_content);
        this.titolo = titolo;
        this.autori = autori;
        this.prezzo = prezzo;
        this.URLImmagineCopertina = URLImmagineCopertina;
        this.activity = activity;
    }

    private void init(){
        //Create a CardHeader
        header = new CardHeader(activity);

        //Set the header title
        header.setTitle(titolo);

        //Set visible the expand/collapse button
        header.setButtonExpandVisible(true);

        addCardHeader(header);

        //Add ClickListener
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Toast.makeText(getContext(), "Click Listener card=" + titolo, Toast.LENGTH_SHORT).show();
            }
        });

        //This provides a simple (and useless) expand area
        CustomExpandCard expand = new CustomExpandCard(activity);
        //Add Expand Area to Card
        addCardExpand(expand);

        //setSwipeable(true);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Retrieve elements
        titoloLibro = (TextView) parent.findViewById(R.id.book_title);
        autoriLibro = (TextView) parent.findViewById(R.id.book_authors);
        prezzoLibro = (TextView) parent.findViewById(R.id.book_price);
        fotoLibro = (ImageView) parent.findViewById(R.id.book_photo);

        if (titoloLibro != null)
            titoloLibro.setText(titolo);


        if (autoriLibro != null)
            autoriLibro.setText(autori);

        if (prezzoLibro != null)
            prezzoLibro.setText(prezzo);

        if (fotoLibro != null) {
            new GetBookThumb(fotoLibro).execute(URLImmagineCopertina);
            Utilities utilities = new Utilities();
            //utilities.scaleImage(fotoLibro);
        }
    }

    @Override
    public int getType() {
        //Very important with different inner layouts
        return 0;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public void setAutori(String autori) {
        this.autori = autori;
    }

    public void setPrezzo(String prezzo) {
        this.prezzo = prezzo;
    }

    public void setURLImmagineCopertina(String URLImmagineCopertina) {
        this.URLImmagineCopertina = URLImmagineCopertina;
    }
}
