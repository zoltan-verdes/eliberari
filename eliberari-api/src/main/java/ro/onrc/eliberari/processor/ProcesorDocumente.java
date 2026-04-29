package ro.onrc.eliberari.processor;

import ro.onrc.eliberari.LogListener;
import ro.onrc.eliberari.model.Cerere;
import ro.onrc.eliberari.model.Act;
import ro.onrc.eliberari.model.LotCereri;
import ro.onrc.eliberari.model.InfoPagina;
import ro.onrc.eliberari.model.TipPagina;
import ro.onrc.eliberari.service.DocumentOptimizer;
import ro.onrc.eliberari.service.PdfPrintService;
import ro.onrc.eliberari.service.PdfService;
import ro.onrc.eliberari.service.ZipService;
import ro.onrc.eliberari.utils.ImageProcessor;

import org.apache.pdfbox.Loader;
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
    private final ZipService zipService;
    private final PdfPrintService printService;

    private LotCereri lotCereri = new LotCereri();
    public List<Act> listPagini = new ArrayList<>();

    private BufferedImage cod_ci;
    private DocumentOptimizer docOptimezer;


    // Spring injectează automat serviciile prin constructor
    public ProcesorDocumente(ProcesorPagina procPagina, PdfService pdfService, DocumentOptimizer documentOptimizer, ZipService zipService, PdfPrintService printService) {
        this.procPagina = procPagina;
        this.pdfService = pdfService;
        this.docOptimezer = documentOptimizer;
        this.zipService = zipService;
        this.printService = printService;
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
                    try (PDDocument doc = Loader.loadPDF(act.getFisierLot())) {
                        printService.printeazaSilent(doc,1);
                    } catch (Exception e) {
                        listener.onLog("Eroare la listarea fișierului " + act.getDenumire_fisier() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    public void recunoastereActeScanate(File fisier, LogListener listener) throws Exception {

        Semaphore ocrLimit = new Semaphore(40);

        File fisier_optimizat = docOptimezer.eliminaGoale(fisier);
        try (PDDocument document = Loader.loadPDF(fisier_optimizat)) {
            List<Integer> paginiCurente = new ArrayList<>();

            int nrPagini = document.getNumberOfPages();
            System.out.println("Avem un document cu " + document.getNumberOfPages() + " nrPagini");
            if (nrPagini != listPagini.size()){
                System.out.println("Numărul de pagini din document nu corespunde cu numărul de pagini procesate anterior! " + nrPagini + " vs " + listPagini.size());
                return;
            }

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
                            Act actNou = new Act(numarCerereCurent, tipPaginaCurenta, f);
                            actNou.setNrPaginiScanate(paginiCurente.size());
                            cerereCurenta.addAct(actNou);
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
                            Act actNou = new Act(numarCerereCurent, tipPaginaCurenta, f);
                            actNou.setNrPaginiScanate(paginiCurente.size());
                            cerereCurenta.addAct(actNou);
                    }
                    paginiCurente.clear();
            }
        }
   
    }


public List<String> proceseazaDocumentScanat(File fisier) throws Exception {

        List<String> log = new ArrayList<>();
        File fisier_optimizat = docOptimezer.eliminaGoale(fisier);
        try (PDDocument document = Loader.loadPDF(fisier_optimizat)) {
            List<Integer> paginiCurente = new ArrayList<>();

            int nrPagini = document.getNumberOfPages();
            System.out.println("Avem un document cu " + document.getNumberOfPages() + " nrPagini");
            if (nrPagini != listPagini.size()){
                log.add("Numărul de pagini din document nu corespunde cu numărul de pagini procesate anterior! " + nrPagini + " vs " + listPagini.size());
                System.out.println("Numărul de pagini din document nu corespunde cu numărul de pagini procesate anterior! " + nrPagini + " vs " + listPagini.size());
                return log;
            }

            for (int i = 0; i < nrPagini; i++) {
               // verificam ca pagina scanata coresponde cu pagina din lista de pagini procesate anterior
                if (listPagini.get(i)!=null) 
                try {
                    var imagine = pdfService.randeazaPagina(document, i, 150); 
                    System.out.println("procesam pagina " + i + " dimensiunea (" + imagine.getWidth() + ","+ imagine.getHeight() + ")");
                    

                    if (listPagini.get(i).getTipPagina() == TipPagina.CI) {;
                        // trebuie sa rotim pagina cu 90 grade pentru a citi corect codul CI
                        imagine = ImageProcessor.rotate90(imagine);
                    };
                    
                    if (!procPagina.isMarkerPresent(imagine, listPagini.get(i).getTipPagina())) {
                        log.add("Pagina " + i + " nu este "+listPagini.get(i).getTipPagina());
                        System.out.println("Pagina " + i + " nu este "+listPagini.get(i).getTipPagina());
                        return log;
                    }
                    listPagini.get(i).setNrPaginiScanate(listPagini.get(i).getNrPaginiLot());
                    String denumireFisier = listPagini.get(i).getDenumire_fisier();
                    paginiCurente.add(i);
                    for (int j=1;j<listPagini.get(i).getNrPaginiLot();j++)
                        paginiCurente.add(++i);

                    System.out.println("Salvam: " + denumireFisier);
                    File f = pdfService.salveazaGrupPagini(document, paginiCurente, denumireFisier);
                    paginiCurente.clear();

                    } catch (IOException e) {
                        // Aici gestionezi eroarea de PDF (logare sau marcare pagină ca eșuată)
                        System.err.println("Eroare la pagina " + i + ": " + e.getMessage());
                        log.add("Eroare la pagina " + i + ": ");
                    }
                };
            }

        return log;
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
            long n1 = extrageNumarDinNume(f1.getName())*10+determinaTipActDinNume(f1.getName()).getPrioritate();
            long n2 = extrageNumarDinNume(f2.getName())*10+determinaTipActDinNume(f2.getName()).getPrioritate();
            return Long.compare(n1, n2);
        });

        this.lotCereri = new LotCereri();
        this.listPagini = new ArrayList<>();
        Cerere cerereCurenta = null;
        boolean file_lista_verificare = false;

        for (File f : fisiere) {
            String numeFisier = f.getName();
            System.out.println("Procesăm fișierul: " + numeFisier+" numar pagini totale "+listPagini.size());
            long numarL = extrageNumarDinNume(numeFisier);
            String numarS = String.valueOf(numarL);
            
            // Determinăm tipul actului pe baza numelui
            TipPagina tip = determinaTipActDinNume(numeFisier);
            int paginiInPdf = 0;
            try (PDDocument doc = Loader.loadPDF(f)) {
                paginiInPdf = doc.getNumberOfPages();
                System.out.print("tiparim " + numeFisier+" " );
//                if (tip != TipPagina.CI)
//                    if (tip == TipPagina.Constatator) printService.printeazaCuFoxit(f,1);
//                else printService.printeazaCuFoxit(f,1);
                System.out.println("trecut cu succes" );

                if (tip == TipPagina.ListaVerificare) {
                    file_lista_verificare = true;
                    continue; // Nu adăugăm lista de verificare ca act, ci o asociem cererii
                }
                Act act = new Act(numarL, tip, numeFisier, paginiInPdf, f);
                
                listPagini.add(act);
                for(int i=1;i<paginiInPdf;i++){
                    listPagini.add(null);
                }
                // Dacă este o cerere nouă, încercăm să extragem datele firmei din textul primului act
                if (cerereCurenta == null || cerereCurenta.getNumar()!=numarL) {
                    if ((cerereCurenta!=null)&&(!file_lista_verificare)){
                        System.out.println("Cererea " + cerereCurenta.getNumar() + " nu are listă de verificare asociată!");
                    }
                    cerereCurenta = new Cerere(numarS, "negasit", "negasit", "negasit");
                }
                cerereCurenta.addAct(act);
                lotCereri.adauga(cerereCurenta);    
            } catch (Exception e) {
                System.out.println("Eroare la citirea fișierului: " + numeFisier);
            }


        }

        return lotCereri.getToate();
    }


    private long extrageNumarDinNume(String nume) {
        // REGEX: Caută prima secvență de cifre din nume (început, mijloc sau sfârșit)
        // Exemplu: "123_act.pdf" sau "Incheiere_123.pdf" -> 123
        Pattern p = Pattern.compile("\\d{2}_(\\d+)");
        Matcher m = p.matcher(nume);
        if (m.find()) {
//            System.out.println("Am găsit numărul în format dd_: " + m.group(1));
            return Long.parseLong(m.group(1));
        }
        else {
            p = Pattern.compile("Punctual-(\\d+)-");
            m = p.matcher(nume);
            if (m.find()) {
//                System.out.println("Am găsit numărul în format punctual_: " + m.group(1));
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
        if (nume.contains("FI-Punctual-")) return TipPagina.ListaVerificare;
        
        
        return TipPagina.Altele;
    }

    public BufferedImage getCodCI() {
        return this.cod_ci;
    }





}

class FisiereActe{
    private int numarCerere;
    private TipPagina tipPagina;
    private String denumireFisier;

    public FisiereActe(int numarCerere, TipPagina tipPagina, String denumireFisier) {
        this.numarCerere = numarCerere;
        this.tipPagina = tipPagina;
        this.denumireFisier = denumireFisier;
    }
    public int getNumarCerere() {
        return numarCerere;
    }
    public TipPagina getTipPagina() {
        return tipPagina;
    }
    public String getDenumireFisier() {
        return denumireFisier;
    }

}