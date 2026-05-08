package ro.onrc.eliberari.model;

import java.util.Objects;

public class Cerere implements Comparable<Cerere> {
    private final long numar;
    private final String data;
    private final String cui;
    private final String firma;
    private boolean inch=false,ci=false,cim=false;
    private int cc= 0, pagTotal = 0;

    public Cerere(String numar, String data, String cui, String firma) {
        this.numar = parseLongSafely(numar);
        this.data = data;
        this.cui = cui;
        this.firma = firma;
    }

    public Cerere(InfoPagina infoPag) {
        this(infoPag.getNumar(), infoPag.getData(), infoPag.getCui(), infoPag.getFirma());
    }

 
    // Getters pentru informațiile de bază
    public long getNumar() { return numar; }
    public String getData() { return data; }
    public String getCui() { return cui; }
    public String getFirma() { return firma; }


    /**
     * Returnează actele sortate: Incheiere, CI, CIM, Constatator, Altele
     */
    

    public void addAct(TipPagina tip, int nrPagini) {
        switch (tip) {
            case Incheiere -> {inch = true;}
            case CI -> {ci = true; pagTotal++;}
            case CIM -> {cim = true; pagTotal++;}
            case Constatator -> {cc+=nrPagini; pagTotal+=nrPagini;}
            default -> {;}
        }
    }

    public boolean getInch() { return inch; }
    public boolean getCI() { return ci; } 
    public boolean getCIM() { return cim; }
    public int getCC() { return cc; }
    public int getNrPagTotal() { return pagTotal; }
    
    @Override
    public int compareTo(Cerere o) {
        return Long.compare(this.numar, o.numar);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cerere cerere = (Cerere) o;
        return numar == cerere.numar;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numar);
    }

    private long parseLongSafely(String s) {
        if (s == null) return 0;
        try {
            return Long.parseLong(s.trim().replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
