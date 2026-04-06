package ro.onrc.eliberari;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EliberariApp {

    public static void main(String[] args) {
        // Acum doar pornim motorul Spring, nu mai executăm nimic automat.
        SpringApplication.run(EliberariApp.class, args);
    }
}