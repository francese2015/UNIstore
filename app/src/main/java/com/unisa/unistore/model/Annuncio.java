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

    public Annuncio(ParseUser autore_libro, Date data_pubblicazione_libro, Libro libro) {
        this.autore = autore_libro;
        this.data_pubblicazione = data_pubblicazione_libro;
        this.libro = libro;
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
}
