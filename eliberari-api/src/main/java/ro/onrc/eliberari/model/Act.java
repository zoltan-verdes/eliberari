package ro.onrc.eliberari.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.File;

public class Act {
    private final long numar;
    private final TipPagina tipPagina;
    private final String denumire_fisier;
    private int nrPaginiLot = 0;
    private int nrPaginiScanate = 0;
    private File fisierLot=null, fisierScanat=null;

    public Act(long numar, TipPagina tipPagina, String denumire) {
        this.numar = numar;
        this.tipPagina = tipPagina;
        this.denumire_fisier = denumire;
    }


    public Act(long numar, TipPagina tipPagina, String denumire, int nrPaginiLot, File fisier) {
        this.numar = numar;
        this.tipPagina = tipPagina;
        this.denumire_fisier = denumire;
        this.nrPaginiLot = nrPaginiLot;
        this.fisierLot = fisier;
    }

    public Act(long numar, TipPagina tipPagina, File fisier) {
        this.numar = numar;
        this.tipPagina = tipPagina;
        this.denumire_fisier = "";
        this.fisierScanat = fisier;
    }



    public TipPagina getTipPagina() { return tipPagina; }
    public String getDenumire_fisier() { return denumire_fisier; }
    public int getNrPaginiLot() { return nrPaginiLot; }
    public int getNrPaginiScanate() { return nrPaginiScanate; }
    public long getNumar() { return numar; }
    public void setNrPaginiScanate(int nr) { this.nrPaginiScanate = nr; }
    public void setFisierScanat(File fisier) { this.fisierScanat = fisier; }
    public File getFisierScanat() { return fisierScanat; }



 
    public boolean paginiCorespund() { 
        return nrPaginiLot > 0 && nrPaginiLot == nrPaginiScanate; 
    }

    public String getStatusPaginatie() { return nrPaginiScanate + "/" + nrPaginiLot; }
    @JsonIgnore
    public File getFisierLot() { return fisierLot; }
    public void setFisierLot(File fisier) { this.fisierLot = fisier; }

}
