package ro.onrc.eliberari.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Act {
    private final long numar;
    private final TipAct tipAct;
    private final String denumire_fisier;
    private int nrPagini = 0;


    public Act(long numar, TipAct tipPagina, String denumire) {
        this.numar = numar;
        this.tipAct = tipPagina;
        this.denumire_fisier = denumire;
    }

    @JsonCreator
    public Act(@JsonProperty("numar") long numar, 
               @JsonProperty("tipAct") TipAct tipAct,
               @JsonProperty("denumire_fisier") String denumire_fisier,
               @JsonProperty("nrPagini") int nrPagini){
        this.numar = numar;
        this.tipAct = tipAct;   
        this.denumire_fisier = denumire_fisier;
        this.nrPagini = nrPagini;   
    }

    public TipAct getTipAct() { return tipAct; }
    public String getDenumire_fisier() { return denumire_fisier; }
    public int getNrPagini() { return nrPagini; }
    public long getNumar() { return numar; }

}
