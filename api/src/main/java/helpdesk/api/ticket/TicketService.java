package helpdesk.api.ticket;

import helpdesk.api.auth.AuthenticatedUserService;
import helpdesk.api.user.User;
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
}
