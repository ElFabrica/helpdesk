package helpdesk.api.dashboard.dto;

import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;
import java.util.Map;

public record DashboardIndicatorsResponseDTO(
        long total,
        Map<TicketStatus, Long> byStatus,
        Map<TicketPriority, Long> byPriority
) {
}
