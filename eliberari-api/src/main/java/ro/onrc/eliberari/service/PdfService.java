package ro.onrc.eliberari.service;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
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
//        CustomRenderer renderer = new CustomRenderer(doc);
        PDFRenderer renderer = new PDFRenderer(doc);
        return renderer.renderImageWithDPI(paginaIndex, 300);
    }

    public BufferedImage randeazaPagina(PDDocument doc, int paginaIndex, int DPI) throws IOException {
//        CustomRenderer renderer = new CustomRenderer(doc);
        PDFRenderer renderer = new PDFRenderer(doc);
        return renderer.renderImageWithDPI(paginaIndex, DPI);
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


public void eliminaHelveticaAnsi(PDDocument doc) throws IOException {
    for (PDPage page : doc.getPages()) {
        PDResources resources = page.getResources();
        if (resources == null) {
            continue;
        }

        for (COSName fontName : resources.getFontNames()) {
            PDFont fontCurent = resources.getFont(fontName);
            if (fontCurent == null || !fontCurent.getName().contains("Helvetica")) {
                continue;
            }
            COSDictionary fontDict = fontCurent.getCOSObject();
            COSBase encoding = fontDict.getItem(COSName.ENCODING);
            if (encoding instanceof COSName && COSName.WIN_ANSI_ENCODING.equals(encoding)) {
                resources.getCOSObject().removeItem(fontName);
            }
        }
    }
}

}

