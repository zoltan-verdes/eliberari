package ro.onrc.eliberari.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseService {
    private SseEmitter currentEmitter;

    public SseEmitter createEmitter() {
        this.currentEmitter = new SseEmitter(Long.MAX_VALUE);
        this.currentEmitter.onCompletion(() -> this.currentEmitter = null);
        this.currentEmitter.onTimeout(() -> this.currentEmitter = null);
        return this.currentEmitter;
    }

    public void sendLog(String mesaj) {
        if (currentEmitter != null) {
            try {
                currentEmitter.send(SseEmitter.event().data(mesaj));
            } catch (IOException e) {
                currentEmitter = null;
            }
        }
    }

    public void sendData(String eventName, Object data) {
        if (currentEmitter != null) {
            try {
                currentEmitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                currentEmitter = null;
            }
        }
    }
}