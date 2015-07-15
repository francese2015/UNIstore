package com.unisa.unistore.model;

import com.parse.ParseFile;

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
    private ParseFile fileFoto;
    private String data_pubblicazione;
    private String descrizione;

    public Libro() {
        this.autori = new ArrayList<>();
    }

    public Libro(String id_libro, String titolo_libro,
                 List<String> autori_libro, String url_immagine_copertina, String descrizione,
                 String data_pubblicazione) {
        this.id = id_libro;
        this.titolo = titolo_libro;
        if(autori_libro.isEmpty())
            this.autori = new ArrayList<>();
        else
            this.autori = autori_libro;
        this.url_immagine_copertina = url_immagine_copertina;
        this.descrizione = descrizione;
        this.data_pubblicazione = data_pubblicazione;
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

    public void addAutoriLibro(String autori_libro) {
        this.autori.add(autori_libro);
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

    public void setFileFoto(ParseFile fileFoto) {
        this.fileFoto = fileFoto;
    }

    public ParseFile getFileFoto() {

        return fileFoto;
    }

    public void setDataPubblicazione(String data_pubblicazione) {
        this.data_pubblicazione = data_pubblicazione;
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
