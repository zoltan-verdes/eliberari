package ro.onrc.eliberari;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import ro.onrc.eliberari.config.AppConfig;
import ro.onrc.eliberari.model.Cerere;
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
@CrossOrigin(origins = "http://localhost:4200")
public class ProcesareController {

    private final ProcesorDocumente procesor;
    private final AppConfig config;

    public ProcesareController(ProcesorDocumente procesor, AppConfig config) {
        this.procesor = procesor;
        this.config = config;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProcesare() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Conexiune de lungă durată

        // Rulăm procesarea pe un fir de execuție separat pentru a nu bloca serverul
        Thread.ofVirtual().start(() -> {
            try {
                List<Cerere> toateRezultatele = new ArrayList<>();

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

                        List<Cerere> rezultateFisier = procesor.proceseaza(f, sseListener);
                        toateRezultatele.addAll(rezultateFisier); // LINIE NOUĂ

                        sseListener.onLog("Fișier finalizat: " + f.getName());
                    }
                }

                sseListener.onLog("--- Toate fișierele au fost procesate ---");

                emitter.send(SseEmitter.event().name("tabel").data(toateRezultatele));

                emitter.complete(); // Închidem fluxul
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    @GetMapping(value = "/stream-image", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    // @GetMapping("/stream-image")
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

}
