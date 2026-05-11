package ro.onrc.eliberari.model;

import java.util.List;

public class ScanatDTO {
    List<String> log;
    boolean[] paginiIgnorate;

public ScanatDTO( boolean[] paginiIgnorate, List<String> log) {
    this.log = log;
    this.paginiIgnorate = paginiIgnorate;

}

public List<String> getLog() {
    return log;
}

public boolean[] getPaginiIgnorate() {
    return paginiIgnorate;
}
    
}
