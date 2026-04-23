package ro.onrc.eliberari.model;

public class Cerere {
    private String numar;
    private String data; // Poți folosi String sau LocalDate dacă vrei să-l formatezi ulterior
    private String cui;
    private String firma;
    private Boolean inch, ci, cim;
    private int cc;
    
    public Cerere(String numar, String data, String cui, String firma) {
        this.numar = numar;
        this.data = data;
        this.cui = cui;
        this.firma = firma;
        this.inch = false;
        this.ci = false;
        this.cim = false;
        this.cc = 0;
    }

    public Cerere(InfoPagina infoPag) {
        this.numar = infoPag.getNumar();
        this.data = infoPag.getData();
        this.cui = infoPag.getCui();
        this.firma = infoPag.getFirma();
        this.inch = false;
        this.ci = false;
        this.cim = false;
        this.cc = 0;
    }


    public String getNumar() {return numar; }
    public void setNumar(String numar) {this.numar = numar;}

    public String getData() {return data;}
    public void setData(String data) {this.data = data;}

    public String getCui() {return cui;}
    public void setCui(String cui) {this.cui = cui;}

    public String getFirma() {return firma;}
    public void setFirma(String firma) {this.firma = firma;}

    public Boolean getInch() {return inch;}
    public void setInch(Boolean incheiere) {this.inch = incheiere;}

    public Boolean getCi() {return ci;}
    public void setCi(Boolean ci) {this.ci = ci;}

    public Boolean getCim() {return cim;}
    public void setCim(Boolean cim) {this.cim = cim;}

    public int getCc() {return cc;}
    public void setCc(int cc) {this.cc = cc;}
    public void addCc() {this.cc += 1;}

}
