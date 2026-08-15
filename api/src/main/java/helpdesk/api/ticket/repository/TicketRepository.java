package helpdesk.api.ticket.repository;

import helpdesk.api.ticket.entity.Ticket;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByPriority(TicketPriority priority);
    List<Ticket> findByCategory(TicketCategory category);
}
