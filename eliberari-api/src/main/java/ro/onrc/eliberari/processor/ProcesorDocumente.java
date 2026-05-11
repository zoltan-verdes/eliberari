package ro.onrc.eliberari.processor;

import ro.onrc.eliberari.LogListener;
import ro.onrc.eliberari.model.Cerere;
import ro.onrc.eliberari.model.Act;
import ro.onrc.eliberari.model.LotCereri;
import ro.onrc.eliberari.model.ScanatDTO;
import ro.onrc.eliberari.model.InfoPagina;
import ro.onrc.eliberari.model.TipAct;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;





@Component
public class ProcesorDocumente {

    private final PdfPrintService pdfPrintService;
    private final PdfService pdfService;
    private final ProcesorPagina procPagina;
    private final ZipService zipService;
    private final ActRepository actRepository;

    private LotCereri lotCereri = new LotCereri();
    public List<Act> listPagini = new ArrayList<>();

    private BufferedImage cod_ci;
    private DocumentOptimizer docOptimezer;
    private int nrPaginiIgnorate = 0;


    // Spring injectează automat serviciile prin constructor
    public ProcesorDocumente(ProcesorPagina procPagina, PdfService pdfService, DocumentOptimizer documentOptimizer, ZipService zipService, ActRepository actRepository, PdfPrintService pdfPrintService) {
        this.procPagina = procPagina;
        this.pdfService = pdfService;
        this.docOptimezer = documentOptimizer;
        this.zipService = zipService;
        this.actRepository = actRepository;
        this.pdfPrintService = pdfPrintService;
    }


    public LotCereri getLotCereri() {
        return lotCereri;
    }

    public ActRepository getActRepository() {
        return actRepository;
    }

    /**
     * Metodă care parcurge lista de cereri curentă și trimite actele la listare (imprimare)
     * în ordinea riguroasă: Incheiere, CI, CIM, Constatatoare.
     */

public boolean[] verificamIgnorate(PDDocument doc) throws IOException {
    boolean[] paginiIgnorate = new boolean[doc.getNumberOfPages()];
    for (int i = 0; i < doc.getNumberOfPages(); i++) {
        BufferedImage imgBruta = pdfService.randeazaPagina(doc, i);
        paginiIgnorate[i] = ImageProcessor.estePaginaAlba(imgBruta);
        if (paginiIgnorate[i]) nrPaginiIgnorate++;
    }
    return paginiIgnorate;
}


public ScanatDTO proceseazaDocumentScanat(File fisier) throws Exception {

        List<String> log = new ArrayList<>();
//        File fisier_optimizat = docOptimezer.eliminaGoale(fisier);
        try (PDDocument document = Loader.loadPDF(fisier)) {
            boolean[] paginiIgnorate = verificamIgnorate(document);

            List<Integer> paginiCurente = new ArrayList<>();

            int nrPagini = document.getNumberOfPages()-nrPaginiIgnorate;
            System.out.println("Avem un document cu " + document.getNumberOfPages() + " nrPagini");
            if (nrPagini != listPagini.size()){
                log.add("Numărul de pagini din document nu corespunde cu numărul de pagini procesate anterior! " + nrPagini + " vs " + listPagini.size());
                System.out.println("Numărul de pagini din document nu corespunde cu numărul de pagini procesate anterior! " + nrPagini + " vs " + listPagini.size());
                return log;
            }
            int index = 0;
            for (int i = 0; i < nrPagini; i++) {
               // verificam ca pagina scanata coresponde cu pagina din lista de pagini procesate anterior
                if (paginiIgnorate[i]) continue;
                if (listPagini.get(index)!=null)
                try {
                    var imagine = pdfService.randeazaPagina(document, i, 150); 
                    System.out.println("procesam pagina " + i + " dimensiunea (" + imagine.getWidth() + ","+ imagine.getHeight() + ")  -  doc asteptat "+listPagini.get(i).getDenumire_fisier());
                    int nrPaginiAct = listPagini.get(index).getNrPagini();

                    if (listPagini.get(index).getTipAct() == TipAct.CI) {;
                        // trebuie sa rotim pagina cu 90 grade pentru a citi corect codul CI
                        imagine = ImageProcessor.rotate90(imagine);
                    };
                    
                    if (!procPagina.isMarkerPresent(imagine, listPagini.get(index).getTipAct())) {
                        log.add("Pagina " + i + " nu este "+listPagini.get(index).getTipAct());
                        System.out.println("Pagina " + i + " nu este "+listPagini.get(index).getTipAct());
//                        return log;
                    }
                    
                    String denumireFisier = listPagini.get(index).getDenumire_fisier();
                    paginiCurente.add(i);
                    for (int j=1;j<nrPaginiAct;j++)
                        paginiCurente.add(++i);

                    System.out.println("Salvam: " + denumireFisier);
                    pdfService.salveazaGrupPagini(document, paginiCurente, denumireFisier);
                    paginiCurente.clear();

                    } catch (IOException e) {
                        // Aici gestionezi eroarea de PDF (logare sau marcare pagină ca eșuată)
                        System.err.println("Eroare la pagina " + i + ": " + e.getMessage());
                        log.add("Eroare la pagina " + i + ": ");
                    }
                    index++;
                };
            }

        return new ScanatDTO(null, log);
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
            System.out.print("Fisierul: " + numeFisier+" - ");
            long numarL = extrageNumarDinNume(numeFisier);
            String numarS = String.valueOf(numarL);
            
            // Determinăm tipul actului pe baza numelui
            TipAct tip = determinaTipActDinNume(numeFisier);
            int paginiInPdf = 0;
            try (PDDocument doc = Loader.loadPDF(f)) {
                paginiInPdf = doc.getNumberOfPages();
                System.out.println(doc.getNumberOfPages()+" pagini. Totale:"+listPagini.size());
                if (tip == TipAct.CIM || tip == TipAct.CI ) paginiInPdf = 1; 
                System.out.print("tiparim " + numeFisier+" " );

//                if (tip != TipAct.CI)
//                    if (tip == TipAct.Constatator) pdfPrintService.printeazaCuFoxit(f,2);
//                else pdfPrintService.printeazaCuFoxit(f,1);
                System.out.println("trecut cu succes" );

                if (tip == TipAct.ListaVerificare) {
                    file_lista_verificare = true;
                    continue; // Nu adăugăm lista de verificare ca act, ci o asociem cererii
                }
                Act act = new Act(numarL, tip, numeFisier, paginiInPdf);
                
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
                 cerereCurenta.addAct(tip, paginiInPdf);
                lotCereri.adauga(cerereCurenta);    
            } catch (Exception e) {
                System.out.println("Eroare la citirea fișierului: " + numeFisier);
            }


        }
        System.out.println("punem in actRepository: " + fisier.getName());
        actRepository.salveazaListaNoua(listPagini, fisier.getName().replace(".zip", ""));

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

    private TipAct determinaTipActDinNume(String nume) {
        
        // AICI vei adăuga pattern-urile regex finale
        if (nume.contains("INCHEIERE")) return TipAct.Incheiere;
        if (nume.contains("inregistrareMentiuni")) return TipAct.CIM;
        if (nume.contains("CertificatConstatator")) return TipAct.Constatator;
        if (nume.contains("CertificatInmatriculare"))  return TipAct.CI;
        if (nume.contains("FI-Punctual-")) return TipAct.ListaVerificare;
        
        
        return TipAct.Altele;
    }

    public BufferedImage getCodCI() {
        return this.cod_ci;
    }


}

class FisiereActe{
    private int numarCerere;
    private TipAct tipPagina;
    private String denumireFisier;

    public FisiereActe(int numarCerere, TipAct tipPagina, String denumireFisier) {
        this.numarCerere = numarCerere;
        this.tipPagina = tipPagina;
        this.denumireFisier = denumireFisier;
    }
    public int getNumarCerere() {
        return numarCerere;
    }
    public TipAct getTipPagina() {
        return tipPagina;
    }
    public String getDenumireFisier() {
        return denumireFisier;
    }

}