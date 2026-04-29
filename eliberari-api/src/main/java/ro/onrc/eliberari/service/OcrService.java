package ro.onrc.eliberari.service;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Service;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import ro.onrc.eliberari.config.AppConstants;


@Service
public class OcrService {
   


    public OcrService() {
    }

    public String ocerizeaza(BufferedImage imagine) {
        return ocerizeaza(imagine, 0, 0, imagine.getWidth(), imagine.getHeight()/2);
    }

    public String ocerizeaza(BufferedImage imagine, int x, int y, int w, int h) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(AppConstants.TESSDATA_PATH);
        tesseract.setLanguage("ron");

        System.out.println("OCR pe zona: x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + " din imagine " + imagine.getWidth() + "x" + imagine.getHeight());
        BufferedImage zona = imagine.getSubimage(x, y, w, h);
        
        
        try {
            File fisier = new File("D:\\DEV\\ocr_test"+x+".png");
            ImageIO.write(zona, "png", fisier);
            String text = tesseract.doOCR(zona);
            System.out.println("Text extras: " + text);
            return text;
        } catch (Exception e) {
            System.err.println("Eroare OCR: " + e.getMessage());
            return "";
        }
    }




}
