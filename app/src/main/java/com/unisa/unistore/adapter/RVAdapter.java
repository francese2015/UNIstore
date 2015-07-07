package com.unisa.unistore.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.unisa.unistore.NoticeDetailActivity;
import com.unisa.unistore.R;
import com.unisa.unistore.model.ListaAnnunci;
import com.unisa.unistore.utilities.ImageUtilities;
import com.unisa.unistore.utilities.Utilities;

import java.util.ArrayList;


/**
 * Created by Daniele on 16/05/2015.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.AnnuncioViewHolder>{
    private static final String PACKAGE = "com.unisa.unistore.adapter";
    public static final String BOOK_TITLE_MESSAGE = "title";
    public static final String BOOK_AUTHORS_MESSAGE = "authors";
    public static final String BOOK_PRICE_MESSAGE = "price";
    public static final String BOOK_IMAGE_URL_MESSAGE = "image";
    public static final String BOOK_DESCRIPTION_MESSAGE = "description";
    public static final String BOOK_STATE_MESSAGE = "state";
    public static final String BOOK_LANGUAGE_MESSAGE = "language";
    public static final String BOOK_ISBN_MESSAGE = "ISBN";
    public static final String BOOK_ID_MESSAGE = "id";

    private Activity activity;
    private ListaAnnunci annunci = new ListaAnnunci();

    private com.nostra13.universalimageloader.core.ImageLoader imageLoader;
    private DisplayImageOptions options;

    public void setListaAnnunci(ListaAnnunci annunci){
        this.annunci = annunci;
    }

    public RVAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public AnnuncioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_layout, parent, false);
        AnnuncioViewHolder pvh = new AnnuncioViewHolder(v, activity);
        return pvh;
    }

    @Override
    public void onBindViewHolder(AnnuncioViewHolder annuncioHolder, int position) {
        String id = annunci.getAnnuncio(position).getLibro().getIDLibro();
        annuncioHolder.idLibro.setText(id);

        String titolo = annunci.getAnnuncio(position).getLibro().getTitoloLibro();
        annuncioHolder.titoloLibro.setText(titolo);

        ArrayList<String> autori = (ArrayList<String>) annunci.getAnnuncio(position).getLibro().getAutoriLibro();
        String strAutori = "";
        if (autori.size() > 0) {
            strAutori = autori.get(0);
            int size = autori.size();
            for (int i = 1; i < size; i++) {
                strAutori += ", " + autori.get(i);
            }
            annuncioHolder.autoriLibro.setText(strAutori);
        }

        String URLImmagineCopertina = annunci.getAnnuncio(position).getLibro().getURLImmagineCopertina();
        annuncioHolder.setURLImmagineCopertina(URLImmagineCopertina);
        new ImageUtilities(activity, annuncioHolder.fotoLibro).displayImage(URLImmagineCopertina);
        //new GetBookThumb(annuncioHolder.fotoLibro).execute(URLImmagineCopertina);


        String prezzo = String.valueOf(annunci.getAnnuncio(position).getPrezzo());
        annuncioHolder.prezzoLibro.setText(prezzo + activity.getString(R.string.euro_symbol));

        String descrizione = annunci.getAnnuncio(position).getLibro().getDescrizione();
        //annuncioHolder.customizeCard(titolo, strAutori, prezzo, URLImmagineCopertina, descrizione);
    }

    @Override
    public int getItemCount() {
        return annunci.size();
    }

    public static class AnnuncioViewHolder extends RecyclerView.ViewHolder {
        private Activity activity;

        private TextView idLibro;
        private TextView titoloLibro;
        private TextView autoriLibro;
        private TextView prezzoLibro;
        private ImageView fotoLibro;

        private CardView cardView;
        private String URLImmagineCopertina;

        AnnuncioViewHolder(View itemView, Activity activity) {
            super(itemView);
            this.activity = activity;

            this.idLibro = (TextView) itemView.findViewById(R.id.book_id);
            this.titoloLibro = (TextView) itemView.findViewById(R.id.book_title);
            this.autoriLibro = (TextView) itemView.findViewById(R.id.book_authors);
            this.prezzoLibro = (TextView) itemView.findViewById(R.id.book_price);
            this.fotoLibro = (ImageView) itemView.findViewById(R.id.take_book_photo);
            //this.descrizioneLibro = (TextView) itemView.findViewById(R.id.book_description);

            cardView = (CardView)itemView.findViewById(R.id.expandable_card_view);
            setCardClickListener();
        }

        private void setCardClickListener() {
            cardView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(Utilities.isClicked())
                        return;

                    Utilities.setClicked(true);
                    Intent i = new Intent(activity, NoticeDetailActivity.class);

                    String id = idLibro.getText().toString();
                    i.putExtra(BOOK_ID_MESSAGE, id);
                    String title = titoloLibro.getText().toString();
                    i.putExtra(BOOK_TITLE_MESSAGE, title);
                    String authors = autoriLibro.getText().toString();
                    i.putExtra(BOOK_AUTHORS_MESSAGE, authors);
                    String price = prezzoLibro.getText().toString();
                    i.putExtra(BOOK_PRICE_MESSAGE, price);
                    Drawable url = fotoLibro.getDrawable();
                    i.putExtra(BOOK_IMAGE_URL_MESSAGE, URLImmagineCopertina);

                    int[] screenLocation = new int[2];
                    v.getLocationOnScreen(screenLocation);
                    i.putExtra(PACKAGE + ".left", screenLocation[0]).
                            putExtra(PACKAGE + ".top", screenLocation[1]).
                            putExtra(PACKAGE + ".width", v.getWidth()).
                            putExtra(PACKAGE + ".height", v.getHeight());

                    Pair<TextView, String> titoloLibroPair = Pair.create(titoloLibro, activity.getString(R.string.title_detail_transition_name));
                    Pair<TextView, String> autoriLibroPair = Pair.create(autoriLibro, activity.getString(R.string.authors_detail_transition_name));
                    Pair<TextView, String> prezzoLibroPair = Pair.create(prezzoLibro, activity.getString(R.string.price_detail_transition_name));
                    Pair<ImageView, String> fotoLibroPair = Pair.create(fotoLibro, activity.getString(R.string.image_detail_transition_name));
                    Pair<View, String>[] pairs = new Pair[]{titoloLibroPair, autoriLibroPair, prezzoLibroPair, fotoLibroPair};

                    ActivityOptionsCompat transitionActivityOptions =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(activity, pairs);
                    ActivityCompat.startActivity(activity, i, transitionActivityOptions.toBundle());
                }
            });
        }

        public void setURLImmagineCopertina(String URLImmagineCopertina) {
            this.URLImmagineCopertina = URLImmagineCopertina;
        }
    }

}
