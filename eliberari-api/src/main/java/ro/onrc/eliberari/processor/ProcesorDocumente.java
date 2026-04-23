package ro.onrc.eliberari.processor;

import ro.onrc.eliberari.LogListener;
import ro.onrc.eliberari.config.AppConfig;
import ro.onrc.eliberari.config.AppConstants;
import ro.onrc.eliberari.model.Cerere;
import ro.onrc.eliberari.model.InfoPagina;
import ro.onrc.eliberari.model.TipPagina;
import ro.onrc.eliberari.service.OcrService;
import ro.onrc.eliberari.service.PdfService;


import org.apache.logging.log4j.simple.internal.SimpleProvider.Config;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProcesorDocumente {


    private final OcrService ocrService;
    private final PdfService pdfService;
    private final AppConfig config;
    private final ProcesorPagina procPagina;
   

    // Spring injectează automat serviciile prin constructor
    public ProcesorDocumente(ProcesorPagina procPagina, OcrService ocrService, PdfService pdfService, AppConfig config) {
        this.procPagina = procPagina;
        this.ocrService = ocrService;
        this.pdfService = pdfService;
        this.config = config;
    }

    public List<Cerere> proceseaza(File fisier, LogListener listener) throws Exception {
        // 1. Obținem token-ul (se face o singură dată sau la expirare)
//        String token = authService.getAccessToken();
//        System.out.println("Autentificat cu succes. Token activ.");
        List<Cerere> listaCereri = new ArrayList<>();
        try (PDDocument document = PDDocument.load(fisier)) {
            List<Integer> paginiCurente = new ArrayList<>();
            
            int contorDocumente = 1;
            System.out.println("Avem un document cu "+document.getNumberOfPages()+" pagini");
            InfoPagina infoPag;

            
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                System.out.println("procesam pagina "+i); listener.onLog("procesam pagina "+i);
                var imagine = pdfService.randeazaPagina(document, i);
                infoPag = procPagina.prelucrarePagina(imagine);
                if (infoPag.getPagina()!=TipPagina.PagGoala){
                System.out.println("Actul  -- "+infoPag.getPagina());

                
                if (infoPag.getPagina()==TipPagina.Incheiere) {
                if (!paginiCurente.isEmpty() && !listaCereri.isEmpty()) {
                        Cerere cerere = listaCereri.getLast();
                        listener.onLog("Salvam: "+cerere.getNumar()+"_" + cerere.getData() +"_"+cerere.getFirma()+"_"+paginiCurente.size()+ ".pdf");
                        pdfService.salveazaGrupPagini(document, paginiCurente, cerere.getNumar()+"_" + cerere.getData() +"_"+cerere.getFirma()+"_"+paginiCurente.size()+ ".pdf");
                        contorDocumente++;
                        paginiCurente.clear();
                }

                listaCereri.add(new Cerere(infoPag));
            }
                
                    
                if (infoPag.getPagina()!=TipPagina.Altele) {
                    listener.onLog("pagina de "+infoPag.getPagina());
                    listener.onLog("info: nr."+infoPag.getNumar()+"/" + infoPag.getData() +"  Firma:"+infoPag.getFirma()+"  CUI:"+infoPag.getCui());
                    if (infoPag.getPagina()==TipPagina.Incheiere) listaCereri.getLast().setInch(true);
                    if (infoPag.getPagina()==TipPagina.CI) listaCereri.getLast().setCi(true);
                    if (infoPag.getPagina()==TipPagina.CIM) listaCereri.getLast().setCim(true);
                    if (infoPag.getPagina()==TipPagina.Constatator) listaCereri.getLast().addCc();
                }
                paginiCurente.add(i);
            } else System.out.println("pagina goala");
            }

            // Salvăm și ultimul set de pagini
            if (!paginiCurente.isEmpty()) {
                pdfService.salveazaGrupPagini(document, paginiCurente, listaCereri.getLast().getNumar()+"_" + listaCereri.getLast().getData() +"_"+listaCereri.getLast().getFirma()+"_"+paginiCurente.size() + ".pdf");
            }
        }
        return listaCereri;
    }
}