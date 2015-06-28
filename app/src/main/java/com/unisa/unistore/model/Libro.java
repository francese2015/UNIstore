package com.unisa.unistore.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniele on 15/05/2015.
 */
public class Libro {
    private String id;
    private String titolo;
    private List<String> autori;
    private String url_immagine_copertina;
    private String data_pubblicazione;
    private String descrizione;

    public Libro(String id_libro, String titolo_libro,
                 List<String> autori_libro, String url_immagine_copertina, String descrizione,
                 String data_pubblicazione_annuncio) {
        this.id = id_libro;
        this.titolo = titolo_libro;
        this.autori = autori_libro;
        this.url_immagine_copertina = url_immagine_copertina;
        this.descrizione = descrizione;
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

    public List<String> getAutoriLibro() {
        return autori;
    }

    public void setUrlImmagineCopertina(String url_immagine_copertina) {
        this.url_immagine_copertina = url_immagine_copertina;
    }

    public String getURLImmagineCopertina() {
        return url_immagine_copertina;
    }

    public void setDataPubblicazioneAnnuncio(String data_pubblicazione_annuncio) {
        this.data_pubblicazione = data_pubblicazione_annuncio;
    }

    public String getDataPubblicazioneAnnuncio() {
        return data_pubblicazione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getIDLibro() {
        return id;
    }
}
