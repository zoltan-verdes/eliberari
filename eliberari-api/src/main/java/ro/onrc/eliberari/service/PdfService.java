package ro.onrc.eliberari.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import ro.onrc.eliberari.config.AppConfig;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class PdfService {
     private final AppConfig config;

       public PdfService(AppConfig config) {
        this.config = config;
    }

    public BufferedImage randeazaPagina(PDDocument doc, int paginaIndex) throws IOException {
        PDFRenderer renderer = new PDFRenderer(doc);
        return renderer.renderImageWithDPI(paginaIndex, 300);
    }

    public File salveazaGrupPagini(PDDocument sursa, List<Integer> pagini, String numeFisier) throws IOException {
        File fisier;
        try (PDDocument nou = new PDDocument()) {
            for (int index : pagini) {
                nou.addPage(sursa.getPage(index));
            };
            fisier=new File(config.getOutputFolder() + numeFisier);
            nou.save(fisier);
            return fisier;
        }
    }

    public String extrageText(PDDocument doc, int paginaIndex) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        // PDFTextStripper folosește indexare de la 1 la n
        stripper.setStartPage(paginaIndex + 1);
        stripper.setEndPage(paginaIndex + 1);
        
        String text = stripper.getText(doc);
        return text != null ? text.trim() : "";
    }

}