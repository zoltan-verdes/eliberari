package ro.onrc.eliberari.model;

public class InfoLot {
    private int nrCereri;
    private int nrActe;
    private int nrPagini;


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

}
