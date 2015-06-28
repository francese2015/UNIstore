package com.unisa.unistore.model;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by Daniele on 15/05/2015.
 */
public class Annuncio {
    private ParseObject autore;
    private Date data_pubblicazione;
    private Libro libro;
    private Number prezzo;

    public Annuncio(ParseUser autore_annuncio, Date data_pubblicazione_annuncio, Libro libro, Number prezzo) {
        this.autore = autore_annuncio;
        this.data_pubblicazione = data_pubblicazione_annuncio;
        this.libro = libro;
        this.prezzo = prezzo;
    }

    public void setAutoreAnnuncio(ParseObject autore_annuncio) {
        this.autore = autore_annuncio;
    }

    public ParseObject getAutoreAnnuncio() {
        return autore;
    }

    public void setDataPubblicazioneAnnuncio(Date data_pubblicazione_annuncio) {
        this.data_pubblicazione = data_pubblicazione_annuncio;
    }

    public Date getDataPubblicazioneAnnuncio() {
        return data_pubblicazione;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    public Libro getLibro() {
        return libro;
    }

    public Number getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(double prezzo) {
        this.prezzo = prezzo;
    }
}
