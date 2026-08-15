package helpdesk.api.ticket.repository;

import helpdesk.api.ticket.entity.Ticket;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByPriority(TicketPriority priority);
    List<Ticket> findByCategory(TicketCategory category);

    @Query("SELECT t.status AS status, COUNT(t) AS total FROM Ticket t GROUP BY t.status")
    List<TicketStatusCount> countByStatusGrouped();

    @Query("SELECT t.priority AS priority, COUNT(t) AS total FROM Ticket t GROUP BY t.priority")
    List<TicketPriorityCount> countByPriorityGrouped();

    interface TicketStatusCount {
        TicketStatus getStatus();
        long getTotal();
    }

    interface TicketPriorityCount {
        TicketPriority getPriority();
        long getTotal();
    }
}
