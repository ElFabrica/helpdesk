package helpdesk.api.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import helpdesk.api.dashboard.dto.DashboardIndicatorsResponseDTO;
import helpdesk.api.dashboard.dto.HighPriorityAlertDTO;
import helpdesk.api.dashboard.event.DashboardTicketCreatedEvent;
import helpdesk.api.dashboard.event.DashboardTicketUpdatedEvent;
import helpdesk.api.ticket.dto.TicketSummaryResponseDTO;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DashboardTicketEventListenerTests {

    @Test
    void createdHighPriorityTicketSendsIndicatorsTicketAndAlert() {
        DashboardIndicatorsResponseDTO indicators = indicators();
        TicketSummaryResponseDTO ticket = ticket(TicketPriority.ALTA);
        RecordingDashboardRealtimeService dashboardRealtimeService = new RecordingDashboardRealtimeService();
        DashboardTicketEventListener listener = new DashboardTicketEventListener(
                dashboardRealtimeService,
                new FixedDashboardService(indicators)
        );

        listener.onTicketCreated(new DashboardTicketCreatedEvent(ticket));

        assertThat(dashboardRealtimeService.sentEvents()).containsExactly(
                new SentEvent(DashboardRealtimeService.INDICATORS_UPDATED_EVENT, indicators),
                new SentEvent(DashboardRealtimeService.TICKET_CREATED_EVENT, ticket),
                new SentEvent(
                        DashboardRealtimeService.HIGH_PRIORITY_ALERT_EVENT,
                        new HighPriorityAlertDTO(ticket.id(), ticket.title(), ticket.priority())
                )
        );
    }

    @Test
    void createdNonHighPriorityTicketDoesNotSendAlert() {
        DashboardIndicatorsResponseDTO indicators = indicators();
        TicketSummaryResponseDTO ticket = ticket(TicketPriority.MEDIA);
        RecordingDashboardRealtimeService dashboardRealtimeService = new RecordingDashboardRealtimeService();
        DashboardTicketEventListener listener = new DashboardTicketEventListener(
                dashboardRealtimeService,
                new FixedDashboardService(indicators)
        );

        listener.onTicketCreated(new DashboardTicketCreatedEvent(ticket));

        assertThat(dashboardRealtimeService.sentEvents()).containsExactly(
                new SentEvent(DashboardRealtimeService.INDICATORS_UPDATED_EVENT, indicators),
                new SentEvent(DashboardRealtimeService.TICKET_CREATED_EVENT, ticket)
        );
    }

    @Test
    void updatedTicketSendsIndicatorsAndTicketUpdate() {
        DashboardIndicatorsResponseDTO indicators = indicators();
        TicketSummaryResponseDTO ticket = ticket(TicketPriority.ALTA);
        RecordingDashboardRealtimeService dashboardRealtimeService = new RecordingDashboardRealtimeService();
        DashboardTicketEventListener listener = new DashboardTicketEventListener(
                dashboardRealtimeService,
                new FixedDashboardService(indicators)
        );

        listener.onTicketUpdated(new DashboardTicketUpdatedEvent(ticket));

        assertThat(dashboardRealtimeService.sentEvents()).containsExactly(
                new SentEvent(DashboardRealtimeService.INDICATORS_UPDATED_EVENT, indicators),
                new SentEvent(DashboardRealtimeService.TICKET_UPDATED_EVENT, ticket)
        );
    }

    private DashboardIndicatorsResponseDTO indicators() {
        return new DashboardIndicatorsResponseDTO(
                1,
                Map.of(TicketStatus.ABERTO, 1L),
                Map.of(TicketPriority.ALTA, 1L)
        );
    }

    private TicketSummaryResponseDTO ticket(TicketPriority priority) {
        return new TicketSummaryResponseDTO(
                42L,
                "Sistema financeiro indisponivel",
                TicketCategory.SOFTWARE,
                priority,
                TicketStatus.ABERTO,
                10L,
                "Maria Solicitante",
                null,
                Instant.EPOCH
        );
    }

    private record SentEvent(String name, Object payload) {
    }

    private static class RecordingDashboardRealtimeService extends DashboardRealtimeService {

        private final List<SentEvent> sentEvents = new ArrayList<>();

        @Override
        public void send(String eventName, Object payload) {
            sentEvents.add(new SentEvent(eventName, payload));
        }

        List<SentEvent> sentEvents() {
            return sentEvents;
        }
    }

    private static class FixedDashboardService extends DashboardService {

        private final DashboardIndicatorsResponseDTO indicators;

        FixedDashboardService(DashboardIndicatorsResponseDTO indicators) {
            super(null);
            this.indicators = indicators;
        }

        @Override
        public DashboardIndicatorsResponseDTO indicators() {
            return indicators;
        }
    }
}
