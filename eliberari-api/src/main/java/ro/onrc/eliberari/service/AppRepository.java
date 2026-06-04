package ro.onrc.eliberari.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ro.onrc.eliberari.config.AppConfig;
import ro.onrc.eliberari.model.Act;
import ro.onrc.eliberari.model.TipAct;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
public class AppRepository {

    private final ObjectMapper objectMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private final AppConfig config;
    private final ZipService zipService;
    private String activ = "";
    private List<String> listLoturi = new ArrayList<>();
    private List<File> fisiereLotCurent = new ArrayList<>();
    private String idLotCurent = "";
    

    public AppRepository(AppConfig config, ZipService zipService) {
        this.config = config;
        this.zipService = zipService;
        this.objectMapper = new ObjectMapper();
        // Înregistrăm modulul pentru a suporta Java 8 Date/Time dacă Act are astfel de câmpuri
        this.objectMapper.registerModule(new JavaTimeModule());


       File folder = new File(config.getLotFolder());
        File[] fisiere = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        long limitaDouaZile = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000);
        if (fisiere != null) {
            listLoturi = Arrays.stream(fisiere)
                    .filter(f -> f.lastModified() >= limitaDouaZile)
                    .sorted((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()))
                    .map(File::getName)
                    .map(name -> name.replace(".json", ""))
                    .collect(Collectors.toList());
        }
    }


    public String salveazaLotDirectorZip(MultipartFile file)    {
        try {            
            File inputDir = new File(config.getInputFolder());
            if (!inputDir.exists())  inputDir.mkdirs();
            else try (var files = Files.list(inputDir.toPath())) {
                files.filter(Files::isRegularFile).forEach(p -> p.toFile().delete());
            }            
            String timestamp = LocalDateTime.now().format(formatter);
            File destFile = new File(inputDir, file.getOriginalFilename().replace(".zip", "")+"- "+timestamp+".zip");
            file.transferTo(destFile);
            this.idLotCurent = destFile.getName().replace(".zip", "");
            this.fisiereLotCurent = zipService.dezarhiveaza(destFile);
                this.fisiereLotCurent.sort((f1, f2) -> {
                    long n1 = extrageNumarDinNume(f1.getName())*10+determinaTipActDinNume(f1.getName()).getPrioritate();
                    long n2 = extrageNumarDinNume(f2.getName())*10+determinaTipActDinNume(f2.getName()).getPrioritate();
                    return Long.compare(n1, n2);
                });
            return idLotCurent;
        } catch (Exception e) {
            System.out.println("Eroare la salvarea fișierului: " + e.getMessage());
            return null;
        }
    }

    public List<File> getFisiereLotDirector(String idLot){
        if (this.idLotCurent.equals(idLot)) {
            return this.fisiereLotCurent;
        } else {
            File lotDir = new File(config.getInputFolder(), idLot);
            if (lotDir.exists() && lotDir.isDirectory()) {
                File[] fisiere = lotDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
                this.fisiereLotCurent = fisiere != null ? Arrays.asList(fisiere) : new ArrayList<>();
                this.idLotCurent = idLot;
                this.fisiereLotCurent.sort((f1, f2) -> {
                    long n1 = extrageNumarDinNume(f1.getName())*10+determinaTipActDinNume(f1.getName()).getPrioritate();
                    long n2 = extrageNumarDinNume(f2.getName())*10+determinaTipActDinNume(f2.getName()).getPrioritate();
                    return Long.compare(n1, n2);
                });
                return this.fisiereLotCurent;
            }
        }
        return new ArrayList<>();
    }

    private long extrageNumarDinNume(String nume) {
        Pattern p = Pattern.compile("\\d{2}_(\\d+)");
        Matcher m = p.matcher(nume);
        if (m.find()) {
            return Long.parseLong(m.group(1));
        }
        else {
            p = Pattern.compile("Punctual-(\\d+)-");
            m = p.matcher(nume);
            if (m.find()) {
            return Long.parseLong(m.group(1));
            }
        }
        return 0;
    }

    private TipAct determinaTipActDinNume(String nume) {
        if (nume.contains("INCHEIERE")) return TipAct.Incheiere;
        if (nume.contains("inregistrareMentiuni")) return TipAct.CIM;
        if (nume.contains("CertificatConstatator")) return TipAct.Constatator;
        if (nume.contains("CertificatInmatriculare"))  return TipAct.CI;
        if (nume.contains("FI-Punctual-")) return TipAct.ListaVerificare;
        return TipAct.Altele;
    }


    /**
     * Salvează o listă nouă de acte într-un fișier JSON.
     * Adaugă automat timestamp-ul la denumirea fișierului.
     */
    public void salveazaListaNoua(List<Act> acte, String idLot) {
//        String timestamp = LocalDateTime.now().format(formatter);
//        String numeFinal = denumireFisier + " - " + timestamp;
        
        File folder = new File(config.getLotFolder());
        if (!folder.exists()) {
            folder.mkdirs(); // Creăm folderul dacă nu există
        }

        File fisierDestinatie = new File(folder, idLot+".json");

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(fisierDestinatie, acte);
            listLoturi.add(idLot); 
            this.setActiv(idLot); 
            System.out.println("Fișier salvat cu succes: " + fisierDestinatie.getAbsolutePath());
        } catch (IOException e) {
            // Probabil ar trebui să aruncăm o excepție personalizată aici
            System.out.println("Eroare la salvarea listei: "+e.getMessage());
            e.printStackTrace();
        }
    }


    public File salveazaFisierScanat(MultipartFile file){
            try {
            File inputDir = new File(config.getOutputFolder());
            if (!inputDir.exists())  inputDir.mkdirs();
            
            File destFile = new File(inputDir, file.getOriginalFilename());
            System.out.println("Salvam fisierul in "+destFile.getAbsolutePath());
            file.transferTo(destFile);
            return destFile;
            } catch (IOException e) {
                System.out.println("Eroare la salvarea fișierului: " + e.getMessage());
                return null;
            }
    }

    /**
     * Citește conținutul unui fișier JSON specificat și returnează lista de acte.
     */
    public List<Act> citesteLista(String numeLot) {
        String numeFisier = numeLot + ".json";
        File fisier = new File(config.getLotFolder(), numeFisier);
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

    public File getFisierAct(String idLot, String numeFisier) {
        File fisier = new File(config.getInputFolder(), idLot + "/" + numeFisier);
        if (fisier.exists()) {
            return fisier;
        } else {
            System.err.println("Fișierul nu a fost găsit: " + fisier.getAbsolutePath());
            return null;
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