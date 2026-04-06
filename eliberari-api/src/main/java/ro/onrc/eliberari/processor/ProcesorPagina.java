package ro.onrc.eliberari.processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import ro.onrc.eliberari.model.TipPagina;
import ro.onrc.eliberari.model.InfoPagina;
import ro.onrc.eliberari.service.OcrService;
import ro.onrc.eliberari.service.PdfService;
import java.awt.image.BufferedImage;


@Component
public class ProcesorPagina {
    private final OcrService ocrService;
    private final PdfService pdfService;

    public ProcesorPagina(OcrService ocrService, PdfService pdfService) {
        this.ocrService = ocrService;
        this.pdfService = pdfService;
    }

    InfoPagina prelucrarePagina(BufferedImage imagine){
        String numar = "negasit";
        String data = "negasit";
        String cui  = "negasit";
        String firma = "negasit";
        TipPagina tipPag = TipPagina.Altele;
        if (pdfService.estePaginaGoala(imagine)) return new InfoPagina(TipPagina.PagGoala);
        String text = ocrService.ocerizeaza(imagine);
        System.out.println("---\n---\n"+text+"\n----");
        if (text.contains("NCHEIERE")) tipPag = TipPagina.Incheiere;
        if (text.contains("CERTIFICAT DE")) tipPag = TipPagina.CIM;
        if (text.contains("CONSTATATOR")) tipPag = TipPagina.Constatator;
        System.out.println("Tip act: "+tipPag);
        if (tipPag != TipPagina.Altele) {
        Matcher potrivire = cautaPatternMultiplu(text, "DOSAR\\s*NR.\\s*(\\d+)\\s*/\\s*(\\d{2}[.\\/-]\\d{2}[.\\/-]\\d{4})");
        if (potrivire.find()){
               System.out.println("am gasit potrivire");
                numar = potrivire.group(1);
                data = potrivire.group(2);
            }
        cui = cautaPattern(text, "Cod unic de inregistrare:\\.?\\s*(\\d+)"); // Caută "Nr. 123" sau "Nr 123"
        firma = cautaPattern(text, "Firma:\\.?\\s*(.*)\\s*Sediul"); // Caută "Nr. 123" sau "Nr 123"
        if (firma.length()>15) firma = firma.substring(0,15);
        }


        return new InfoPagina(tipPag,numar,data,cui,firma);
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
