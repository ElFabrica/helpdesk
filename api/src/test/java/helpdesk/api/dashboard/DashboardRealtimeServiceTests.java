package helpdesk.api.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class DashboardRealtimeServiceTests {

    @Test
    void connectionCanBeRemoved() {
        DashboardRealtimeService service = new DashboardRealtimeService();
        SseEmitter emitter = service.connect();

        service.disconnect(emitter);

        assertThat(service.activeConnections()).isZero();
    }

    @Test
    void failedConnectionDoesNotBreakBroadcast() {
        DashboardRealtimeService service = new DashboardRealtimeService();
        service.register(new FailingSseEmitter());

        assertThatCode(() -> service.send("test-event", "payload"))
                .doesNotThrowAnyException();
        assertThat(service.activeConnections()).isZero();
    }

    private static class FailingSseEmitter extends SseEmitter {

        @Override
        public void send(SseEventBuilder builder) throws IOException {
            throw new IOException("closed connection");
        }
    }
}
