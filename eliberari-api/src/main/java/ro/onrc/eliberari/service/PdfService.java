package ro.onrc.eliberari.service;

import org.apache.pdfbox.pdmodel.PDDocument;
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

    public void salveazaGrupPagini(PDDocument sursa, List<Integer> pagini, String numeFisier) throws IOException {
        try (PDDocument nou = new PDDocument()) {
            for (int index : pagini) {
                nou.addPage(sursa.getPage(index));
            }
            nou.save(new File(config.getOutputFolder() + numeFisier));
        }
    }

    public boolean estePaginaGoala(BufferedImage imagine) {
    int width = imagine.getWidth();
    int height = imagine.getHeight();
    long pixeliColorati = 0;
    
    // Prag de luminozitate: 240 (aproape alb). 
    // Orice e mai mic de 240 este considerat "zgomot" sau text.
    int pragAlb = 240; 

    // Scanăm imaginea (sărim peste pixeli pentru viteză, ex: din 5 în 5)
    for (int y = 0; y < height/2; y+=2) {
        for (int x = 0; x < width; x++) {
            int color = imagine.getRGB(x, y);
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = (color & 0xFF);

            if (r < pragAlb || g < pragAlb || b < pragAlb) {
                pixeliColorati++;
            }
        }
    }
 //   System.out.println("Puncte gasite "+pixeliColorati+"din totalul de "+(width * height /4));
    
    return pixeliColorati < 100; 
}

}