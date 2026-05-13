package ro.onrc.eliberari.model;

import java.util.ArrayList;
import java.util.List;

public class StivaCereri {
    private int numarPagini = 0;
    public List<CerereSimpla> lista = new ArrayList<CerereSimpla>();

    public StivaCereri(){
    }
    public StivaCereri (List<Act> listaActe){
        for (Act act: listaActe) {
            addAct(act);
        }
    }

    public void addCerere(CerereSimpla cerere){
        if (cerere==null) return;
        cerere.setPagInceput(numarPagini);
        lista.add(cerere);
        numarPagini += cerere.getNrPagTotal();
    }

    public void addAct(Act act){
//        System.out.println("Adaugam actul "+act.getNumar()+"-"+act.getTipAct());
        if ( act==null) return;
        if (lista.size()>0 && lista.getLast().getNumar()==act.getNumar()){
            lista.getLast().addAct(act);
            numarPagini += act.getNrPagini();
        }else
        {
            lista.add(new CerereSimpla(act, numarPagini));
            numarPagini += lista.getLast().getNrPagTotal();
        }
    }

    public List<CerereSimpla> getLista(){
        return lista;
    }

}
