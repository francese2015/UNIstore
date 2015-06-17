package com.unisa.unistore.adapter;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.unisa.unistore.R;
import com.unisa.unistore.model.ListaAnnunci;

import java.util.ArrayList;


/**
 * Created by Daniele on 16/05/2015.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.AnnuncioViewHolder>{
    private Activity activity;
    private ListaAnnunci annunci = new ListaAnnunci();

    private com.nostra13.universalimageloader.core.ImageLoader imageLoader;
    private DisplayImageOptions options;

    public void setListaAnnunci(ListaAnnunci annunci){
        this.annunci = annunci;
    }

    public RVAdapter(Activity activity) {
        this.activity = activity;

        initImageLoader();
    }

    @Override
    public AnnuncioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_layout, parent, false);
        AnnuncioViewHolder pvh = new AnnuncioViewHolder(v, activity);
        return pvh;
    }

    @Override
    public void onBindViewHolder(AnnuncioViewHolder annuncioHolder, int position) {
        String titolo = annunci.getAnnuncio(position).getLibro().getTitoloLibro();
        annuncioHolder.titoloLibro.setText(titolo);

        ArrayList<String> autori = (ArrayList<String>) annunci.getAnnuncio(position).getLibro().getAutoriLibro();
        String strAutori = "";
        if(autori.size() > 0) {
            strAutori = autori.get(0);
            int size = autori.size();
            for (int i = 1; i < size; i++) {
                strAutori += ", " + autori.get(i);
            }
            annuncioHolder.autoriLibro.setText(strAutori);
        }

        String URLImmagineCopertina = annunci.getAnnuncio(position).getLibro().getURLImmagineCopertina();
        //new GetBookThumb(annuncioHolder.fotoLibro).execute(URLImmagineCopertina);

        imageLoader.displayImage(URLImmagineCopertina, annuncioHolder.fotoLibro, options);

        String prezzo = Double.toString(annunci.getAnnuncio(position).getPrezzo());
        annuncioHolder.prezzoLibro.setText(prezzo);

        String descrizione = annunci.getAnnuncio(position).getLibro().getDescrizione();
        //annuncioHolder.customizeCard(titolo, strAutori, prezzo, URLImmagineCopertina, descrizione);
    }

    private void initImageLoader() {
        // Load image, decode it to Bitmap and display Bitmap in ImageView (or any other view
        //  which implements ImageAware interface)
        AnimationDrawable mAnimation = (AnimationDrawable) (
                (ImageView) activity.findViewById(R.id.loading_image)).getDrawable();

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(mAnimation)
                .showImageForEmptyUri(R.drawable.image_not_found)
                .showImageOnFail(R.drawable.image_not_found)
                //.delayBeforeLoading(2000) //da decrementare (serve per i test dell'animazione)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true).build();

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(activity));
    }

    @Override
    public int getItemCount() {
        return annunci.size();
    }

    public static class AnnuncioViewHolder extends RecyclerView.ViewHolder {
        private Activity activity;
        private TextView titoloLibro;

        private TextView autoriLibro;
        private TextView prezzoLibro;
        private ImageView fotoLibro;

        private CardView cardView;

        AnnuncioViewHolder(View itemView, Activity activity) {
            super(itemView);
            this.activity = activity;

            this.titoloLibro = (TextView) itemView.findViewById(R.id.book_title);
            this.autoriLibro = (TextView) itemView.findViewById(R.id.book_authors);
            this.prezzoLibro = (TextView) itemView.findViewById(R.id.book_price);
            this.fotoLibro = (ImageView) itemView.findViewById(R.id.book_image);
            //this.descrizioneLibro = (TextView) itemView.findViewById(R.id.book_description);

            cardView = (CardView)itemView.findViewById(R.id.expandable_card_view);
        }

        /*
        private void customizeCard(String titolo, String autori, String prezzo, String URLImmagineCopertina, String descrizione) {
            //Create a Card
            AnnuncioCard card = new AnnuncioCard(activity, titolo, autori, prezzo, URLImmagineCopertina, descrizione);

            //Animator listener
            card.setOnExpandAnimatorEndListener(new Card.OnExpandAnimatorEndListener() {
                @Override
                public void onExpandEnd(Card card) {
                    //Toast.makeText(getActivity(), "Expand " + card.getCardHeader().getTitle(), Toast.LENGTH_SHORT).show();
                }
            });

            //Set card in the cardView
            cardView.setCard(card);
        }
        */
    }

}
