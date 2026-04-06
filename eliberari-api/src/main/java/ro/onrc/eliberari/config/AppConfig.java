package ro.onrc.eliberari.config;



import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.ocr")
public class AppConfig {
    
    private String inputFolder;
    private String outputFolder;
    private String tessdataPath;
    
    private String authServerUrl;
    private String realm;
    private String clientId;
    private String username;
    private String password;
  


    
    // Getters și Setters (Sunt obligatorii pentru Spring ca să poată injecta valorile)
    public String getInputFolder() { return inputFolder; }
    public void setInputFolder(String inputFolder) { this.inputFolder = inputFolder; }

    public String getOutputFolder() { return outputFolder; }
    public void setOutputFolder(String outputFolder) { this.outputFolder = outputFolder; }

    public String getTessdataPath() { return tessdataPath; }
    public void setTessdataPath(String tessdataPath) { this.tessdataPath = tessdataPath; }


    public String getAuthServerUrl() {  return authServerUrl; }
    public void setAuthServerUrl(String authServerUrl) {  this.authServerUrl = authServerUrl; }

    public String getRealm() { return realm; }
    public void setRealm(String realm) {  this.realm = realm; }

    public String getClientId() {  return clientId; }
    public void setClientId(String clientId) {  this.clientId = clientId;  }
    

    public String getUsername() { return username;  }
    public void setUsername(String username) {  this.username = username;}

    public String getPassword() { return password;}
    public void setPassword(String password) { this.password = password; }



}

