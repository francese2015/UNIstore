package com.unisa.unistore.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.unisa.unistore.MainActivity;
import com.unisa.unistore.NoticeDetailActivity;
import com.unisa.unistore.R;
import com.unisa.unistore.model.ListaAnnunci;
import com.unisa.unistore.utilities.ImageUtilities;
import com.unisa.unistore.utilities.Utilities;

import java.util.ArrayList;
import java.util.List;


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

    private MainActivity activity;
    private ListaAnnunci annunci = new ListaAnnunci();

    private com.nostra13.universalimageloader.core.ImageLoader imageLoader;
    private DisplayImageOptions options;
    private boolean isCancellaAnnuncioButtonVisible = false;

    public void setListaAnnunci(ListaAnnunci annunci){
        this.annunci = annunci;
    }

    public RVAdapter(Activity activity) {
        this.activity = (MainActivity) activity;
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

    public void setCancellaAnnuncioButtonVisible(boolean isVisible) {
        this.isCancellaAnnuncioButtonVisible = isVisible;
    }

    public class AnnuncioViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "AnnuncioViewHolder";
        private Activity mainActivity;

        private ImageButton buttonCancellaLibro;

        private TextView idLibro;
        private TextView titoloLibro;
        private TextView autoriLibro;
        private TextView prezzoLibro;
        private ImageView fotoLibro;

        private CardView cardView;
        private String URLImmagineCopertina;

        AnnuncioViewHolder(final View itemView, Activity activity) {
            super(itemView);
            this.mainActivity = activity;

            this.idLibro = (TextView) itemView.findViewById(R.id.book_id);
            this.titoloLibro = (TextView) itemView.findViewById(R.id.book_title);
            this.autoriLibro = (TextView) itemView.findViewById(R.id.book_authors);
            this.prezzoLibro = (TextView) itemView.findViewById(R.id.book_price);
            this.fotoLibro = (ImageView) itemView.findViewById(R.id.take_book_photo);
            //this.descrizioneLibro = (TextView) itemView.findViewById(R.id.book_description);

            if(isCancellaAnnuncioButtonVisible) {
                this.buttonCancellaLibro = (ImageButton) itemView.findViewById(R.id.delete_button);

                buttonCancellaLibro.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancellaAnnuncio();
                    }
                });
                buttonCancellaLibro.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View view) {
                        Vibrator vibe = (Vibrator) mainActivity.getSystemService(Context.VIBRATOR_SERVICE);
                        vibe.vibrate(100);
                        Toast.makeText(mainActivity, view.getContentDescription(), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

                buttonCancellaLibro.setVisibility(View.VISIBLE);
            }

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
                    Intent i = new Intent(mainActivity, NoticeDetailActivity.class);

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

                    Pair<TextView, String> titoloLibroPair = Pair.create(titoloLibro, mainActivity.getString(R.string.title_detail_transition_name));
                    Pair<TextView, String> autoriLibroPair = Pair.create(autoriLibro, mainActivity.getString(R.string.authors_detail_transition_name));
                    Pair<TextView, String> prezzoLibroPair = Pair.create(prezzoLibro, mainActivity.getString(R.string.price_detail_transition_name));
                    Pair<ImageView, String> fotoLibroPair = Pair.create(fotoLibro, mainActivity.getString(R.string.image_detail_transition_name));
                    Pair<View, String>[] pairs = new Pair[]{titoloLibroPair, autoriLibroPair, prezzoLibroPair, fotoLibroPair};

                    ActivityOptionsCompat transitionActivityOptions =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(mainActivity, pairs);
                    ActivityCompat.startActivity(mainActivity, i, transitionActivityOptions.toBundle());
                }
            });
        }

        public void setURLImmagineCopertina(String URLImmagineCopertina) {
            this.URLImmagineCopertina = URLImmagineCopertina;
        }

        private void cancellaAnnuncio() {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Libri");
            query.whereEqualTo("objectId", idLibro.getText().toString());

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (e == null) {
                        int size = list.size();
                        for (int i = 0; i < size; i++) {
                            list.get(i).deleteEventually(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        List<ParseObject> users_online = null;
                                        try {
                                            users_online = ParseQuery.getQuery("Users_Online").whereEqualTo("online", true).find();
                                            for(ParseObject user_online : users_online) {
                                                user_online.add("annunci_eliminati", idLibro.getText().toString());
                                                if(user_online.getObjectId().equals(ParseUser.getCurrentUser().getObjectId()))
                                                    user_online.save();
                                                else
                                                    user_online.saveEventually();
                                            }
                                        } catch (ParseException e1) {
                                            Log.d(TAG, "list.get(i).deleteEventually()/Si è verificato un'errore: " + e.getMessage());
                                            e1.printStackTrace();
                                        }
                                        Log.d(TAG, "Annuncio " + idLibro.getText().toString() + " eliminato");
                                        activity.refresh();
                                        Toast.makeText(mainActivity, "Annuncio cancellato", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(mainActivity, R.string.contact_administrator, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    } else {
                        Toast.makeText(mainActivity, R.string.contact_administrator, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

}
