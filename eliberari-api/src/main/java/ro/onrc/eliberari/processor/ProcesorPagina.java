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
import java.awt.image.BufferedImage;

@Component
public class ProcesorPagina {
    private final OcrService ocrService;
    private final PdfService pdfService;
    private final BarcodeService barcodeService;

    public ProcesorPagina(OcrService ocrService, PdfService pdfService, BarcodeService barcodeService) {
        this.ocrService = ocrService;
        this.pdfService = pdfService;
        this.barcodeService = barcodeService;
    }

    InfoPagina prelucrarePagina(BufferedImage imagine) {
        String numar = "negasit";
        String data = "negasit";
        String cui = "negasit";
        String firma = "negasit";
        String barcode = "";
        TipPagina tipPag = TipPagina.Altele;
        if (pdfService.estePaginaGoala(imagine))
            return new InfoPagina(TipPagina.PagGoala);
        String text = ocrService.ocerizeaza(imagine, 0, AppConstants.Y_START_O, imagine.getWidth(),
                AppConstants.Y_HEIGHT_O);
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
