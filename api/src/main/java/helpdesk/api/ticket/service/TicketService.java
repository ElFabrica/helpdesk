package helpdesk.api.ticket.service;

import helpdesk.api.auth.AuthenticatedUser;
import helpdesk.api.auth.service.AuthenticatedUserService;
import helpdesk.api.ticket.dto.CreateTicketRequestDTO;
import helpdesk.api.ticket.dto.TicketResponseDTO;
import helpdesk.api.ticket.dto.TicketSummaryResponseDTO;
import helpdesk.api.ticket.dto.UpdateTicketRequestDTO;
import helpdesk.api.ticket.entity.Ticket;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;
import helpdesk.api.ticket.repository.TicketRepository;
import helpdesk.api.user.entity.User;
import helpdesk.api.user.repository.UserRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final TicketAuthorizationService ticketAuthorizationService;
    private final UserRepository userRepository;
    private final TicketClassifier ticketClassifier;

    public TicketService(
            TicketRepository ticketRepository,
            AuthenticatedUserService authenticatedUserService,
            TicketAuthorizationService ticketAuthorizationService,
            UserRepository userRepository,
            TicketClassifier ticketClassifier
    ) {
        this.ticketRepository = ticketRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.ticketAuthorizationService = ticketAuthorizationService;
        this.userRepository = userRepository;
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

    @Transactional(readOnly = true)
    public TicketResponseDTO detail(Long id) {
        Ticket ticket = findTicket(id);
        ticketAuthorizationService.assertCanAccess(ticket);

        return TicketResponseDTO.from(ticket);
    }

    @Transactional
    public TicketResponseDTO update(Long id, UpdateTicketRequestDTO request) {
        Ticket ticket = findTicket(id);
        ticketAuthorizationService.assertCanUpdate(ticket);

        if (request.hasAdminFields()) {
            ticketAuthorizationService.assertCanUpdateAdminFields();
        }

        applyCommonUpdates(ticket, request);

        if (request.hasAdminFields()) {
            applyAdminUpdates(ticket, request);
        }

        return TicketResponseDTO.from(ticketRepository.saveAndFlush(ticket));
    }

    @Transactional
    public void cancel(Long id) {
        Ticket ticket = findTicket(id);
        ticketAuthorizationService.assertCanCancel(ticket);

        ticket.setStatus(TicketStatus.FECHADO);
        ticketRepository.saveAndFlush(ticket);
    }

    private Ticket findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chamado nao encontrado"));
    }

    private void applyCommonUpdates(Ticket ticket, UpdateTicketRequestDTO request) {
        if (request.title() != null) {
            ticket.setTitle(requiredText(request.title(), "title"));
        }

        if (request.description() != null) {
            ticket.setDescription(requiredText(request.description(), "description"));
        }

        if (request.status() != null) {
            ticket.setStatus(request.status());
        }
    }

    private void applyAdminUpdates(Ticket ticket, UpdateTicketRequestDTO request) {
        if (request.priority() != null) {
            ticket.setPriority(request.priority());
        }

        if (request.category() != null) {
            ticket.setCategory(request.category());
        }

        if (request.responsibleId() != null) {
            ticket.setResponsible(findUser(request.responsibleId()));
        }
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario responsavel nao encontrado"));
    }

    private String requiredText(String value, String fieldName) {
        if (value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " nao pode ficar em branco");
        }

        return value;
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
