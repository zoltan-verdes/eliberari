package ro.onrc.eliberari.processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import ro.onrc.eliberari.model.TipPagina;
import ro.onrc.eliberari.config.AppConstants;
import ro.onrc.eliberari.model.InfoPagina;
import ro.onrc.eliberari.service.BarcodeService;
import ro.onrc.eliberari.service.OcrService;
import ro.onrc.eliberari.service.PdfService;
import ro.onrc.eliberari.utils.ImageProcessor;

import java.awt.image.BufferedImage;

@Component
public class ProcesorPagina {
    private final OcrService ocrService;
    private final BarcodeService barcodeService;

    public ProcesorPagina(OcrService ocrService, PdfService pdfService, BarcodeService barcodeService) {
        this.ocrService = ocrService;
        this.barcodeService = barcodeService;
    }

    InfoPagina prelucrarePagina(BufferedImage imagine) {
        String numar = "negasit";
        String data = "negasit";
        String cui = "negasit";
        String firma = "negasit";
        String barcode = "";
        TipPagina tipPag = TipPagina.Altele;
        if (ImageProcessor.estePaginaAlba(imagine))
            return new InfoPagina(TipPagina.PagGoala);
        String text = ocrService.ocerizeaza(imagine, 0, 0, imagine.getWidth(), imagine.getHeight() / 3);
        System.out.println("---\n---\n" + text + "\n----");
        if (text.contains("NCHEIERE"))
            tipPag = TipPagina.Incheiere;
        if (text.contains("CERTIFICAT DE"))
            tipPag = TipPagina.CIM;
        if (text.contains("CONSTATATOR"))
            tipPag = TipPagina.Constatator;
        System.out.println("Tip act: " + tipPag);
        barcode = barcodeService.citesteCod(imagine, 230, 590, 600, 250);
        if (barcode == null)
            barcode = barcodeService.citesteCod(imagine, 700, 2600, 350, 450);

        if (tipPag != TipPagina.Altele) {
            Matcher potrivire = cautaPatternMultiplu(text,
                    "DOSAR\\s*NR.\\s*(\\d+)\\s*/\\s*(\\d{2}[.\\/-]\\d{2}[.\\/-]\\d{4})");
            if (potrivire.find()) {
                System.out.println("am gasit potrivire");
                numar = potrivire.group(1);
                data = potrivire.group(2);
            }
            cui = cautaPattern(text, "Cod unic de inregistrare:\\.?\\s*(\\d+)"); // Caută "Nr. 123" sau "Nr 123"
            firma = cautaPattern(text, "Firma:\\.?\\s*(.*)\\s*Sediul"); // Caută "Nr. 123" sau "Nr 123"
            if (firma.length() > 20)
                firma = firma.substring(0, 20);
        }

        return new InfoPagina(tipPag, numar, data, cui, firma, barcode);
    }


    public boolean isMarkerPresent(BufferedImage imagine, TipPagina tipPagina) {
        switch (tipPagina) {
            case Incheiere: return isIncheiere(imagine);
            case Constatator: return isConstator(imagine);
            case CIM: return isCIM(imagine);
            case CI: return isCI(imagine);
            default: return false;
        }
    }

    public boolean isIncheiere(BufferedImage imagine) {
        String text = ocrService.ocerizeaza(imagine, AppConstants.INCH_X, AppConstants.INCH_Y, AppConstants.INCH_WIDTH, AppConstants.INCH_HEIGHT);
        return text.contains("ÎNCHEIERE");
    }

    public boolean isConstator(BufferedImage imagine) {
        String text = ocrService.ocerizeaza(imagine, AppConstants.CC_X, AppConstants.CC_Y, AppConstants.CC_WIDTH, AppConstants.CC_HEIGHT);
        return text.contains("CERTIFICAT CONSTATATOR");
    }

    public boolean isCI(BufferedImage imagine) {
        String text = ocrService.ocerizeaza(imagine, AppConstants.CI_X, AppConstants.CI_Y, AppConstants.CI_WIDTH, AppConstants.CI_HEIGHT);
        return text.contains("CERTIFICAT DE ");
    }

    public boolean isCIM(BufferedImage imagine) {
        String text = ocrService.ocerizeaza(imagine, AppConstants.CIM_X, AppConstants.CIM_Y, AppConstants.CIM_WIDTH, AppConstants.CIM_HEIGHT);
        return text.contains("CERTIFICAT DE");
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
