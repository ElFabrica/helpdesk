package helpdesk.api.dashboard;

import helpdesk.api.dashboard.dto.HighPriorityAlertDTO;
import helpdesk.api.dashboard.event.DashboardTicketCreatedEvent;
import helpdesk.api.dashboard.event.DashboardTicketUpdatedEvent;
import helpdesk.api.ticket.dto.TicketSummaryResponseDTO;
import helpdesk.api.ticket.entity.TicketPriority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class DashboardTicketEventListener {

    private final DashboardRealtimeService dashboardRealtimeService;
    private final DashboardService dashboardService;

    public DashboardTicketEventListener(
            DashboardRealtimeService dashboardRealtimeService,
            DashboardService dashboardService
    ) {
        this.dashboardRealtimeService = dashboardRealtimeService;
        this.dashboardService = dashboardService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketCreated(DashboardTicketCreatedEvent event) {
        sendUpdatedIndicators();
        dashboardRealtimeService.send(DashboardRealtimeService.TICKET_CREATED_EVENT, event.ticket());
        sendHighPriorityAlertIfNeeded(event.ticket());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketUpdated(DashboardTicketUpdatedEvent event) {
        sendUpdatedIndicators();
        dashboardRealtimeService.send(DashboardRealtimeService.TICKET_UPDATED_EVENT, event.ticket());
    }

    private void sendUpdatedIndicators() {
        dashboardRealtimeService.send(
                DashboardRealtimeService.INDICATORS_UPDATED_EVENT,
                dashboardService.indicators()
        );
    }

    private void sendHighPriorityAlertIfNeeded(TicketSummaryResponseDTO ticket) {
        if (ticket.priority() != TicketPriority.ALTA) {
            return;
        }

        dashboardRealtimeService.send(
                DashboardRealtimeService.HIGH_PRIORITY_ALERT_EVENT,
                new HighPriorityAlertDTO(ticket.id(), ticket.title(), ticket.priority())
        );
    }
}
