package ro.onrc.eliberari.processor;

import ro.onrc.eliberari.model.Act;
import ro.onrc.eliberari.model.LotCereri;
import ro.onrc.eliberari.model.TipAct;
import ro.onrc.eliberari.service.AppRepository;
import ro.onrc.eliberari.service.LotRegistry;
import ro.onrc.eliberari.service.PdfPrintService;
import ro.onrc.eliberari.service.PdfService;
import ro.onrc.eliberari.utils.ImageProcessor;


import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;





@Component
public class ProcesorLotDirector {

    private final PdfPrintService pdfPrintService;
    private final PdfService pdfService;
    private final ProcesorPagina procPagina;
    private final AppRepository repository;
    private final LotRegistry registry;

    private LotCereri lotCereri = new LotCereri();
    public List<Act> listActe = new ArrayList<>();
//    public String lotCurent = "";
    private int nrPaginiIgnorate = 0;
    private BufferedImage cod_ci;


    // Spring injectează automat serviciile prin constructor
    public ProcesorLotDirector(ProcesorPagina procPagina, PdfService pdfService, AppRepository repository, PdfPrintService pdfPrintService, LotRegistry registry) {
        this.procPagina = procPagina;
        this.pdfService = pdfService;
        this.repository = repository;
        this.pdfPrintService = pdfPrintService;
        this.registry = registry;
    }


    public LotCereri getLotCereri() {
        return lotCereri;
    }

    public AppRepository getRepository() {
        return repository;
    }



    /**
     * Procesează lotul zip salveaza in registru si obtine lista de fisiere 
     * pentru fiecare fisier extrage numarul, data, tipul actului si numarul de pagini,
     * si-l salveaza in regitru in forma de lista de acte (clasa Act)
     */
    public String proceseazaLot(MultipartFile lotZip) throws IOException {
        if (lotZip == null) return null;
//        String lotId = lotZip.getName().replace(".zip", "");
        String idLot = repository.salveazaLotDirectorZip(lotZip);
        List<File> fisiere = repository.getFisiereLotDirector(idLot);

        this.listActe = new ArrayList<>();
//        this.lotCurent = idLot;
        String data="";

        for (File f : fisiere) {
            String numeFisier = f.getName();
            System.out.print("Fisierul: " + numeFisier+" - ");
            long numarL = extrageNumarDinNume(numeFisier);
            if (!extrageDataDinNume(numeFisier).isEmpty()) data = extrageDataDinNume(numeFisier);
            TipAct tip = determinaTipActDinNume(numeFisier);
            int paginiInPdf = 1;
            if ((tip == TipAct.Incheiere || tip == TipAct.Constatator ))
            try (PDDocument doc = Loader.loadPDF(f)) {
                paginiInPdf = doc.getNumberOfPages();
                System.out.println(doc.getNumberOfPages()+" pagini. Totale:"+listActe.size());
            } catch (IOException e) {
                System.out.println("Eroare la citirea fișierului: " + numeFisier);
            }
                Act act = new Act(numarL, data, tip, numeFisier, paginiInPdf);
                listActe.add(act); 
        }
        System.out.println("punem in lotRegistry: " + idLot+" cu "+listActe.size()+" acte");
        registry.registerNewLot(idLot, listActe);
        return idLot;
    }


    public void tiparesteLot(String idLot) throws Exception {
        List<Act> acte = registry.getLotForProcessing(idLot);
        if (acte == null || acte.isEmpty()) {
            System.out.println("Nu există acte pentru lotul: " + idLot);
            return;
        }
        System.out.println("Începem tipărirea pentru lotul: " + idLot);
        int nrExemplare = 1;
        for (Act act : acte) {
            nrExemplare = 1;
            File fisierAct = repository.getFisierAct(idLot, act.getDenumireFisier());
            System.out.println("Tipărim actul: " + act.getDenumireFisier());
            if (act.getTipAct() == TipAct.Constatator) nrExemplare = 2 ;
            if (act.getTipAct() == TipAct.Incheiere) continue;
            pdfPrintService.printeaza(fisierAct, nrExemplare);
        }
    }


    public boolean[] verificamIgnorate(PDDocument doc) throws IOException {
        boolean[] paginiIgnorate = new boolean[doc.getNumberOfPages()];
        nrPaginiIgnorate = 0;
        for (int i = 0; i < doc.getNumberOfPages(); i++) {
            BufferedImage imgBruta = pdfService.randeazaPagina(doc, i);
            paginiIgnorate[i] = ImageProcessor.estePaginaAlba(imgBruta);
            if (paginiIgnorate[i]) nrPaginiIgnorate++;
        }
        return paginiIgnorate;
    }

    public boolean[] proceseazaDocumentScanat(File fisier) throws Exception {
        listActe = registry.getLotForProcessing(fisier.getName().replace(".pdf", ""));
        boolean paginiIgnorate[] = new boolean[listActe.size()];

        try (PDDocument document = Loader.loadPDF(fisier)) {
            paginiIgnorate = verificamIgnorate(document);
        } catch (IOException e) {
            System.err.println("Eroare la citirea fișierului: " + e.getMessage());
            
            return paginiIgnorate;
        }
        return paginiIgnorate;
    };

    public void verificaConntinutScanat(String numeLot) throws Exception {
        listActe = registry.getLotForProcessing(numeLot);
        int nrPaginiActe = registry.getInfoLot(numeLot).getNrPagini();
        boolean paginiIgnorate[] = registry.getPageStatuses(numeLot);
        nrPaginiIgnorate = 0;
        for (int i = 0; i < paginiIgnorate.length; i++) {
            if (paginiIgnorate[i]) nrPaginiIgnorate++;
        }        
        File fisier = registry.getFisierScanat(numeLot);

        int index = 0;
        int i=0;
        try (PDDocument document = Loader.loadPDF(fisier)) {


                    var imagine = pdfService.randeazaPagina(document, i, 150); 
                    int nrPaginiAct = listActe.get(index).getNrPagini();

                    if (listActe.get(index).getTipAct() == TipAct.CI) {;
                        // trebuie sa rotim pagina cu 90 grade pentru a citi corect codul CI
                        imagine = ImageProcessor.rotate90(imagine);
                    };
                    
                    if (!procPagina.isMarkerPresent(imagine, listActe.get(index).getTipAct())) {
                        System.out.println("Pagina " + i + " nu este "+listActe.get(index).getTipAct());
                    }
                } catch (IOException e) {
                    // Aici gestionezi eroarea de PDF (logare sau marcare pagină ca eșuată)
                    System.err.println("Eroare la pagina " + i + ": " + e.getMessage());
                }


    }


    public String desparteFisierScanat(String numeLot, boolean[] paginiIgnorate) throws Exception {

        listActe = registry.getLotForProcessing(numeLot);
        int nrPaginiActe = registry.getInfoLot(numeLot).getNrPagini();
//        boolean paginiIgnorate[] = registry.getPageStatuses(numeLot);
        nrPaginiIgnorate = 0;
        for (int i = 0; i < paginiIgnorate.length; i++) {
            if (paginiIgnorate[i]) nrPaginiIgnorate++;
        }        
        File fisier = registry.getFisierScanat(numeLot);

        try (PDDocument document = Loader.loadPDF(fisier)) {

            List<Integer> paginiCurente = new ArrayList<>();
            File outputDir = new File(fisier.getAbsolutePath().replace(".pdf","\\"));
            System.out.println("Incercam sa creem folderul "+ outputDir.getAbsolutePath());
            if (!outputDir.exists())  {outputDir.mkdirs();}
            else try (var files = Files.list(outputDir.toPath())) {
                files.filter(Files::isRegularFile).forEach(p -> p.toFile().delete());
            }            

            int nrPagini = document.getNumberOfPages();
            System.out.println("Avem un document cu " + document.getNumberOfPages() + " nrPagini");
            if ((nrPagini-nrPaginiIgnorate) != nrPaginiActe) {
                System.out.println("Numărul de pagini din document nu corespunde cu numărul de pagini procesate anterior! " + (nrPagini-nrPaginiIgnorate) + " vs " + nrPaginiActe);
                return "Numărul de pagini din document nu corespunde cu numărul de pagini procesate anterior! " + (nrPagini-nrPaginiIgnorate) + " vs " + nrPaginiActe;
            }
            int index = 0;
            for (Act act : listActe) {
//                System.out.println("Actul " + act.getDenumireFisier() + " are " + act.getNrPagini() + " pagini.");
                TipAct tip = act.getTipAct();
                if (tip != TipAct.CI && tip != TipAct.CIM && tip != TipAct.Constatator) continue;
                String denumireFisier = act.getDenumireFisier();
                paginiCurente.clear();

                for (int i=0;i<act.getNrPagini();i++) {
                    while (paginiIgnorate[index]) {
                        System.out.println("Ignoram pagina " + (index++) + " pentru ca este alba");
                    }
                
                paginiCurente.add(index++);
                }
                System.out.println("Salvam: " + denumireFisier+" cu paginile: " + paginiCurente);
                File f = pdfService.salveazaGrupPagini(document, paginiCurente, outputDir.getAbsolutePath(), denumireFisier);
            }
            return "Separare terminat cu succes";
         } catch (IOException e) {
            System.err.println("Eroare la citirea fișierului: " + e.getMessage());
            return "Eroare la citirea fișierului: " + e.getMessage();
        }
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
    private String extrageDataDinNume(String nume) {
        // REGEX: Caută prima secvență de cifre din nume (început, mijloc sau sfârșit)
        // Exemplu: "123_act.pdf" sau "Incheiere_123.pdf" -> 123
        Pattern p = Pattern.compile("\\d{2}_\\d+_(\\d{2}.\\d{2}.\\d{4})");
        Matcher m = p.matcher(nume);
        if (m.find()) {
//            System.out.println("Am găsit data : " + m.group(1));
            return m.group(1);
        }
        else return "";
    }


    private TipAct determinaTipActDinNume(String nume) {
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
