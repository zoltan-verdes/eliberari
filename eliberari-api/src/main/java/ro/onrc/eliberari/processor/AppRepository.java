package ro.onrc.eliberari.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ro.onrc.eliberari.config.AppConfig;
import ro.onrc.eliberari.model.Act;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AppRepository {

    private final ObjectMapper objectMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private final AppConfig appConfig;
    private String activ = "";
    private List<String> listLoturi = new ArrayList<>();

    public AppRepository(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.objectMapper = new ObjectMapper();
        // Înregistrăm modulul pentru a suporta Java 8 Date/Time dacă Act are astfel de câmpuri
        this.objectMapper.registerModule(new JavaTimeModule());


       File folder = new File(appConfig.getLotFolder());
        File[] fisiere = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (fisiere != null) {
            listLoturi = Arrays.stream(fisiere)
                    .map(File::getName)
                    .map(name -> name.replace(".json", ""))
                    .collect(Collectors.toList());
        }
    }


    public File salveazaFisierIntrare(MultipartFile file)    {
        try {            
            File inputDir = new File(appConfig.getInputFolder());
            if (!inputDir.exists())  inputDir.mkdirs();
            else try (var files = Files.list(inputDir.toPath())) {
                files.filter(Files::isRegularFile).forEach(p -> p.toFile().delete());
            }            
            String timestamp = LocalDateTime.now().format(formatter);
            File destFile = new File(inputDir, file.getOriginalFilename().replace(".zip", "")+"- "+timestamp+".zip");
            file.transferTo(destFile);
            return destFile;
        } catch (Exception e) {
            System.out.println("Eroare la salvarea fișierului: " + e.getMessage());
            return null;
        }
    }


    /**
     * Salvează o listă nouă de acte într-un fișier JSON.
     * Adaugă automat timestamp-ul la denumirea fișierului.
     */
    public void salveazaListaNoua(List<Act> acte, String denumireFisier) {
//        String timestamp = LocalDateTime.now().format(formatter);
//        String numeFinal = denumireFisier + " - " + timestamp;
        
        File folder = new File(appConfig.getLotFolder());
        if (!folder.exists()) {
            folder.mkdirs(); // Creăm folderul dacă nu există
        }

        File fisierDestinatie = new File(folder, denumireFisier+".json");

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(fisierDestinatie, acte);
            listLoturi.add(denumireFisier); 
            this.setActiv(denumireFisier); 
            System.out.println("Fișier salvat cu succes: " + fisierDestinatie.getAbsolutePath());
        } catch (IOException e) {
            // Probabil ar trebui să aruncăm o excepție personalizată aici
            System.out.println("Eroare la salvarea listei: "+e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Citește conținutul unui fișier JSON specificat și returnează lista de acte.
     */
    public List<Act> citesteLista(String numeLot) {
        String numeFisier = numeLot + ".json";
        File fisier = new File(appConfig.getLotFolder(), numeFisier);
        if (!fisier.exists()) {
            System.err.println("Fișierul JSON nu a fost găsit: " + fisier.getAbsolutePath());
            return new ArrayList<>();
        }

        try {
            System.out.println("AppRepository.citesteLista cu date");
            return objectMapper.readValue(fisier, new TypeReference<List<Act>>() {});
        } catch (IOException e) {
            System.err.println("Eroare la citirea fișierului JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Returnează o listă cu numele tuturor fișierelor JSON disponibile în folderul configurat.
     */
    public List<String> getListeDisponibile() {
        return listLoturi;
    }

    public boolean setActiv(String activ) {
        if (listLoturi.contains(activ)) {
            this.activ = activ;
            return true;
        } else {
            System.err.println("Fișierul specificat nu există în lista de loturi disponibile: " + activ);
            return false;
        }
    }

    public String getActiv() {
        return activ;
    }

    
}