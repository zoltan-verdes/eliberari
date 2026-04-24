package ro.onrc.eliberari.model;

//import java.time.LocalDate;

/**
 * Această clasă "cară" datele de la OCR către restul aplicației.
 */
public class InfoPagina {
    private String numar;
    private String data; // Poți folosi String sau LocalDate dacă vrei să-l formatezi ulterior
    private String cui;
    private String firma;
    private TipPagina tipPag;
    private String barcode;

    // Constructor, Getters și Setters
    public InfoPagina(TipPagina tipPag) {
        this.tipPag = tipPag;
    }

    public InfoPagina(TipPagina tipPag, String numar, String data, String cui, String firma, String barcode) {
        this.tipPag = tipPag;
        this.numar = numar;
        this.data = data;
        this.cui = cui;
        this.firma = firma;
        this.barcode = barcode;
    }

    public InfoPagina(InfoPagina obCerere) {
        this.tipPag = obCerere.tipPag;
        this.numar = obCerere.numar;
        this.data = obCerere.data;
        this.cui = obCerere.cui;
        this.firma = obCerere.firma;
        this.barcode = "";
    }

    public String getNumar() {
        return numar;
    }

    public String getData() {
        return data;
    }

    public String getCui() {
        return cui;
    }

    public String getFirma() {
        return firma;
    }

    public TipPagina getTipPagina() {
        return tipPag;
    }

    public String getBarcode() {
        return barcode;
    }

    @Override
    public String toString() {
        return "DocumentInfo{tip" + tipPag + ",  numar='" + numar + "', data='" + data + ", cui:" + cui + ", firma:"
                + firma + "'}";
    }
}