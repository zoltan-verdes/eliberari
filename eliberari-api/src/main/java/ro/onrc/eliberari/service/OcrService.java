package ro.onrc.eliberari.service;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Service;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import ro.onrc.eliberari.config.AppConstants;


@Service
public class OcrService {
    private long index = 1;


    public OcrService() {
    }

    public String ocerizeaza(BufferedImage imagine) {
        return ocerizeaza(imagine, 0, 0, imagine.getWidth(), imagine.getHeight()/2);
    }

    public String ocerizeaza(BufferedImage imagine, int x, int y, int w, int h) {
        return ocerizeaza(imagine, x, y, w, h, false);
    } 

    public String ocerizeaza(BufferedImage imagine, int x, int y, int w, int h, boolean singleLine) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(AppConstants.TESSDATA_PATH);
        tesseract.setLanguage("ron");
        tesseract.setVariable("user_defined_dpi", "300");
        if (singleLine) tesseract.setPageSegMode(7);

        System.out.println("OCR pe zona: x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + " din imagine " + imagine.getWidth() + "x" + imagine.getHeight());
        BufferedImage zona = imagine.getSubimage(x, y, w, h);
        
        
        try {
            File fisier = new File("D:\\DEV\\"+"-ocr_test"+x+".png");
            ImageIO.write(zona, "png", fisier);
            String text = tesseract.doOCR(zona);
//              String text = tesseract.doOCR(fisier);
            return text;
        } catch (Exception e) {
            System.err.println("Eroare OCR: " + e.getMessage());
            return "";
        }
    }




}
