package ro.onrc.eliberari.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Optional;

public class LotCereri {
    // TreeMap păstrează cererile sortate numeric după cheie (numar)
    // Căutarea și inserarea devin O(log n) în loc de O(n)
    private final Map<Long, Cerere> cereriMap = new TreeMap<>();

    /**
     * Adaugă o cerere dacă nu există deja una cu același număr.
     */
    public void adauga(Cerere c) {
        cereriMap.putIfAbsent(c.getNumar(), c);
    }

    /**
     * Verifică dacă o cerere cu acest număr există deja.
     * Folosește indexul map-ului (viteză maximă).
     */
    public boolean existaCerere(long numar) {
        return cereriMap.containsKey(numar);
    }

    /**
     * Returnează o cerere existentă după număr.
     */
    public Optional<Cerere> getCerere(long numar) {
        return Optional.ofNullable(cereriMap.get(numar));
    }

    public List<Cerere> getToate() {
        return new ArrayList<>(cereriMap.values());
    }

    public boolean isEmpty() {
        return cereriMap.isEmpty();
    }
}