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
import ro.onrc.eliberari.model.CerereDTO;
import ro.onrc.eliberari.processor.ProcesorDocumente;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

@RestController
@RequestMapping("/api/ocr")
@CrossOrigin(origins = "http://localhost:4200", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class ProcesareController {

    private final ProcesorDocumente procesor;
    private final AppConfig config;

    public ProcesareController(ProcesorDocumente procesor, AppConfig config) {
        this.procesor = procesor;
        this.config = config;
    }

    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter streamProcesare() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Conexiune de lungă durată

        // Rulăm procesarea pe un fir de execuție separat pentru a nu bloca serverul
        Thread.ofVirtual().start(() -> {
            try {

                LogListener sseListener = (mesaj) -> {
                    try {
                        System.out.println(mesaj);
                        emitter.send(SseEmitter.event().data(mesaj));
                    } catch (IOException e) { // Conexiune închisă de client
                    }
                };

                // Exemplu de integrare cu logica ta existentă
                File inputDir = new File(config.getInputFolder());
                File[] fisiere = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

                if (fisiere != null) {
                    for (File f : fisiere) {
                        sseListener.onLog("IN streamProcesare(): Se procesează fișierul: " + f.getName());

                        // Aici apelezi metoda ta de procesare

                        procesor.recunoastereActeScanate(f, sseListener);

                        sseListener.onLog("Fișier finalizat: " + f.getName());
                    }
                }

                sseListener.onLog("--- Toate fișierele au fost procesate ---");

                List<CerereDTO> cereriDTO = procesor.getLotCereri().getToate().stream().map(CerereDTO::new).toList();
                emitter.send(SseEmitter.event().name("tabel").data(cereriDTO));


                emitter.complete(); // Închidem fluxul
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    @GetMapping(value = "/stream-image", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter streamImage() throws IOException {
        SseEmitter emitter = new SseEmitter();

        // Presupunem că ai BufferedImage-ul tău (poate cel rotit)
        BufferedImage img = procesor.getCodCI();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

        // Trimitem string-ul prin SSE
        emitter.send(SseEmitter.event().name("image-data").data(base64Image));

        return emitter;
    }

    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        List<String> response = new ArrayList<>();
        System.out.println("Fișier primit: " + file.getOriginalFilename());
        try {            
            File inputDir = new File(config.getInputFolder());
            if (!inputDir.exists())  inputDir.mkdirs();
            
            File destFile = new File(inputDir, file.getOriginalFilename());
            file.transferTo(destFile);
            procesor.proceseazaLot(destFile);
            response.add("Fișier încărcat și procesat cu succes.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Eroare la salvarea fișierului: " + e.getMessage());
            e.printStackTrace();
            response.add("Eroare la încărcarea fișierului.");
            return ResponseEntity.status(500).body(response);
        }
    }
    @PostMapping("/upload-scan")
    public ResponseEntity<List<String>> uploadScan(@RequestParam("file") MultipartFile file) throws Exception {
        List<String> response = new ArrayList<>();
        System.out.println("Fișier primit: " + file.getOriginalFilename());
        try {            
            File inputDir = new File(config.getOutputFolder());
            if (!inputDir.exists())  inputDir.mkdirs();
            
            File destFile = new File(inputDir, file.getOriginalFilename());
            file.transferTo(destFile);
            response.addAll(procesor.proceseazaDocumentScanat(destFile));
            response.add("Fișier încărcat și procesat cu succes.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Eroare la salvarea fișierului: " + e.getMessage());
            e.printStackTrace();
            response.add("Eroare la încărcarea fișierului."+e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

}
