package ro.onrc.eliberari;

public interface LogListener {
    void onLog(String mesaj);
}



// cod vechi din ProcesareController.java

/*    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter streamProcesare() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Conexiune de lungă durată

        // Rulăm procesarea pe un fir de execuție separat pentru a nu bloca serverul
        Thread.ofVirtual().start(() -> {
            try {

                LogListener sseListener = (mesaj) -> {
                    try {
                        System.out.println(mesaj);
                        emitter.send(SseEmitter.event().data(mesaj));
                    } catch (IOException e) { // Conexiune închisă de client
                    }
                };

                // Exemplu de integrare cu logica ta existentă
                File inputDir = new File(config.getInputFolder());
                File[] fisiere = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

                if (fisiere != null) {
                    for (File f : fisiere) {
                        sseListener.onLog("IN streamProcesare(): Se procesează fișierul: " + f.getName());
                        procesor.recunoastereActeScanate(f, sseListener);
                        sseListener.onLog("Fișier finalizat: " + f.getName());
                    }
                }

                sseListener.onLog("--- Toate fișierele au fost procesate ---");

                List<CerereDTO> cereriDTO = procesor.getLotCereri().getToate().stream().map(CerereDTO::new).toList();
                emitter.send(SseEmitter.event().name("tabel").data(cereriDTO));


                emitter.complete(); // Închidem fluxul
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }
 */