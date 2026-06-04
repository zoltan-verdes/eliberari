package ro.onrc.eliberari.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Act {
    private final long numar;
    private final TipAct tipAct;
    private final String denumireFisier;
    private String data="";
    private int nrPagini = 0;


    public Act(long numar, TipAct tipPagina, String denumire) {
        this.numar = numar;
        this.tipAct = tipPagina;
        this.denumireFisier = denumire;
    }

    public Act(long numar, TipAct tipAct, String denumire_fisier, int nrPagini){
        this.numar = numar;
        this.tipAct = tipAct;   
        this.denumireFisier = denumire_fisier;
        this.nrPagini = nrPagini;   
    }

    @JsonCreator
    public Act(@JsonProperty("numar") long numar, 
               @JsonProperty("data") String data, 
               @JsonProperty("tipAct") TipAct tipAct,
               @JsonProperty("denumire_fisier") String denumire_fisier,
               @JsonProperty("nrPagini") int nrPagini){
        this.numar = numar;
        this.tipAct = tipAct;   
        this.denumireFisier = denumire_fisier;
        this.nrPagini = nrPagini;   
        this.data = data;
    }

    public TipAct getTipAct() { return tipAct; }
    public String getDenumireFisier() { return denumireFisier; }
    public int getNrPagini() { return nrPagini; }
    public long getNumar() { return numar; }
    public String getData() {return data;}

}
