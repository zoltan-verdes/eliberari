package ro.onrc.eliberari.service;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.CCITTFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import ro.onrc.eliberari.model.InfoPagina;

@Service
public class DocumentOptimizer {

    private final PdfService pdfService; // Serviciul tău actual de randare

    public DocumentOptimizer(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    public File optimizeazaSiCurata(File fisierIntrare) throws Exception {
        File fisierIesire = new File(fisierIntrare.getParent(), "CLEAN_" + fisierIntrare.getName());
        Semaphore ocrLimit = new Semaphore(40);

        try (PDDocument docOriginal = PDDocument.load(fisierIntrare);
                PDDocument docNou = new PDDocument()) {

            int nrPagini = docOriginal.getNumberOfPages();
            CountDownLatch latch = new CountDownLatch(nrPagini);
            BufferedImage[] imagineCurata = new BufferedImage[nrPagini];

            for (int i = 0; i < nrPagini; i++) {
                final int index = i;
                Thread.ofVirtual().start(() -> {
                    try {
                        // 1. Randează pagina la 300 DPI
                        BufferedImage imgBruta = pdfService.randeazaPagina(docOriginal, index);

                        // 2. Verifică dacă este albă (folosești metoda ta existentă)
                        if (estePaginaAlba(imgBruta)) {
                            imagineCurata[index] = null; // Sari peste pagina aceasta
                        } else
                            // 3. Curățare și Binarizare (pentru claritate și dimensiune)
                            imagineCurata[index] = aplicaFiltreCuratare(imgBruta);
                        ocrLimit.acquire();

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

            for (int i = 0; i < docOriginal.getNumberOfPages(); i++) {
                // 2. Verifică dacă este albă (folosești metoda ta existentă)
                if (imagineCurata[i] == null) {
                    continue; // Sari peste pagina aceasta
                }
                // 4. Adaugă în noul document cu compresie G4
                adaugaPaginaBinarizata(docNou, imagineCurata[i]);
            }

            docNou.save(fisierIesire);
        }
        return fisierIesire;
    }

    private BufferedImage aplicaFiltreCuratare(BufferedImage src) {
        // Aici pui logica de Blur + Threshold discutată anterior
        // Aceasta elimină granulația (dithering-ul)
        // Pas 1: Conversie la Grayscale (dacă nu e deja)
        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();

        // Pas 2: Aplicăm un filtru de Blur (Box Blur) 3x3
        // Acesta unește punctele negre care formează barele codului
        float[] matrix = new float[9];
        java.util.Arrays.fill(matrix, 1 / 9f);
        BufferedImageOp blurOp = new ConvolveOp(new Kernel(3, 3, matrix));
        BufferedImage blurred = blurOp.filter(gray, null);

        // Pas 3: Binarizare (Thresholding)
        // Valorile sub 180 devin negru pur (0), peste devin alb pur (255)
        BufferedImage binary = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        for (int x = 0; x < blurred.getWidth(); x++) {
            for (int y = 0; y < blurred.getHeight(); y++) {
                int pixel = blurred.getRaster().getSample(x, y, 0);
                if (pixel < 180) { // Prag de binarizare
                    binary.getRaster().setSample(x, y, 0, 0); // Negru
                } else {
                    binary.getRaster().setSample(x, y, 0, 1); // Alb
                }
            }
        }
        return binary;
    }

    private void adaugaPaginaBinarizata(PDDocument doc, BufferedImage img) throws IOException {

        // PDPage pagina = new PDPage(new PDRectangle(img.getWidth(), img.getHeight()));

        PDRectangle formatA4 = PDRectangle.A4;
        PDPage pagina = new PDPage(formatA4);

        doc.addPage(pagina);

        // Forțăm compresia CCITT G4 pentru dimensiune minimă
        PDImageXObject pdImage = CCITTFactory.createFromImage(doc, img);
        try (PDPageContentStream contents = new PDPageContentStream(doc, pagina)) {
            contents.drawImage(pdImage, 0, 0, formatA4.getWidth(), formatA4.getHeight());
        }
    }

    private boolean estePaginaAlba(BufferedImage imagine) {
        // Metoda ta de detectare pagini goale
        int width = imagine.getWidth();
        int height = imagine.getHeight();
        long pixeliColorati = 0;

        // Prag de luminozitate: 240 (aproape alb).
        // Orice e mai mic de 240 este considerat "zgomot" sau text.
        int pragAlb = 240;

        // Scanăm imaginea (sărim peste pixeli pentru viteză, ex: din 5 în 5)
        for (int y = 0; y < height / 2; y += 2) {
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
        // System.out.println("Puncte gasite "+pixeliColorati+"din totalul de "+(width *
        // height /4));

        return pixeliColorati < 100;
    }

    public BufferedImage rotateImage90(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        // Creăm o imagine nouă cu dimensiunile inversate
        BufferedImage newImage = new BufferedImage(height, width, img.getType());
        Graphics2D g2 = newImage.createGraphics();

        // Configurăm transformarea
        AffineTransform at = new AffineTransform();
        // 1. Mutăm originea în centrul noii imagini
        at.translate(height / 2.0, width / 2.0);
        // 2. Rotim cu 90 de grade (Radiani: PI/2)
        at.rotate(Math.PI / 2.0);
        // 3. Mutăm înapoi pentru a desena corect din colțul imaginii sursă
        at.translate(-width / 2.0, -height / 2.0);

        g2.setTransform(at);
        g2.drawImage(img, 0, 0, null);
        g2.dispose();

        return newImage;
    }

}