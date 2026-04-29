package ro.onrc.eliberari.utils;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class ImageProcessor {


    /**
     * Rotește imaginea cu 90 de grade în sensul acelor de ceasornic.
     * Utilă pentru a trece de la Portrait la Landscape.
     */
    public static BufferedImage rotate90(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Tipul imaginii ar trebui păstrat (de ex. TYPE_INT_RGB sau TYPE_BYTE_BINARY)
        BufferedImage newImage = new BufferedImage(height, width, image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType());
        Graphics2D g2 = newImage.createGraphics();

        AffineTransform at = new AffineTransform();
        at.translate(height / 2.0, width / 2.0);
        at.rotate(Math.PI / 2.0);
        at.translate(-width / 2.0, -height / 2.0);

        g2.setTransform(at);
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        return newImage;
    }

    /**
     * Aplică filtrele de curățare: Grayscale -> Blur -> Threshold (Binarizare).
     */
    public static BufferedImage aplicaFiltreCuratare(BufferedImage image) {
        // Pas 1: Conversie la Grayscale
        BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        // Pas 2: Box Blur 3x3 pentru a netezi zgomotul și a uni elementele codului de bare
        float[] matrix = new float[9];
        java.util.Arrays.fill(matrix, 1 / 9f);
        BufferedImageOp blurOp = new ConvolveOp(new Kernel(3, 3, matrix));
        BufferedImage blurred = blurOp.filter(gray, null);

        // Pas 3: Binarizare (Thresholding)
        BufferedImage binary = new BufferedImage(blurred.getWidth(), blurred.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        for (int x = 0; x < blurred.getWidth(); x++) {
            for (int y = 0; y < blurred.getHeight(); y++) {
                // Obținem valoarea canalului 0 (pentru TYPE_BYTE_GRAY, acesta este luminanța)
                int pixel = blurred.getRaster().getSample(x, y, 0);
                if (pixel < 180) { 
                    binary.getRaster().setSample(x, y, 0, 0); // Negru
                } else {
                    binary.getRaster().setSample(x, y, 0, 1); // Alb
                }
            }
        }

        return binary;

    }


        public static boolean estePaginaAlba(BufferedImage imagine) {
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
//        System.out.println("Puncte gasite "+pixeliColorati+"din totalul de "+(width * height /4));
        return pixeliColorati < 100;
    }




}