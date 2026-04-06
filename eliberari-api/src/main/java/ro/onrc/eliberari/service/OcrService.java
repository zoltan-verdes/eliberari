package ro.onrc.eliberari.service;

import net.sourceforge.tess4j.Tesseract;
import ro.onrc.eliberari.config.AppConfig;
import ro.onrc.eliberari.config.AppConstants;
import ro.onrc.eliberari.model.InfoPagina;
import ro.onrc.eliberari.model.TipPagina;


import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OcrService {

    private final Tesseract tesseract;

    public OcrService(AppConfig config) {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(AppConstants.TESSDATA_PATH);
        this.tesseract.setLanguage("ron");
    }


    public String  ocerizeaza(BufferedImage imagine){
            BufferedImage zona = imagine.getSubimage(0, AppConstants.Y_START_O, imagine.getWidth(), AppConstants.Y_HEIGHT_O);
            try{
                String text = tesseract.doOCR(zona);
                return text;        
            } catch (Exception e) {
            return "";
        }
    }

    public boolean contineMarker(BufferedImage imagine, String marker) {
        try {
            // Decupăm zona de interes direct aici
//            System.out.println("Dimensiuni pagina" + imagine.getWidth()+","+imagine.getHeight());
            BufferedImage zona = imagine.getSubimage(0, AppConstants.Y_START, imagine.getWidth(), AppConstants.Y_HEIGHT);
            String text = tesseract.doOCR(zona);
            return text.contains(marker);
        } catch (Exception e) {
            return false;
        }
    }

    public InfoPagina extrageInformatiiCerere(BufferedImage imagine) {
        try {
            // Decupăm zona de interes direct aici
//            System.out.println("Dimensiuni pagina" + imagine.getWidth()+","+imagine.getHeight());
            BufferedImage zona = imagine.getSubimage(0, AppConstants.Y_START_C, imagine.getWidth(), AppConstants.Y_HEIGHT_C);
            String text = tesseract.doOCR(zona);
//      sablon pentru Dovezi de ridicare numar si data cererii
//            Matcher potrivire = cautaPatternMultiplu(text, "Cerere:?\\s*(\\d+)\\s*din\\s*data\\s*(\\d{2}[.\\/-]\\d{2}[.\\/-]\\d{4})");
//      sablon pentru incheieri
        System.out.println(text);
            Matcher potrivire = cautaPatternMultiplu(text, "DOSAR\\s*NR.\\s*(\\d+)\\s*/\\s*(\\d{2}[.\\/-]\\d{2}[.\\/-]\\d{4})");
            String numar = "negasit";
            String data = "negasit";
            if (potrivire.find()){
                System.out.println("am gasit potrivire");
                numar = potrivire.group(1);
                data = potrivire.group(2);
            }
            String cui = cautaPattern(text, "Cod unic de inregistrare:\\.?\\s*(\\d+)"); // Caută "Nr. 123" sau "Nr 123"
//            String firma = cautaPattern(text, "Firma:\\.?\\s*(.*)\\s*CUI"); // Caută "Nr. 123" sau "Nr 123"
            String firma = cautaPattern(text, "Firma:\\.?\\s*(.*)\\s*Sediul"); // Caută "Nr. 123" sau "Nr 123"
//            return text.contains(marker);
            if (firma.length()>15) firma = firma.substring(0,15);
            return new InfoPagina(TipPagina.Altele,numar, data, cui, firma);
        } catch (Exception e) {
            return null;
        }
    }


    private String cautaPattern(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "negasit";
    }

    private Matcher cautaPatternMultiplu(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher;
    }
 
}

