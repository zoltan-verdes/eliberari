package ro.onrc.eliberari.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Cerere implements Comparable<Cerere> {
    private final long numar;
    private final String data;
    private final String cui;
    private final String firma;
    private final List<Act> acte = new ArrayList<>();

    public Cerere(String numar, String data, String cui, String firma) {
        this.numar = parseLongSafely(numar);
        this.data = data;
        this.cui = cui;
        this.firma = firma;
    }

    public Cerere(InfoPagina infoPag) {
        this(infoPag.getNumar(), infoPag.getData(), infoPag.getCui(), infoPag.getFirma());
    }

    private long parseLongSafely(String s) {
        if (s == null) return 0;
        try {
            return Long.parseLong(s.trim().replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Getters pentru informațiile de bază
    public long getNumar() { return numar; }
    public String getData() { return data; }
    public String getCui() { return cui; }
    public String getFirma() { return firma; }

    public void addAct(Act nou) {
        TipPagina tip = nou.getTipPagina();
        if (tip == TipPagina.Incheiere || tip == TipPagina.CI || tip == TipPagina.CIM) {
            boolean dejaExista = acte.stream().anyMatch(a -> a.getTipPagina() == tip);
            if (dejaExista) {
                throw new IllegalStateException("Cererea are deja un act de tip " + tip);
            }
        }
        this.acte.add(nou);
    }

    /**
     * Returnează actele sortate: Incheiere, CI, CIM, Constatator, Altele
     */
    public List<Act> getActeOrdonate() {
        List<Act> sortate = new ArrayList<>(acte);
        sortate.sort(Comparator.comparingInt(a -> switch (a.getTipPagina()) {
            case Incheiere -> 1;
            case CI -> 2;
            case CIM -> 3;
            case Constatator -> 4;
            default -> 5;
        }));
        return sortate;
    }

    public List<Act> getToateActele() {
        return new ArrayList<>(acte);
    }

    public Act getIncheiere() { return findUnique(TipPagina.Incheiere); }
    public Act getCI() { return findUnique(TipPagina.CI); }
    public Act getCIM() { return findUnique(TipPagina.CIM); }
    public Act getAct(TipPagina tipPagina) { return findUnique(tipPagina); }
    
    public List<Act> getConstatatoare() {
        return acte.stream()
                .filter(a -> a.getTipPagina() == TipPagina.Constatator)
                .toList();
    }

    private Act findUnique(TipPagina tip) {
        return acte.stream()
                .filter(a -> a.getTipPagina() == tip)
                .findFirst()
                .orElse(null);
    }

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
}
