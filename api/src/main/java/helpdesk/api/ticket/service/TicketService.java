package helpdesk.api.ticket.service;

import helpdesk.api.auth.AuthenticatedUser;
import helpdesk.api.auth.service.AuthenticatedUserService;
import helpdesk.api.ticket.dto.CreateTicketRequestDTO;
import helpdesk.api.ticket.dto.TicketResponseDTO;
import helpdesk.api.ticket.dto.TicketSummaryResponseDTO;
import helpdesk.api.ticket.entity.Ticket;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;
import helpdesk.api.ticket.repository.TicketRepository;
import helpdesk.api.user.entity.User;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final TicketClassifier ticketClassifier;

    public TicketService(
            TicketRepository ticketRepository,
            AuthenticatedUserService authenticatedUserService,
            TicketClassifier ticketClassifier
    ) {
        this.ticketRepository = ticketRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.ticketClassifier = ticketClassifier;
    }

    @Transactional
    public TicketResponseDTO create(CreateTicketRequestDTO request) {
        User requester = authenticatedUserService.getAuthenticatedUserEntity();
        TicketClassification classification = ticketClassifier.classify(request.title(), request.description());

        Ticket ticket = new Ticket(
                request.title(),
                request.description(),
                classification.category(),
                classification.priority(),
                classification.origin(),
                requester,
                null
        );

        return TicketResponseDTO.from(ticketRepository.saveAndFlush(ticket));
    }

    @Transactional(readOnly = true)
    public List<TicketSummaryResponseDTO> list(
            TicketStatus status,
            TicketPriority priority,
            TicketCategory category
    ) {
        AuthenticatedUser authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        Specification<Ticket> specification = Specification.allOf(
                requesterScope(authenticatedUser),
                hasStatus(status),
                hasPriority(priority),
                hasCategory(category)
        );

        return ticketRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(TicketSummaryResponseDTO::from)
                .toList();
    }

    private Specification<Ticket> requesterScope(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.isAdmin()) {
            return alwaysTrue();
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("requester").get("id"), authenticatedUser.id());
    }

    private Specification<Ticket> hasStatus(TicketStatus status) {
        if (status == null) {
            return alwaysTrue();
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    private Specification<Ticket> hasPriority(TicketPriority priority) {
        if (priority == null) {
            return alwaysTrue();
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("priority"), priority);
    }

    private Specification<Ticket> hasCategory(TicketCategory category) {
        if (category == null) {
            return alwaysTrue();
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category"), category);
    }

    private Specification<Ticket> alwaysTrue() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }
}
