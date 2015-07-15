package com.unisa.unistore.model;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by Daniele on 15/05/2015.
 */
public class Annuncio {
    /**
     * Nessun potenziale acquirente ha segnalato l'intenzione di acquistare il libro
     */
    public static final int TRANSAZIONE_INIZIO = 0;
    /**
     * Un potenziale acquirente ha espresso l'intenzione di acquistare il libro
     */
    public static final int TRANSAZIONE_IN_TRATTATIVA = 1;
    /**
     * L'acquisto e' stato concordato, quindi il libro deve essere rimosso dagli annunci (puo' essere mantenuto sul cloud fino alla fine della transazione)
     */
    public static final int TRANSAZIONE_ACQUISTO_CONCORDATO = 2;
    /**
     * La transazione e' terminata con successo (se ancora presente sul cloud, l'annuncio deve essere rimosso completamente)
     */
    public static final int TRANSAZIONE_FINE = 3;
    private ParseObject autore;
    private Date data_pubblicazione;
    private Libro libro;
    private Number prezzo;

    public Annuncio() {
        libro = new Libro();
    }

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
