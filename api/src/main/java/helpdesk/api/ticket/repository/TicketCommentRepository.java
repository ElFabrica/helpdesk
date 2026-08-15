package helpdesk.api.ticket.repository;

import helpdesk.api.ticket.entity.TicketComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
