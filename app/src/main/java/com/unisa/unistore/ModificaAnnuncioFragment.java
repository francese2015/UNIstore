package com.unisa.unistore;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ModificaAnnuncioFragment extends Fragment {
	
	public ModificaAnnuncioFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_modificaannuncio, container, false);
         
        return rootView;
    }
}
