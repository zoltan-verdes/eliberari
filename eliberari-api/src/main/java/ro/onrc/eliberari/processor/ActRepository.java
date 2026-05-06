package ro.onrc.eliberari.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ro.onrc.eliberari.config.AppConfig;
import ro.onrc.eliberari.model.Act;

import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ActRepository {

    private final ObjectMapper objectMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private final AppConfig appConfig;

    public ActRepository(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.objectMapper = new ObjectMapper();
        // Înregistrăm modulul pentru a suporta Java 8 Date/Time dacă Act are astfel de câmpuri
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Salvează o listă nouă de acte într-un fișier JSON.
     * Adaugă automat timestamp-ul la denumirea fișierului.
     */
    public void salveazaListaNoua(List<Act> acte, String denumireFisier) {
        String timestamp = LocalDateTime.now().format(formatter);
        String numeFinal = denumireFisier + " - " + timestamp + ".json";
        
        File folder = new File(appConfig.getLotFolder());
        if (!folder.exists()) {
            folder.mkdirs(); // Creăm folderul dacă nu există
        }

        File fisierDestinatie = new File(folder, numeFinal);

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(fisierDestinatie, acte);
            System.out.println("Fișier salvat cu succes: " + fisierDestinatie.getAbsolutePath());
        } catch (IOException e) {
            // Probabil ar trebui să aruncăm o excepție personalizată aici
            e.printStackTrace();
        }
    }

    /**
     * Citește conținutul unui fișier JSON specificat și returnează lista de acte.
     */
    public List<Act> citesteLista(String numeFisierComplet) {
        File fisier = new File(appConfig.getLotFolder(), numeFisierComplet);
        
        if (!fisier.exists()) {
            System.err.println("Fișierul nu a fost găsit: " + numeFisierComplet);
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(fisier, new TypeReference<List<Act>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Returnează o listă cu numele tuturor fișierelor JSON disponibile în folderul configurat.
     */
    public List<String> getListeDisponibile() {
        File folder = new File(appConfig.getLotFolder());
        File[] fisiere = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (fisiere == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(fisiere)
                .map(File::getName)
                .collect(Collectors.toList());
    }
}