package ro.onrc.eliberari.model;

import java.io.File;

public class InfoLot {
    private int nrCereri;
    private int nrActe;
    private int nrPagini;
    private File fisierScanat;


    public InfoLot(int nrCereri,int nrActe, int nrPagini) {
        this.nrCereri = nrCereri;
        this.nrActe = nrActe;
        this.nrPagini = nrPagini;
    }
    public int getNrCereri() {
        return nrCereri;
    }
    public int getNrActe(){
        return nrActe;
    }

    public int getNrPagini() {
        return nrPagini;
    }

    public File getFisierScanat() { return fisierScanat; }

    public void setFisierScanat(File fisier) {this.fisierScanat = fisier;}

}
