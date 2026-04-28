package ro.onrc.eliberari.service;

import java.awt.image.BufferedImage;
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

import ro.onrc.eliberari.utils.ImageProcessor;


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
                        if (ImageProcessor.estePaginaAlba(imgBruta)) {
                            imagineCurata[index] = null; // Sari peste pagina aceasta
                        } else {
                            // 3. Curățare și Binarizare (pentru claritate și dimensiune)
                            imagineCurata[index] = ImageProcessor.aplicaFiltreCuratare(imgBruta);
                        }

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

    public File eliminaGoale(File fisierIntrare) throws Exception {
        File fisierIesire = new File(fisierIntrare.getParent(), fisierIntrare.getName()+"_fara_pag_goale.pdf");
        Semaphore ocrLimit = new Semaphore(40);

        try (PDDocument docOriginal = PDDocument.load(fisierIntrare);
                PDDocument docNou = new PDDocument()) {

            int nrPagini = docOriginal.getNumberOfPages();
            CountDownLatch latch = new CountDownLatch(nrPagini);
            boolean[] paginiGoale = new boolean[nrPagini];

            for (int i = 0; i < nrPagini; i++) {
                final int index = i;
                Thread.ofVirtual().start(() -> {
                    try {
                        BufferedImage imgBruta = pdfService.randeazaPagina(docOriginal, index);
                        paginiGoale[index] = ImageProcessor.estePaginaAlba(imgBruta);
                        ocrLimit.acquire();

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();

                    } catch (IOException e) {
                        System.err.println("Eroare la pagina " + index + ": " + e.getMessage());
                    } finally {
                        ocrLimit.release();
                        latch.countDown();
                    }
                });
            }
            latch.await();

            for (int i = 0; i < docOriginal.getNumberOfPages(); i++) {
                if (paginiGoale[i]) {
                    continue;
                }
                PDPage pagina = docOriginal.getPage(i);
                docNou.addPage(pagina);
            }

            docNou.save(fisierIesire);
        }
        return fisierIesire;
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

    

}