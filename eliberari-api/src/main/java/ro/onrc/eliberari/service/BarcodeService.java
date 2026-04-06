package ro.onrc.eliberari.service;

import java.awt.image.BufferedImage;

import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.stereotype.Service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

@Service
public class BarcodeService {

    public String citesteCod(BufferedImage imagine) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(imagine);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // Încercăm să decodăm
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            // Nu a fost găsit niciun cod de bare pe această pagină
            return null;
        } catch (Exception e) {
            System.out.println("Eroare la procesarea codului de bare");
            return null;
        }
    }
}