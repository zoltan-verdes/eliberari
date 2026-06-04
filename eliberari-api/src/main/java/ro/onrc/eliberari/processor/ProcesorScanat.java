package ro.onrc.eliberari.processor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import ro.onrc.eliberari.model.Act;
import ro.onrc.eliberari.model.TipAct;
import ro.onrc.eliberari.service.LotRegistry;
import ro.onrc.eliberari.service.PdfService;
import ro.onrc.eliberari.utils.ImageProcessor;

@Component
public class ProcesorScanat {
    private LotRegistry registry;
    private final PdfService pdfService;
    private final ProcesorPagina procPagina;
    private List<Act> listActe;
    private int nrPaginiIgnorate = 0;

    public ProcesorScanat(LotRegistry registry, PdfService pdfService, ProcesorPagina procPagina) {
        this.registry = registry;
        this.pdfService = pdfService;
        this.procPagina = procPagina;

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

}
