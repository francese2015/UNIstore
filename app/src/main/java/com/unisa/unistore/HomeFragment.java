package com.unisa.unistore;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.parse.ParseUser;
import com.unisa.unistore.adapter.RVAdapter;
import com.unisa.unistore.model.ListaAnnunci;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;
import it.gmariotti.cardslib.library.view.CardView;

public class HomeFragment extends Fragment {

    private MainActivity activity;
    private Toolbar toolbar;
    private ActionBar supportActionBar;

    private boolean mListShown;
    protected View mProgressContainer;
    protected View mListContainer;
    private CardArrayRecyclerViewAdapter mCardArrayAdapter;

    public HomeFragment(){}

    public void setActivity(MainActivity a) {
        activity = a;
    }

    public void setToolbar(Toolbar t) {
        toolbar = t;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RecyclerView rv = (RecyclerView) getActivity().findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        ListaAnnunci la = new ListaAnnunci();
        la.initializeData();

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity);
        rv.setLayoutManager(mLayoutManager);

        RVAdapter adapter = new RVAdapter(la);
        rv.setAdapter(adapter);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);
        // update the actionbar to show the up carat/affordance
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        ImageButton fab_aggiungiAnnuncio = (ImageButton) activity.findViewById(R.id.fab_image_button);
        fab_aggiungiAnnuncio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(ParseUser.getCurrentUser() != null) {
                    Intent intent = new Intent(getActivity(), PubblicaAnnuncioActivity.class);
                    getActivity().startActivityForResult(intent, 12345);
                } else {
                    Context context = activity.getApplicationContext();
                    CharSequence text = getString(R.string.profile_title_logged_out);
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });

        /*
        Utilities utilities = new Utilities();
        utilities.scaleImage((ImageView) activity.findViewById(R.id.bookPhoto));
        utilities.scaleImage((ImageView) activity.findViewById(R.id.bookPhoto2));
        utilities.scaleImage((ImageView) activity.findViewById(R.id.bookPhoto3));
        utilities.scaleImage((ImageView) activity.findViewById(R.id.bookPhoto4));
        */
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get item selected and deal with it
        switch (item.getItemId()) {
            case android.R.id.home:
                //called when the up affordance/carat in actionbar is pressed
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setToolbar(ActionBar supportActionBar) {
        this.supportActionBar = supportActionBar;
    }

    /*
    private class LoaderAsyncTask extends AsyncTask<Void, Void, ArrayList<Card>> {

        LoaderAsyncTask() {
        }

        @Override
        protected ArrayList<Card> doInBackground(Void... params) {
            //elaborate images
            ArrayList<Card> cards = initCards();
            return cards;
        }

        @Override
        protected void onPostExecute(ArrayList<Card> cards) {
            CardArrayRecyclerViewAdapter mCardArrayAdapter = new CardArrayRecyclerViewAdapter(getActivity(), cards);

            //Staggered grid view
            CardRecyclerView mRecyclerView = (CardRecyclerView) getActivity().findViewById(R.id.carddemo_recyclerview);
            mRecyclerView.setHasFixedSize(false);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            //Set the empty view
            if (mRecyclerView != null) {
                mRecyclerView.setAdapter(mCardArrayAdapter);
            }
        }
    }
*/
    private Card initCard() {
        //Create a Card
        Card card = new Card(getActivity());

        //Create a CardHeader
        CardHeader header = new CardHeader(getActivity());

        //Set visible the expand/collapse button
        header.setButtonExpandVisible(true);

        //Set the header title
        header.setTitle("Book Title");

        //Add ClickListener
        card.setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Toast.makeText(getActivity(), "Click Listener card=" + card.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        //Set visible the expand/collapse button
        header.setButtonExpandVisible(true);

        //Add Header to card
        card.addCardHeader(header);

        //This provides a simple (and useless) expand area
        CardExpand expand = new CardExpand(getActivity());
        //Set inner title in Expand Area
        expand.setTitle("Card Expand Title");
        card.addCardExpand(expand);

        //Set card in the cardView
        CardView cardView = (CardView) getActivity().findViewById(R.id.expandable_card_view);
        cardView.setCard(card);

        return card;
    }

    private ArrayList<Card> initCards() {
        //Init an array of Cards
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < 200; i++) {
            Card card = initCard();
            cards.add(card);
        }

        return cards;
    }

}
