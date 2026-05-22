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
    private List<Act> listPagini;
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
        listPagini = registry.getLotForProcessing(fisier.getName().replace(".pdf", ""));
        boolean paginiIgnorate[] = new boolean[listPagini.size()];

        try (PDDocument document = Loader.loadPDF(fisier)) {
            paginiIgnorate = verificamIgnorate(document);
        } catch (IOException e) {
            System.err.println("Eroare la citirea fișierului: " + e.getMessage());
            
            return paginiIgnorate;
        }
        return paginiIgnorate;
    };
        


    public String desparteFisierScanat(String numeLot) throws Exception {

        List<String> log = new ArrayList<>();
        listPagini = registry.getLotForProcessing(numeLot);
        boolean paginiIgnorate[] = registry.getPageStatuses(numeLot);
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
            if ((nrPagini-nrPaginiIgnorate) != listPagini.size()){
                log.add("Numărul de pagini din document nu corespunde cu numărul de pagini procesate anterior! " + (nrPagini-nrPaginiIgnorate) + " vs " + listPagini.size());
                System.out.println("Numărul de pagini din document nu corespunde cu numărul de pagini procesate anterior! " + (nrPagini-nrPaginiIgnorate) + " vs " + listPagini.size());
                return "Numărul de pagini din document nu corespunde cu numărul de pagini procesate anterior! " + (nrPagini-nrPaginiIgnorate) + " vs " + listPagini.size();
            }
            int index = 0;
            for (int i = 0; i < nrPagini; i++) {
               // verificam ca pagina scanata coresponde cu pagina din lista de pagini procesate anterior
                if (paginiIgnorate[i]) continue;
                if (listPagini.get(index)!=null)
                try {
                    var imagine = pdfService.randeazaPagina(document, i, 150); 
                    System.out.println("procesam pagina " + i + " dimensiunea (" + imagine.getWidth() + ","+ imagine.getHeight() + ")  -  doc asteptat "+listPagini.get(index).getDenumire_fisier());
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
                        {paginiCurente.add(++i);index++;}

                    System.out.println("Salvam: " + denumireFisier);
                    File f = pdfService.salveazaGrupPagini(document, paginiCurente, outputDir.getAbsolutePath(), denumireFisier);
                    registry.addFisierRezultat(numeLot, f);
                    paginiCurente.clear();

                    } catch (IOException e) {
                        // Aici gestionezi eroarea de PDF (logare sau marcare pagină ca eșuată)
                        System.err.println("Eroare la pagina " + i + ": " + e.getMessage());
                        log.add("Eroare la pagina " + i + ": ");
                    }
                    index++;
                };
            }

//        return log;//new ScanatDTO(null, log);
          return "Separare terminat cu succes";
        
    }

}
