package ro.onrc.eliberari.model;

import java.util.List;

/**
 * Wrapper (DTO) pentru clasa Cerere, optimizat pentru răspunsul către UI.
 */
public class CerereDTO {
    public long numar;
    public String data;
    public String firma;
    public String cui;

    public StatusActDTO incheiere;
    public StatusActDTO ci;
    public StatusActDTO cim;
    public StatusActDTO cc;

    public static class StatusActDTO {
        public int nrPagLot;
        public int nrPagScanat;
        public boolean complet; // true dacă nrPagLot == nrPagScanat

        public StatusActDTO(int nrPagLot, int nrPagScanat) {
            this.nrPagLot = nrPagLot;
            this.nrPagScanat = nrPagScanat;
            this.complet = nrPagLot == nrPagScanat;
        }
    }

    public CerereDTO(Cerere c) {
        this.numar = c.getNumar();
        this.data = c.getData();
        this.firma = c.getFirma();
        this.cui = c.getCui();

        // Mapăm actele unice (Incheiere, CI, CIM)
        this.incheiere = toStatus(c.getIncheiere());
        this.ci = toStatus(c.getCI());
        this.cim = toStatus(c.getCIM());

        // Calculăm CC ca suma tuturor actelor de tip Constatator (Constatatoare)
        List<Act> listCC = c.getConstatatoare();
        if (listCC != null && !listCC.isEmpty()) {
            int totalLot = 0;
            int totalScanat = 0;
            for (Act a : listCC) {
                totalLot += a.getNrPaginiLot();
                totalScanat += a.getNrPaginiScanate();
            }
            this.cc = new StatusActDTO(totalLot, totalScanat);
        }
    }

    private StatusActDTO toStatus(Act a) {
        if (a == null) return null;
        return new StatusActDTO(a.getNrPaginiLot(), a.getNrPaginiScanate());
    }
}