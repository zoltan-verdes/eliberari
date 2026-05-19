package ro.onrc.eliberari.config;



import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.ocr")
public class AppConfig {
    
    private String inputFolder;
    private String outputFolder;
    private String lotFolder;
    private String foxitPath;
    private String tessdataPath;
    
    
    // Getters și Setters (Sunt obligatorii pentru Spring ca să poată injecta valorile)
    public String getInputFolder() { return inputFolder; }
    public void setInputFolder(String inputFolder) { this.inputFolder = inputFolder; }

    public String getOutputFolder() { return outputFolder; }
    public void setOutputFolder(String outputFolder) { this.outputFolder = outputFolder; }

    public String getLotFolder() { return lotFolder; }
    public void setLotFolder(String lotFolder) { this.lotFolder = lotFolder; }

    public String getFoxitPath(){ return foxitPath;}
    public void setFoxitPath(String foxitPath){ this.foxitPath = foxitPath;}

    public String getTessdataPath() { return tessdataPath; }
    public void setTessdataPath(String tessdataPath) { this.tessdataPath = tessdataPath; }

}

