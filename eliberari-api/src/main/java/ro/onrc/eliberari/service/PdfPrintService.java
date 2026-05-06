package ro.onrc.eliberari.service;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;
import org.springframework.stereotype.Service;

import ro.onrc.eliberari.config.AppConstants;

import org.springframework.beans.factory.InitializingBean;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Service
public class PdfPrintService implements InitializingBean {

    private PrintService cachedPrinter;

    @Override
    public void afterPropertiesSet() {
        this.cachedPrinter = Arrays.stream(PrintServiceLookup.lookupPrintServices(null, null))
                .filter(service -> service.getName().equalsIgnoreCase(AppConstants.TARGET_PRINTER))
                .findFirst()
                .orElse(null);

        if (cachedPrinter == null) {
            System.err.println("Atenție: Imprimanta '%s' nu a fost găsită.".formatted(AppConstants.TARGET_PRINTER));
        }
    }


public void printeazaFoxIt(File pdfFile, int numarCopii) throws IOException, InterruptedException {
        if (!pdfFile.exists()) {
            throw new IOException("Fișierul PDF nu a fost găsit la calea: " + pdfFile.getAbsolutePath());
        }
        String foxitPath = "C:\\Program Files (x86)\\Foxit Software\\Foxit PDF Reader\\FoxitPDFReader.exe";
        // Executăm comanda pentru fiecare exemplar în parte
        for (int i = 0; i < numarCopii; i++) {
            // Folosim ProcessBuilder (recomandat Java 18+) pentru a evita Runtime.exec
            ProcessBuilder pb = new ProcessBuilder(
                foxitPath, 
                "/t",                   // Parametrul pentru Silent Print
                pdfFile.getAbsolutePath(), 
                AppConstants.TARGET_PRINTER
            );
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            // Așteptăm ca Foxit să predea job-ul către Windows Print Spooler
            boolean finished = process.waitFor(20, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroy(); // Închidem procesul dacă s-a blocat
            }

            // O mică pauză de 500ms între copii pentru a preveni suprapunerea în spooler
            if (numarCopii > 1) {
                Thread.sleep(1000);
            }
        }
    }


public void printeazaCuFoxit(File pdfFile, int numarCopii) throws IOException, InterruptedException {
    String foxitPath = "C:\\Program Files (x86)\\Foxit Software\\Foxit PDF Reader\\FoxitPDFReader.exe";
    
    // Transmitem calea absoluta direct executabilului
    String command = String.format("\"%s\" /t \"%s\" \"%s\"", 
                        foxitPath, 
                        pdfFile.getAbsolutePath(), 
                        AppConstants.TARGET_PRINTER);
    
    for (int i = 0; i < numarCopii; i++) {
        Runtime.getRuntime().exec(command).waitFor(); // Așteptăm 1 secundă între comenzi pentru a evita suprasolicitarea
        Thread.sleep(2000);
    }
}    

public void printeazaSilent(PDDocument document, int numarCopii) throws PrinterException {
    PrintService printer = Optional.ofNullable(cachedPrinter)
            .orElseThrow(() -> new PrinterException("Imprimanta negăsită"));

    var job = PrinterJob.getPrinterJob();
    job.setPrintService(printer);

    // Setările de margini (A4, 3mm) pe care le-am stabilit că funcționează
    Paper paper = new Paper();
    paper.setSize(595.27, 841.89);
    paper.setImageableArea(8.5, 8.5, 578.27, 824.89);
    PageFormat pf = job.defaultPage();
    pf.setPaper(paper);

    PDFPrintable printable = new PDFPrintable(document, Scaling.SHRINK_TO_FIT);
    
    job.setPrintable(printable, pf);
    job.setCopies(numarCopii);

    job.print();
}
    

    public boolean printeazaCuDialog(PDDocument document) throws PrinterException {
        var job = PrinterJob.getPrinterJob();
        job.setPageable(new PDFPageable(document));
        
        return job.printDialog() && executePrint(job);
    }

    private boolean executePrint(PrinterJob job) {
        try {
            job.print();
            return true;
        } catch (PrinterException e) {
            return false;
        }
    }
}