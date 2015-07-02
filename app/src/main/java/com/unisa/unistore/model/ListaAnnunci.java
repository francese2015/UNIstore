package com.unisa.unistore.model;

import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Daniele on 15/05/2015.
 */
public class ListaAnnunci {
    private ArrayList<Annuncio> lista_annunci;

    public ListaAnnunci(ArrayList<Annuncio> lista_annunci) {
        this.lista_annunci = lista_annunci;
    }

    public ListaAnnunci() {
        lista_annunci = new ArrayList<Annuncio>();
    }

    public void setListaAnnunci(ArrayList<Annuncio> lista_annunci) {
        this.lista_annunci = lista_annunci;
    }

    public ArrayList<Annuncio> getListaAnnunci() {
        return lista_annunci;
    }

    public int size() { return lista_annunci.size(); }

    public boolean isEmpty() { return lista_annunci.isEmpty(); }

    public Annuncio getAnnuncio(int i) { return lista_annunci.get(i); }

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

    /**
     * Metodo da usare per i test.
     */
    public void initializeData(){
        ArrayList<String> autori = new ArrayList<>();
        autori.add("Deitel");
        autori.add("Manzoni");
        Libro l1 = new Libro("id1", "titolo1", autori, "http://bks2.books.google.it/books/content?id=eZVvPgAACAAJ&printsec=frontcover&img=1&zoom=1&source=gbs_api",
                "Questa è una descrizione", new Date().toString());
        Libro l2 = new Libro("id2", "titolo2", autori, "http://bks2.books.google.it/books/content?id=eZVvPgAACAAJ&printsec=frontcover&img=1&zoom=1&source=gbs_api",
                "Questa è un'altra descrizione", new Date().toString());
        Libro l3 = new Libro("id3", "titolo3", autori, "http://bks2.books.google.it/books/content?id=eZVvPgAACAAJ&printsec=frontcover&img=1&zoom=1&source=gbs_api",
                "Questa è ancora un'altra descrizione", new Date().toString());

        Annuncio a1 = new Annuncio(ParseUser.getCurrentUser(), new Date(), l1, 30);
        Annuncio a2 = new Annuncio(ParseUser.getCurrentUser(), new Date(), l2, 30);
        Annuncio a3 = new Annuncio(ParseUser.getCurrentUser(), new Date(), l3, 30);

        lista_annunci.add(a1);
        lista_annunci.add(a2);
        lista_annunci.add(a3);
    }

    public void addAnnunci(List<ParseObject> scoreList, boolean onTop) {
        String id = "";
        String titolo = "";
        JSONArray autori = new JSONArray();
        ArrayList<String> lista_autori = null;
        int i, size = 0;
        String url = "";
        String data = "";
        ParseUser autoreAnnuncio = null;
        Libro libro = null;

        for(ParseObject libroParse : scoreList) {
            id = libroParse.getObjectId();
            titolo = libroParse.getString("titolo");

            lista_autori = new ArrayList<>();
            autori = libroParse.getJSONArray("autori");
            if(autori != null) {
                try {
                    size = autori.length();
                    for (i = 0; i < size; i++)
                        lista_autori.add(autori.get(i).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            lista_autori.add(libroParse.getString("autori").toString());
            url = libroParse.getString("url_immagine_copertina");
            String descrizione = libroParse.getString("descrizione");
            data = libroParse.getString("data");

            libro = new Libro(id, titolo, lista_autori, url, descrizione, data);

            autoreAnnuncio = (ParseUser) libroParse.getParseObject("autore_annuncio");
            Date date = libroParse.getDate("updatedAt");
            Number prezzo = libroParse.getNumber("prezzo_annuncio");

            Annuncio annuncio = new Annuncio(autoreAnnuncio, date, libro, prezzo);

            if(onTop)
                lista_annunci.add(0, annuncio);
            else
                lista_annunci.add(annuncio);
        }

    }

    public void clear() {
        lista_annunci.clear();
    }
}
