package ro.onrc.eliberari.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.InitializingBean;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Arrays;
import java.util.Optional;

@Service
public class PdfPrintService implements InitializingBean {

    private static final String TARGET_PRINTER = "imprimantaListari";
    private PrintService cachedPrinter;

    @Override
    public void afterPropertiesSet() {
        this.cachedPrinter = Arrays.stream(PrintServiceLookup.lookupPrintServices(null, null))
                .filter(service -> service.getName().equalsIgnoreCase(TARGET_PRINTER))
                .findFirst()
                .orElse(null);

        if (cachedPrinter == null) {
            System.err.println("Atenție: Imprimanta '%s' nu a fost găsită.".formatted(TARGET_PRINTER));
        }
    }

    public void printeazaSilent(PDDocument document, int numarCopii) throws PrinterException {
        PrintService printer = Optional.ofNullable(cachedPrinter)
                .orElseThrow(() -> new PrinterException("Imprimanta '%s' nu este disponibilă.".formatted(TARGET_PRINTER)));

        var job = PrinterJob.getPrinterJob();
        job.setPrintService(printer);
        job.setPageable(new PDFPageable(document));

        var attributes = new HashPrintRequestAttributeSet();
        attributes.add(new Copies(numarCopii));

        job.print(attributes);
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