package com.unisa.unistore;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class HomeFragment extends Fragment {

    private MainActivity activity;

    public HomeFragment(){}

    public void setActivity(MainActivity a) {
        activity = a;
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        ImageButton fab_aggiungiAnnuncio = (ImageButton) activity.findViewById(R.id.fab_aggiungiAnnuncio);
        fab_aggiungiAnnuncio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.layout_root_view, new PubblicaFragment())
                        // Add this t.the front of the card.
                        .addToBackStack(null)
                        .commit();
            }
        });
    }
}
