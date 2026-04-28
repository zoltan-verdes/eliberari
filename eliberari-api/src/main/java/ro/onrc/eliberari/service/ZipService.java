package ro.onrc.eliberari.service;

import org.springframework.stereotype.Service;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;

@Service
public class ZipService {

    /**
     * Arhivează o listă de fișiere într-un singur fișier ZIP.
     * 
     * @param fisiere           Sursă: lista de fișiere care trebuie arhivate.
     * @param caleZipDestinatie Calea completă unde se va crea arhiva (ex:
     *                          "output/arhive.zip").
     */
    public void arhiveaza(List<File> fisiere, String caleZipDestinatie) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(caleZipDestinatie);
                ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (File fisier : fisiere) {
                if (!fisier.exists())
                    continue;

                try (FileInputStream fis = new FileInputStream(fisier)) {
                    // Creăm o intrare nouă în ZIP
                    ZipEntry zipEntry = new ZipEntry(fisier.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[4096];
                    int length;
                    while ((length = fis.read(buffer)) >= 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    /**
     * Desface (decomprimă) un fișier ZIP într-un director specificat.
     * 
     * @param caleZip            Sursă: calea către fișierul .zip.
     * @param directorDestinatie Unde să fie extrase fișierele.
     *
     */

    public List<File> dezarhiveaza(File zipFile) throws IOException {
        return dezarhiveaza(zipFile, zipFile.getAbsolutePath().substring(0, zipFile.getAbsolutePath().length() - 4));
    }

    public List<File> dezarhiveaza(File zipFile, String directorDestinatie) throws IOException {
        List<File> fisiere = new ArrayList<>();
        File destDir = new File(directorDestinatie);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                File noulFisier = noulFisier(destDir, zipEntry);

                if (zipEntry.isDirectory()) {
                    if (!noulFisier.isDirectory() && !noulFisier.mkdirs()) {
                        throw new IOException("Eșec la crearea directorului: " + noulFisier);
                    }
                } else {
                    // Creăm directoarele părinte dacă nu există
                    File parent = noulFisier.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new IOException("Eșec la crearea directorului părinte: " + parent);
                    }
                    fisiere.add(noulFisier);
                    // Scriem conținutul fișierului
                    try (FileOutputStream fos = new FileOutputStream(noulFisier)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
        return fisiere;
    }

    /**
     * Metodă de securitate pentru a preveni "Zip Slip Vulnerability".
     * Se asigură că fișierul extras nu iese în afara directorului țintă (ex:
     * ../../etc/passwd).
     */
    private File noulFisier(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Intrare ZIP invalidă (Zip Slip): " + zipEntry.getName());
        }
        return destFile;
    }
}