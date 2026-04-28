package ro.onrc.eliberari.processor;

import ro.onrc.eliberari.LogListener;
import ro.onrc.eliberari.config.AppConfig;
import ro.onrc.eliberari.model.Cerere;
import ro.onrc.eliberari.model.Act;
import ro.onrc.eliberari.model.LotCereri;
import ro.onrc.eliberari.model.InfoPagina;
import ro.onrc.eliberari.model.TipPagina;
import ro.onrc.eliberari.service.DocumentOptimizer;
import ro.onrc.eliberari.service.PdfService;
import ro.onrc.eliberari.service.ZipService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProcesorDocumente {

    private final PdfService pdfService;
    private final ProcesorPagina procPagina;
    private final AppConfig config;
    private final ZipService zipService;

    private LotCereri lotCereri = new LotCereri();
    public List<Act> pagini = new ArrayList<>();

    private BufferedImage cod_ci;
    private DocumentOptimizer docOptimezer;


    // Spring injectează automat serviciile prin constructor
    public ProcesorDocumente(ProcesorPagina procPagina, PdfService pdfService, DocumentOptimizer documentOptimizer, AppConfig config, ZipService zipService) {
        this.procPagina = procPagina;
        this.config = config;
        this.pdfService = pdfService;
        this.docOptimezer = documentOptimizer;
        this.zipService = zipService;
    }


    public LotCereri getLotCereri() {
        return lotCereri;
    }

    /**
     * Metodă care parcurge lista de cereri curentă și trimite actele la listare (imprimare)
     * în ordinea riguroasă: Incheiere, CI, CIM, Constatatoare.
     */
    public void listeazaDocumente(LogListener listener) {
        if (lotCereri.isEmpty()) {
            listener.onLog("Nu există documente încărcate pentru listare.");
            return;
        }

        for (Cerere cerere : lotCereri.getToate()) {
            listener.onLog("--- Trimitere la listare Cererea nr: " + cerere.getNumar() + " ---");
            for (Act act : cerere.getActeOrdonate()) {
                if (act.getFisierLot() != null && act.getFisierLot().exists()) {
                    listener.onLog("Listez: " + act.getTipPagina() + " -> " + act.getDenumire_fisier());
                    // Aici se va adăuga logica de Java Print Service (PrinterJob)
                }
            }
        }
    }

    public void proceseazaDocumentScanat(File fisier, LogListener listener) throws Exception {

        Semaphore ocrLimit = new Semaphore(40);

        File fisier_optimizat = docOptimezer.optimizeazaSiCurata(fisier);
        try (PDDocument document = PDDocument.load(fisier_optimizat)) {
            List<Integer> paginiCurente = new ArrayList<>();

            int nrPagini = document.getNumberOfPages();
            System.out.println("Avem un document cu " + document.getNumberOfPages() + " pagini");

            InfoPagina[] infoPagini = new InfoPagina[nrPagini];
            CountDownLatch latch = new CountDownLatch(nrPagini);

            InfoPagina infoPag;

            for (int i = 0; i < nrPagini; i++) {
                final int index = i;
                Thread.ofVirtual().start(() -> {
                    try {
                        ocrLimit.acquire();
                        var imagine = pdfService.randeazaPagina(document, index);
                        System.out.println("procesam pagina " + index + " dimensiunea (" + imagine.getWidth() + ","
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

            int numarCerereCurent = 0;
            TipPagina tipPaginaCurenta = TipPagina.Altele;
            Cerere cerereCurenta = null;
            List<Act> listCC = null;
            int ccCurent = 0;
            Act actCurent = null;
            
            for (int i = 0; i < nrPagini; i++) {
                infoPag = infoPagini[i];
                if (infoPag.getTipPagina() == TipPagina.PagGoala) continue;
                
                System.out.println("Pagina tip  -- " + infoPag.getTipPagina());
                System.out.println("Barcode  -- " + infoPag.getBarcode());
                    
                // intai salvam paginile anterioare daca avem o pagina noua de act principal
                if ((cerereCurenta != null)&&(infoPag.isActPrincipal())) {
                    System.out.println("Salvam: " + numarCerereCurent + "_" + paginiCurente.size() + ".pdf");
                    File f = pdfService.salveazaGrupPagini(document, paginiCurente, numarCerereCurent + "_"
                            + "_" + paginiCurente.size() + ".pdf");
                    if (actCurent != null) {
                            actCurent.setNrPaginiScanate(paginiCurente.size());
                            actCurent.setFisierScanat(f);
                        }else{
                            cerereCurenta.addAct(new Act(tipPaginaCurenta, paginiCurente.size(), f));
                    }
                    paginiCurente.clear();
                }   

                if (infoPag.isActPrincipal()) {
                    if (!infoPag.getNumar().equals("negasit")) numarCerereCurent = infoPag.getNumarInt();
                    if (lotCereri.getCerere(numarCerereCurent).isEmpty()) {
                        cerereCurenta = new Cerere(infoPag);
                        lotCereri.adauga(cerereCurenta);
                    } else {
                        cerereCurenta = lotCereri.getCerere(numarCerereCurent).orElseThrow();
                    };
                    tipPaginaCurenta = infoPag.getTipPagina();
                    if (tipPaginaCurenta == TipPagina.Constatator) {
                        listCC = cerereCurenta.getConstatatoare();
                        if (ccCurent < listCC.size()) {
                            actCurent = listCC.get(ccCurent);
                            ccCurent++;
                        } else {
                            actCurent = null;
                        };
                    }else{
                        actCurent = cerereCurenta.getAct(tipPaginaCurenta);
                    };
                }
                paginiCurente.add(i);
                
            }

            // Salvăm și ultimul set de pagini
            if (!paginiCurente.isEmpty()) {
                    System.out.println("Salvam: " + numarCerereCurent + "_" + paginiCurente.size() + ".pdf");
                    File f = pdfService.salveazaGrupPagini(document, paginiCurente, numarCerereCurent + "_"
                            + "_" + paginiCurente.size() + ".pdf");
                    if (actCurent != null) {
                            actCurent.setNrPaginiScanate(paginiCurente.size());
                            actCurent.setFisierScanat(f);
                        }else{
                            cerereCurenta.addAct(new Act(tipPaginaCurenta, paginiCurente.size(), f));
                    }
                    paginiCurente.clear();
            }
        }
   
    }

    /**
     * Procesează o listă de fișiere primite (ex. după dezarhivare).
     * Grupează fișierele pe baza numărului găsit în nume și identifică tipul actului.
     */
    public List<Cerere> proceseazaLot(File fisier) throws IOException {


        if (fisier == null) return null;

        List<File> fisiere = zipService.dezarhiveaza(fisier);


        // 1. Sortăm lista după numărul extras din denumirea fișierului
        fisiere.sort((f1, f2) -> {
            long n1 = extrageNumarDinNume(f1.getName());
            long n2 = extrageNumarDinNume(f2.getName());
            return Long.compare(n1, n2);
        });

        this.lotCereri = new LotCereri();
        this.pagini = new ArrayList<>();
        Cerere cerereCurenta = null;

        for (File f : fisiere) {
            String numeFisier = f.getName();
            System.out.println("Procesăm fișierul: " + numeFisier);
            long numarL = extrageNumarDinNume(numeFisier);
            String numarS = String.valueOf(numarL);
            
            // Determinăm tipul actului pe baza numelui
            TipPagina tip = determinaTipActDinNume(numeFisier);

            int paginiInPdf = 0;

            try (PDDocument doc = PDDocument.load(f)) {
                paginiInPdf = doc.getNumberOfPages();
                Act act = new Act(tip, numeFisier, paginiInPdf, f, numarL);
                System.out.println("Adaugam actul: " + tip + " cu numarul: " + numarS + " care are " + paginiInPdf + " pagini.");
                pagini.add(act);
                for(int i=1;i<paginiInPdf;i++){
                    pagini.add(null);
                }
                // Dacă este o cerere nouă, încercăm să extragem datele firmei din textul primului act
                if (cerereCurenta == null || cerereCurenta.getNumar()!=numarL) {
                    cerereCurenta = new Cerere(numarS, "negasit", "negasit", "negasit");
                    cerereCurenta.addAct(act);
                    lotCereri.adauga(cerereCurenta);    
                } else {
                    cerereCurenta.addAct(act);
                }

            } catch (Exception e) {
                System.out.println("Eroare la citirea fișierului: " + numeFisier);
            }


        }

        return lotCereri.getToate();
    }

    private String cautaPattern(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "negasit";
    }

    private long extrageNumarDinNume(String nume) {
        // REGEX: Caută prima secvență de cifre din nume (început, mijloc sau sfârșit)
        // Exemplu: "123_act.pdf" sau "Incheiere_123.pdf" -> 123
        Pattern p = Pattern.compile("\\d{2}_(\\d+)");
        Matcher m = p.matcher(nume);
        if (m.find()) {
            System.out.println("Am găsit numărul în format dd_: " + m.group(1));
            return Long.parseLong(m.group(1));
        }
        else {
            p = Pattern.compile("Punctual-(\\d+)-");
            m = p.matcher(nume);
            if (m.find()) {
                System.out.println("Am găsit numărul în format punctual_: " + m.group(1));
            return Long.parseLong(m.group(1));
            }
        }
        return 0;
    }

    private TipPagina determinaTipActDinNume(String nume) {
        
        // AICI vei adăuga pattern-urile regex finale
        if (nume.contains("INCHEIERE")) return TipPagina.Incheiere;
        if (nume.contains("inregistrareMentiuni")) return TipPagina.CIM;
        if (nume.contains("CertificatConstatator")) return TipPagina.Constatator;
        if (nume.contains("CertificatInmatriculare"))  return TipPagina.CI;
        
        return TipPagina.Altele;
    }

    public BufferedImage getCodCI() {
        return this.cod_ci;
    }
}