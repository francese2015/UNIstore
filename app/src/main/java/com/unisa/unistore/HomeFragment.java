package com.unisa.unistore;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseUser;

public class HomeFragment extends Fragment {

    private MainActivity activity;
    private Toolbar toolbar;
    private ActionBar supportActionBar;

    private ParseUser currentUser;

    public HomeFragment(){}

    public void setActivity(MainActivity a) {
        activity = a;
    }

    public void setToolbar(Toolbar t) {
        toolbar = t;
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        currentUser = ParseUser.getCurrentUser();

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
                if(currentUser != null) {
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

        Utilities utilities = new Utilities();

        utilities.scaleImage((ImageView) activity.findViewById(R.id.bookPhoto));
        utilities.scaleImage((ImageView) activity.findViewById(R.id.bookPhoto2));
        utilities.scaleImage((ImageView) activity.findViewById(R.id.bookPhoto3));
        utilities.scaleImage((ImageView) activity.findViewById(R.id.bookPhoto4));
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
}
