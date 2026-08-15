package helpdesk.api.dashboard;

import helpdesk.api.dashboard.dto.DashboardIndicatorsResponseDTO;
import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;
import helpdesk.api.ticket.repository.TicketRepository;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;

    public DashboardService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public DashboardIndicatorsResponseDTO indicators() {
        return new DashboardIndicatorsResponseDTO(
                ticketRepository.count(),
                countByStatus(),
                countByPriority()
        );
    }

    private Map<TicketStatus, Long> countByStatus() {
        EnumMap<TicketStatus, Long> counts = zeroedEnumMap(TicketStatus.class);
        ticketRepository.countByStatusGrouped()
                .forEach(row -> counts.put(row.getStatus(), row.getTotal()));

        return counts;
    }

    private Map<TicketPriority, Long> countByPriority() {
        EnumMap<TicketPriority, Long> counts = zeroedEnumMap(TicketPriority.class);
        ticketRepository.countByPriorityGrouped()
                .forEach(row -> counts.put(row.getPriority(), row.getTotal()));

        return counts;
    }

    private <E extends Enum<E>> EnumMap<E, Long> zeroedEnumMap(Class<E> enumType) {
        EnumMap<E, Long> counts = new EnumMap<>(enumType);
        Arrays.stream(enumType.getEnumConstants())
                .forEach(value -> counts.put(value, 0L));

        return counts;
    }
}
