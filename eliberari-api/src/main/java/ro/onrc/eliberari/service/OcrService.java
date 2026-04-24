package ro.onrc.eliberari.service;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Service;
import java.awt.image.BufferedImage;
import ro.onrc.eliberari.config.AppConstants;

@Service
public class OcrService {

    public OcrService() {
    }

    public String ocerizeaza(BufferedImage imagine) {
        return ocerizeaza(imagine, 0, AppConstants.Y_START_O, imagine.getWidth(), AppConstants.Y_HEIGHT_O);
    }

    public String ocerizeaza(BufferedImage imagine, int x, int y, int w, int h) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(AppConstants.TESSDATA_PATH);
        tesseract.setLanguage("ron");

        BufferedImage zona = imagine.getSubimage(x, y, w, h);
        try {
            String text = tesseract.doOCR(zona);
            return text;
        } catch (Exception e) {
            return "";
        }
    }

    public boolean contineMarker(BufferedImage imagine, String marker) {
        try {
            BufferedImage zona = imagine.getSubimage(0, AppConstants.Y_START, imagine.getWidth(),
                    AppConstants.Y_HEIGHT);
            String text = ocerizeaza(zona);
            return text.contains(marker);
        } catch (Exception e) {
            return false;
        }
    }

}
