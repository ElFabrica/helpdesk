package helpdesk.api.dashboard;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class DashboardRealtimeService {

    static final String INDICATORS_UPDATED_EVENT = "indicators-updated";
    static final String TICKET_CREATED_EVENT = "ticket-created";
    static final String TICKET_UPDATED_EVENT = "ticket-updated";
    static final String HIGH_PRIORITY_ALERT_EVENT = "high-priority-alert";

    private static final long EMITTER_TIMEOUT = Duration.ofMinutes(30).toMillis();

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter connect() {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        register(emitter);

        return emitter;
    }

    void register(SseEmitter emitter) {
        emitters.add(emitter);

        emitter.onCompletion(() -> disconnect(emitter));
        emitter.onTimeout(() -> {
            disconnect(emitter);
            emitter.complete();
        });
        emitter.onError(error -> disconnect(emitter));

        sendInitialConnectionComment(emitter);
    }

    public void send(String eventName, Object payload) {
        emitters.forEach(emitter -> send(emitter, eventName, payload));
    }

    int activeConnections() {
        return emitters.size();
    }

    void disconnect(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    private void sendInitialConnectionComment(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException | IllegalStateException exception) {
            disconnect(emitter);
            emitter.completeWithError(exception);
        }
    }

    private void send(SseEmitter emitter, String eventName, Object payload) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(payload));
        } catch (IOException | IllegalStateException exception) {
            disconnect(emitter);
            emitter.completeWithError(exception);
        }
    }
}
