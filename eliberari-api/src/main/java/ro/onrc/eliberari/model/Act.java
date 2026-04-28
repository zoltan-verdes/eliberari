package ro.onrc.eliberari.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.File;

public class Act {
   private final TipPagina tipPagina;
    private long numar;
    private String denumire_fisier;
    private int nrPaginiLot = 0;
    private int nrPaginiScanate = 0;
    private File fisierLot=null, fisierScanat=null;

    public Act(TipPagina tipPagina, String denumire) {
        this.tipPagina = tipPagina;
        this.denumire_fisier = denumire;
    }


    public Act(TipPagina tipPagina, int nrPaginiScanate, File fisierScanat) {
        this.tipPagina = tipPagina;
        this.nrPaginiScanate = nrPaginiScanate;
        this.fisierScanat = fisierScanat;
    }

    public Act(TipPagina tipPagina, String denumire, int nrPaginiLot, File fisier, long numar) {
        this.tipPagina = tipPagina;
        this.denumire_fisier = denumire;
        this.nrPaginiLot = nrPaginiLot;
        this.fisierLot = fisier;
        this.numar = numar;
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
