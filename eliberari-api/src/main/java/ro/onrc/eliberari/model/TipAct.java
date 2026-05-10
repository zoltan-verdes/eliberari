package ro.onrc.eliberari.model;

public enum TipAct {
    Incheiere,
    Constatator,
    CIM,
    CI,
    ListaVerificare,
    PagGoala,
    Altele;

    public String formaScurta() {
        switch (this) {
            case Incheiere:
                return "INCH";
            case Constatator:
                return "CC";
            case CIM:
                return "CIM";
            case CI:
                return "CI";
            case ListaVerificare:
                return "LV";
            default:
                return this.name();
        }
        
    }

    public int getPrioritate() {
        switch (this) {
            case Incheiere:
                return 1;
            case CI:
                return 2;
            case CIM:
                return 3;
            case Constatator:
                return 4;
            case ListaVerificare:
                return 5;
            default:
                return Integer.MAX_VALUE; // Prioritate foarte mică pentru tipuri necunoscute
        }
    }
}
