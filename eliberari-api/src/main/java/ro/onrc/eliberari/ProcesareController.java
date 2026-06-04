package ro.onrc.eliberari;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ro.onrc.eliberari.config.AppConfig;
import ro.onrc.eliberari.model.CerereSimpla;
import ro.onrc.eliberari.processor.ProcesorLotDirector;
import ro.onrc.eliberari.processor.ProcesorScanat;
import ro.onrc.eliberari.service.AppRepository;
import ro.onrc.eliberari.service.LotRegistry;


import java.io.File;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api/ocr")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
/*@CrossOrigin(
    originPatterns = "http://192.168.*", 
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)*/
public class ProcesareController {

    private final ProcesorLotDirector procesorLotDirector;
    private final ProcesorScanat procesorScanat;
    private final AppConfig config;
    private final LotRegistry lotRegistry;
    



    public ProcesareController(ProcesorLotDirector procesor, AppConfig config, LotRegistry lotRegistry, ProcesorScanat procesorScanat) {
        this.procesorLotDirector = procesor;
        this.config = config;
        this.lotRegistry = lotRegistry;
        this.procesorScanat = procesorScanat;
    }



    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        List<String> response = new ArrayList<>();
        System.out.println("Fișier primit: " + file.getOriginalFilename());
        try {            
            String idLot = procesorLotDirector.proceseazaLot(file);
            if (idLot == null)  throw new Exception("Eroare la procesarea lotului");
            response.add(idLot);
            response.add("Lot " + idLot + " încărcat și procesat cu succes.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Eroare la salvarea fișierului: " + e.getMessage());
            response.add("Eroare la încărcarea fișierului.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/tipareste-lot-dir")
    public ResponseEntity<String> tiparesteLotDir(@RequestParam("nume") String numeLot) {
        System.out.println("Tipareste Lot Director ("+numeLot+")");
        try {
                procesorLotDirector.tiparesteLot(numeLot);
                return ResponseEntity.ok("Lotul " + numeLot + " a fost trimis la imprimantă cu succes.");
        } catch (Exception e) {
            // Pe eroare 500 trimitem un simplu String.
            return ResponseEntity.status(500).body("Eroare la aducerea tabelului: " + e.getMessage());
        }
    }    



    @PostMapping("/upload-scan")
    public ResponseEntity<?> uploadScan(@RequestParam("file") MultipartFile file) throws Exception {
        boolean[] response;
        System.out.println("Fișier primit: " + file.getOriginalFilename());
        try {            
            File destFile = lotRegistry.setFisierScanat(file);
            response = procesorScanat.proceseazaDocumentScanat(destFile);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Eroare la salvarea fișierului: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/desparte")
    public ResponseEntity<String> desparte(@RequestBody DesparteRequest request) {
        System.out.println("Solicitare despartire pentru lotul: " + request.numeLot());
        try {
            File inputDir = new File(config.getOutputFolder());
            File fisier = new File(inputDir, request.numeLot() + ".pdf");
            
            if (!fisier.exists()) {
                return ResponseEntity.status(404).body("Fișierul scanat nu a fost găsit pe server.");
            }

            lotRegistry.savePageStatuses(request.numeLot(), request.statusChanged());
            String mesaj = procesorScanat.desparteFisierScanat(request.numeLot(), request.statusChanged());
            
            return ResponseEntity.ok().body(mesaj);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Eroare la procesarea separării: " + e.getMessage());
        }
    }


    @GetMapping("/liste-disponibile")
    public ResponseEntity<List<String[]>> getListeDisponibile() {
        System.out.println("getListeDiponibile()");
        try {
            List<String[]> liste = lotRegistry.getListaLoturiExtins();
            return ResponseEntity.ok(liste);
        } catch (Exception e) {
            System.out.println("Eroare la obținerea listelor disponibile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }


@PostMapping("/set-activ")
public ResponseEntity<?> setActiv(@RequestParam("nume") String numeLot) {
    System.out.println("SetAcitv("+numeLot+")");
    try {
        if (this.lotRegistry.exists(numeLot)) {
            // Returnăm direct lista de cereri, nu obiectul wrapper StivaCereri, 
            // pentru a fi mai ușor de mapat în Angular.
            List<CerereSimpla> lista = lotRegistry.getLotForUI(numeLot);
            return ResponseEntity.ok(lista);
        } else {
            // Trimitem doar 404, fără body. Angular va decide ce să facă.
            return ResponseEntity.notFound().build();
        }
    } catch (Exception e) {
        // Pe eroare 500 trimitem un simplu String.
        return ResponseEntity.status(500).body("Eroare la aducerea tabelului: " + e.getMessage());
    }
}    

}

record DesparteRequest(String numeLot, boolean[] statusChanged) {}    
