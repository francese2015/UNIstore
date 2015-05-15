package com.unisa.unistore.model;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Daniele on 15/05/2015.
 */
public class ListaAnnunci extends ParseObject {
    private ArrayList<Annuncio> lista_annunci;

    public ListaAnnunci(ArrayList<Annuncio> lista_annunci) {
        this.lista_annunci = lista_annunci;
    }

    public void setListaAnnunci(ArrayList<Annuncio> lista_annunci) {
        this.lista_annunci = lista_annunci;
    }

    public ArrayList<Annuncio> getListaAnnunci() {
        return lista_annunci;
    }

    public int size() { return lista_annunci.size(); }

    public boolean isEmpty() { return lista_annunci.isEmpty(); }

    public boolean addAnnuncio(Annuncio annuncio) {
        return lista_annunci.add(annuncio);
    }

    public Annuncio setAnnuncio(int i, Annuncio annuncio) {
        return lista_annunci.set(i, annuncio);
    }

    public Annuncio remove(int i) {
        return lista_annunci.remove(i);
    }

    /**
     * Cerca un annuncio in base al titolo del libro che si vuole trovare.
     * @param titolo Il titolo del libro che si sta cercando.
     * @return Una lista di annunci a cui corrisponde il titolo cercato.
     */
    public ArrayList<Annuncio> findAnnuncioPerTitolo(String titolo) {
        Iterator<Annuncio> iterator = null;
        Annuncio tmp_annuncio = null;
        ArrayList<Annuncio> annunci = new ArrayList<Annuncio>();

        for(iterator = lista_annunci.iterator(); iterator.hasNext(); tmp_annuncio = iterator.next()) {
            if(tmp_annuncio.getLibro().getTitoloLibro().equalsIgnoreCase(titolo))
                annunci.add(tmp_annuncio);
        }
        
        return annunci;
    }
}
