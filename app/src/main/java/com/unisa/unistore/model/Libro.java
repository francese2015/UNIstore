package com.unisa.unistore.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Daniele on 15/05/2015.
 */
public class Libro {

    private String titolo;
    private ArrayList<String> autori;
    private URL url_immagine_copertina;
    private Date data_pubblicazione;

    public Libro(String titolo_libro,
                 ArrayList<String> autori_libro, URL url_immagine_copertina,
                 Date data_pubblicazione_annuncio) {
        this.titolo = titolo_libro;
        this.autori = autori_libro;
        this.url_immagine_copertina = url_immagine_copertina;
        this.data_pubblicazione = data_pubblicazione_annuncio;
    }

    public void setTitoloLibro(String titolo_libro) {
        this.titolo = titolo_libro;
    }

    public String getTitoloLibro() {
        return titolo;
    }

    public void setAutoriLibro(ArrayList<String> autori_libro) {
        this.autori = autori_libro;
    }

    public ArrayList<String> getAutoriLibro() {
        return autori;
    }

    public void setUrlImmagineCopertina(URL url_immagine_copertina) {
        this.url_immagine_copertina = url_immagine_copertina;
    }

    public URL getUrlImmagineCopertina() {
        return url_immagine_copertina;
    }

    public void setDataPubblicazioneAnnuncio(Date data_pubblicazione_annuncio) {
        this.data_pubblicazione = data_pubblicazione_annuncio;
    }

    public Date getDataPubblicazioneAnnuncio() {
        return data_pubblicazione;
    }
}
