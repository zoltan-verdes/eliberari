package ro.onrc.eliberari.model;


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

    public Act(long numar, TipAct tipPagina, String denumire, int nrPagini) {
        this.numar = numar;
        this.tipAct = tipPagina;
        this.denumire_fisier = denumire;
        this.nrPagini = nrPagini;
    }

    public TipAct getTipAct() { return tipAct; }
    public String getDenumire_fisier() { return denumire_fisier; }
    public int getNrPagini() { return nrPagini; }
    public long getNumar() { return numar; }

}
