package ro.onrc.eliberari.model;

public class CerereSimpla{
    private final long numar;
    private final String data;
    private int ci =0, cim =0, cc= 0, pagTotal = 0, pagInceput = 0;

    public CerereSimpla(String numar, String data) {
        this.numar = parseLongSafely(numar);
        this.data = data;
    }

    public CerereSimpla(long numar, String data) {
        this.numar = numar;
        this.data = data;
    }

    public CerereSimpla(long numar, String data, int pagInceput) {
        this.numar = numar;
        this.data = data;
        this.pagInceput = pagInceput;
    }

    public CerereSimpla(Act act){
        this.numar = act.getNumar();
        this.data = act.getData();
        addAct(act);
    }
    

    public CerereSimpla(Act act, int pagInceput){
        this.numar = act.getNumar();
        this.data = act.getData();
        this.pagInceput = pagInceput;
        addAct(act);
    }

    public long getNumar() { return numar; }
    public String getData() { return data; }
    public int getCI() { return ci; } 
    public int getCIM() { return cim; }
    public int getCC() { return cc; }

    public int getNrPagTotal(){ return pagTotal; }
    public int getPaginaInceput() { return pagInceput; }


    
    public void addAct(TipAct tip, int nrPagini) {
        switch (tip) {
            case CI -> {ci = 1; pagTotal++;}
            case CIM -> {cim = 1; pagTotal++;}
            case Constatator -> {cc+=nrPagini; pagTotal+=nrPagini;}
            default -> {;}
        }
    }

    public int addAct(Act act){
           int nrPag = 0;
            switch (act.getTipAct()) {
                case CI -> {nrPag = ci = 1; pagTotal++; }
                case CIM -> {nrPag = cim = 1; pagTotal++;}
                case Constatator -> {cc+=act.getNrPagini(); pagTotal+=act.getNrPagini();}
                default -> {;}
            }
            return nrPag;
    }

    
    public void setPagInceput(int pagInceput){
        this.pagInceput =  pagInceput;
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
