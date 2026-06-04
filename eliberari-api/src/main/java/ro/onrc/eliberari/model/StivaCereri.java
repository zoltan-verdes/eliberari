package ro.onrc.eliberari.model;

import java.util.ArrayList;
import java.util.List;

public class StivaCereri {
    public int numarPagini = 0, nr_acte = 0;
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
        cerere.setPagInceput(numarPagini+1);
        lista.add(cerere);
        numarPagini += cerere.getNrPagTotal();
        nr_acte += cerere.getNrActe();
    }

    public void addAct(Act act){
//        System.out.println("Adaugam actul "+act.getNumar()+"-"+act.getTipAct());
        if ( act==null) return;
        if ( act.getTipAct() != TipAct.CI && act.getTipAct() != TipAct.CIM && act.getTipAct() != TipAct.Constatator) return;
        nr_acte++;
        if (lista.size()>0 && lista.getLast().getNumar()==act.getNumar()){
            lista.getLast().addAct(act);
            numarPagini += act.getNrPagini();
        }else
        {
            lista.add(new CerereSimpla(act, numarPagini+1));
            numarPagini += lista.getLast().getNrPagTotal();
        }
    }

    public List<CerereSimpla> getLista(){
        return lista;
    }

    public InfoLot getInfo(){
        return new InfoLot(lista.size(), nr_acte, numarPagini);
    }


}
