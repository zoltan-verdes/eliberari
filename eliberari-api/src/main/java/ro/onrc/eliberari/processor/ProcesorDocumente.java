package ro.onrc.eliberari.processor;

import ro.onrc.eliberari.LogListener;
import ro.onrc.eliberari.config.AppConfig;
import ro.onrc.eliberari.model.Cerere;
import ro.onrc.eliberari.model.InfoPagina;
import ro.onrc.eliberari.model.TipPagina;
import ro.onrc.eliberari.service.DocumentOptimizer;
import ro.onrc.eliberari.service.PdfService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProcesorDocumente {

    private final PdfService pdfService;
    private final ProcesorPagina procPagina;
    private final AppConfig config;
    private BufferedImage cod_ci;
    private DocumentOptimizer docOptimezer;

    // Spring injectează automat serviciile prin constructor
    public ProcesorDocumente(ProcesorPagina procPagina, PdfService pdfService, AppConfig config,
            DocumentOptimizer documentOptimizer) {
        this.procPagina = procPagina;

        this.pdfService = pdfService;
        this.config = config;
        this.docOptimezer = documentOptimizer;
    }

    public List<Cerere> proceseaza(File fisier, LogListener listener) throws Exception {

        List<Cerere> listaCereri = new ArrayList<>();
        File fisier_optimizat = docOptimezer.optimizeazaSiCurata(fisier);
        try (PDDocument document = PDDocument.load(fisier)) {
            List<Integer> paginiCurente = new ArrayList<>();

            int contorDocumente = 1;
            listener.onLog("Avem un document cu " + document.getNumberOfPages() + " pagini");
            InfoPagina infoPag;

            for (int i = 0; i < document.getNumberOfPages(); i++) {

                var imagine = pdfService.randeazaPagina(document, i);
                listener.onLog("procesam pagina " + i + " dimensiunea (" + imagine.getWidth() + ","
                        + imagine.getHeight() + ")");

                infoPag = procPagina.prelucrarePagina(imagine);

                if (infoPag.getTipPagina() != TipPagina.PagGoala) {
                    System.out.println("Actul  -- " + infoPag.getTipPagina());
                    listener.onLog("Pagina tip  -- " + infoPag.getTipPagina());
                    listener.onLog("Barcode  -- " + infoPag.getBarcode());

                    if (infoPag.getTipPagina() == TipPagina.Incheiere) {
                        if (!paginiCurente.isEmpty() && !listaCereri.isEmpty()) {
                            Cerere cerere = listaCereri.getLast();
                            listener.onLog("Salvam: " + cerere.getNumar() + "_" + cerere.getData() + "_"
                                    + cerere.getFirma() + "_" + paginiCurente.size() + ".pdf");
                            pdfService.salveazaGrupPagini(document, paginiCurente, cerere.getNumar() + "_"
                                    + cerere.getData() + "_" + cerere.getFirma() + "_" + paginiCurente.size() + ".pdf");
                            contorDocumente++;
                            paginiCurente.clear();
                        }

                        listaCereri.add(new Cerere(infoPag));
                    }

                    if (infoPag.getTipPagina() != TipPagina.Altele) {
                        listener.onLog("pagina de " + infoPag.getTipPagina());
                        listener.onLog("info: nr." + infoPag.getNumar() + "/" + infoPag.getData() + "  Firma:"
                                + infoPag.getFirma() + "  CUI:" + infoPag.getCui());
                        if (infoPag.getTipPagina() == TipPagina.Incheiere)
                            listaCereri.getLast().setInch(true);
                        if (infoPag.getTipPagina() == TipPagina.CI)
                            listaCereri.getLast().setCi(true);
                        if (infoPag.getTipPagina() == TipPagina.CIM)
                            listaCereri.getLast().setCim(true);
                        if (infoPag.getTipPagina() == TipPagina.Constatator)
                            listaCereri.getLast().addCc();
                    }
                    paginiCurente.add(i);
                } else
                    System.out.println("pagina goala");
            }

            // Salvăm și ultimul set de pagini
            if (!paginiCurente.isEmpty()) {
                pdfService.salveazaGrupPagini(document, paginiCurente,
                        listaCereri.getLast().getNumar() + "_" + listaCereri.getLast().getData() + "_"
                                + listaCereri.getLast().getFirma() + "_" + paginiCurente.size() + ".pdf");
            }
        }
        return listaCereri;
    }

    public BufferedImage getCodCI() {
        return this.cod_ci;
    }
}