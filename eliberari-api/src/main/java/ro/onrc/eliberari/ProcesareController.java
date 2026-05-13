package ro.onrc.eliberari;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import ro.onrc.eliberari.config.AppConfig;
import ro.onrc.eliberari.model.CerereSimpla;
import ro.onrc.eliberari.model.StivaCereri;
import ro.onrc.eliberari.processor.AppRepository;
import ro.onrc.eliberari.processor.ProcesorDocumente;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

@RestController
@RequestMapping("/api/ocr")
@CrossOrigin(origins = "http://localhost:4200", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class ProcesareController {

    private final ProcesorDocumente procesor;
    private final AppConfig config;
    private final AppRepository appRepository;


    public ProcesareController(ProcesorDocumente procesor, AppConfig config, AppRepository appRepository) {
        this.procesor = procesor;
        this.config = config;
        this.appRepository = appRepository;
    }



    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        List<String> response = new ArrayList<>();
        System.out.println("Fișier primit: " + file.getOriginalFilename());
        try {            
            File destFile = appRepository.salveazaFisierIntrare(file);
            procesor.proceseazaLot(destFile);
            response.add(destFile.getName().replace(".zip", ""));
            response.add("S-a creat lotul " + destFile.getName().replace(".zip", ""));
            response.add("Fișier încărcat și procesat cu succes.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Eroare la salvarea fișierului: " + e.getMessage());
            response.add("Eroare la încărcarea fișierului.");
            return ResponseEntity.status(500).body(response);
        }
    }
    @PostMapping("/upload-scan")
    public ResponseEntity<?> uploadScan(@RequestParam("file") MultipartFile file) throws Exception {
        List<String> response = new ArrayList<>();
        System.out.println("Fișier primit: " + file.getOriginalFilename());
        try {            
            File inputDir = new File(config.getOutputFolder());
            if (!inputDir.exists())  inputDir.mkdirs();
            
            File destFile = new File(inputDir, file.getOriginalFilename());
            System.out.println("Salvam fisierul in "+destFile.getAbsolutePath());
            file.transferTo(destFile);
            response.addAll(procesor.proceseazaDocumentScanat(destFile));
            response.add("Fișier încărcat și procesat cu succes.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Eroare la salvarea fișierului: " + e.getMessage());
            e.printStackTrace();
            response.add("Eroare la încărcarea fișierului."+e.getMessage());
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/liste-disponibile")
    public ResponseEntity<List<String>> getListeDisponibile() {
        System.out.println("getListeDiponibile()");
        try {
            List<String> liste = appRepository.getListeDisponibile();
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
        if (this.appRepository.setActiv(numeLot)) {
            // Returnăm direct lista de cereri, nu obiectul wrapper StivaCereri, 
            // pentru a fi mai ușor de mapat în Angular.
            List<CerereSimpla> lista = (new StivaCereri(appRepository.citesteLista(numeLot))).getLista();
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


/*
     Cod vechi pentru a transmite o imagine

    @GetMapping(value = "/stream-image", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter streamImage() throws IOException {
        SseEmitter emitter = new SseEmitter();
        BufferedImage img = procesor.getCodCI();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

        emitter.send(SseEmitter.event().name("image-data").data(base64Image));

        return emitter;
    }
*/