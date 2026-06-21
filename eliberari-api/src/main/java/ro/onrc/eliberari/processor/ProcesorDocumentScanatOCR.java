package ro.onrc.eliberari.processor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import ro.onrc.eliberari.LogListener;
import ro.onrc.eliberari.config.AppConfig;
import ro.onrc.eliberari.model.Act;
import ro.onrc.eliberari.model.Cerere;
import ro.onrc.eliberari.model.InfoPagina;
import ro.onrc.eliberari.model.TipAct;
import ro.onrc.eliberari.service.DocumentOptimizer;
import ro.onrc.eliberari.service.LotRegistry;
import ro.onrc.eliberari.service.PdfService;
import ro.onrc.eliberari.utils.ImageProcessor;





@Component
public class ProcesorDocumentScanatOCR {
    private final PdfService pdfService;
    private final ProcesorPagina procPagina;
    private List<Act> listActe;

    private final AppConfig config;
    private BufferedImage cod_ci;
    private DocumentOptimizer docOptimezer;    

    public ProcesorDocumentScanatOCR(ProcesorPagina procPagina, PdfService pdfService, AppConfig config, DocumentOptimizer documentOptimizer) {
        this.procPagina = procPagina;
        this.pdfService = pdfService;
        this.config = config;
        this.docOptimezer = documentOptimizer;

    }



    public List<Cerere> proceseaza(File fisier, LogListener listener) throws Exception {

        Semaphore ocrLimit = new Semaphore(40);
        List<Cerere> listaCereri = new ArrayList<>();
        File fisier_optimizat = docOptimezer.optimizeazaSiCurata(fisier);
        try (PDDocument document = Loader.loadPDF(fisier)) {
            List<Integer> paginiCurente = new ArrayList<>();

            int nrPagini = document.getNumberOfPages();
            listener.onLog("Avem un document cu " + document.getNumberOfPages() + " pagini");

            InfoPagina[] infoPagini = new InfoPagina[nrPagini];
            CountDownLatch latch = new CountDownLatch(nrPagini);

            InfoPagina infoPag;

            for (int i = 0; i < nrPagini; i++) {
                final int index = i;
                Thread.ofVirtual().start(() -> {
                    try {
                        ocrLimit.acquire();
                        var imagine = pdfService.randeazaPagina(document, index);
                        listener.onLog("procesam pagina " + index + " dimensiunea (" + imagine.getWidth() + ","
                                + imagine.getHeight() + ")");

                        infoPagini[index] = procPagina.prelucrarePagina(imagine);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Bună practică pentru thread-uri

                    } catch (IOException e) {
                        // Aici gestionezi eroarea de PDF (logare sau marcare pagină ca eșuată)
                        System.err.println("Eroare la pagina " + index + ": " + e.getMessage());
                    } finally {
                        ocrLimit.release();
                        latch.countDown();
                    }
                });
            }
            latch.await();

            for (int i = 0; i < nrPagini; i++) {

                infoPag = infoPagini[i];

                if (infoPag.getTipPagina() != TipAct.PagGoala) {
                    listener.onLog("Pagina tip  -- " + infoPag.getTipPagina());
                    listener.onLog("Barcode  -- " + infoPag.getBarcode());

                    if (infoPag.getTipPagina() == TipAct.Incheiere) {
                        if (!paginiCurente.isEmpty() && !listaCereri.isEmpty()) {
                            Cerere cerere = listaCereri.getLast();
                            listener.onLog("Salvam: " + cerere.getNumar() + "_" + cerere.getData() + "_"
                                    + cerere.getFirma() + "_" + paginiCurente.size() + ".pdf");
                            pdfService.salveazaGrupPagini(document, paginiCurente, cerere.getNumar() + "_"
                                    + cerere.getData() + "_" + cerere.getFirma() + "_" + paginiCurente.size() + ".pdf");
                            paginiCurente.clear();
                        }

                        listaCereri.add(new Cerere(infoPag));
                    }

                    if (infoPag.getTipPagina() != TipAct.Altele) {
                        listener.onLog("pagina de " + infoPag.getTipPagina());
                        listener.onLog("info: nr." + infoPag.getNumar() + "/" + infoPag.getData() + "  Firma:"
                                + infoPag.getFirma() + "  CUI:" + infoPag.getCui());
                        if (infoPag.getTipPagina() == TipAct.Incheiere)
                            listaCereri.getLast().setInch(true);
                        if (infoPag.getTipPagina() == TipAct.CI)
                            listaCereri.getLast().setCI(true);
                        if (infoPag.getTipPagina() == TipAct.CIM)
                            listaCereri.getLast().setCIM(true);
                        if (infoPag.getTipPagina() == TipAct.Constatator)
                            listaCereri.getLast().addCC();
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
