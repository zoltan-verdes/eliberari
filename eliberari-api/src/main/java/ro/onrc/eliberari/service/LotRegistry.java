package ro.onrc.eliberari.service;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ro.onrc.eliberari.model.Act;
import ro.onrc.eliberari.model.Cerere;
import ro.onrc.eliberari.model.CerereSimpla;
import ro.onrc.eliberari.model.InfoLot;
import ro.onrc.eliberari.model.StivaCereri;
import ro.onrc.eliberari.processor.AppRepository;
import ro.onrc.eliberari.processor.ProcesorDocumente;

@Service
public class LotRegistry {
    AppRepository repository;
    ProcesorDocumente procesor;
    private final Map<String, List<Act>> internalStorage = new ConcurrentHashMap<>();
    private final Map<String, List<CerereSimpla>> viewStorage = new ConcurrentHashMap<>();
    private final Map<String, boolean[]> pageStatuses = new ConcurrentHashMap<>();
    private final Map<String, List<File>> fisiereRezultate = new ConcurrentHashMap<>();
    private final Map<String, InfoLot> infoLot = new ConcurrentHashMap<>();


public LotRegistry(AppRepository repository, ProcesorDocumente procesor) {
        this.repository = repository;
        this.procesor = procesor;
        incarcaLoturileDePeDisc();
    }

    private void incarcaLoturileDePeDisc() {
        List<String> denumireLoturi = repository.getListeDisponibile();
        System.out.println("Se incarca listele de pe disc numar gasite:"+denumireLoturi.size());
        for (String denumire : denumireLoturi) {
            System.out.println("Citim lotul "+denumire+" de pe disc");
            List<Act> lot = repository.citesteLista(denumire);
            System.out.println("Am citit un numar de acte "+lot.size());
            internalStorage.put(denumire, lot);
            StivaCereri stiva = new StivaCereri(lot);
            System.out.println("Avem un numar de cereri"+stiva.getLista().size());
            viewStorage.put(denumire, stiva.getLista());
            infoLot.put(denumire, stiva.getInfo());
        }
        System.out.println("S-au încărcat " + internalStorage.size() + " loturi din arhivă.");
    }


    public void registerNewLot(File lotZip) throws IOException {
        String lotId = lotZip.getName().replace(".zip", "");
        System.out.println("Adaugam lotul "+lotId);
        List<Act> internal = procesor.proceseazaLot(lotZip);
        internalStorage.put(lotId,internal);

        // 2. Din datele brute, extragi doar "esența" pentru UI (Totaluri, Tabel)
        StivaCereri stiva = new StivaCereri(internal);
        viewStorage.put(lotId, stiva.getLista());

        infoLot.put(lotId, stiva.getInfo());
        repository.salveazaListaNoua(internal, lotId);        
    }

    public boolean exists(String lotId){ return internalStorage.containsKey(lotId);     }

    public List<String> getListaLoturi() { return internalStorage.keySet().stream().toList(); }

    public List<CerereSimpla> getLotForUI(String lotId) { return viewStorage.get(lotId); }

    public List<Act> getLotForProcessing(String lotId) { return internalStorage.get(lotId); }

    public void savePageStatuses(String lotId, boolean[] statuses) { pageStatuses.put(lotId, statuses); }

    public boolean[] getPageStatuses(String lotId) { return pageStatuses.get(lotId); }

    public File getFisierScanat(String lotId) { return infoLot.get(lotId).getFisierScanat(); }

    public File setFisierScanat(MultipartFile fisier) { 
        String lotId = fisier.getOriginalFilename().replace(".pdf", "");
        System.out.println("setam fisierul scant pentru lotul: "+lotId);
        File file = repository.salveazaFisierScanat(fisier);
        infoLot.get(lotId).setFisierScanat(file); 
        return file;
    }



    public List<String[]> getListaLoturiExtins(){
        return infoLot.entrySet().stream()
        .map(entry -> new String[] {
        entry.getKey(),                            // Identificatorul (String)
        String.valueOf(entry.getValue().getNrCereri()), 
        String.valueOf(entry.getValue().getNrActe()),
        String.valueOf(entry.getValue().getNrPagini())
    })
    .collect(Collectors.toList());
    }

    public void addFisierRezultat(String lotId, File fisier) {
        List<File> fisiere = fisiereRezultate.get(lotId);
        if (fisiere == null) {
            fisiere = new ArrayList<>();
        }
        fisiere.add(fisier);
        fisiereRezultate.put(lotId, fisiere);
    }


    

}