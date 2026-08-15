package helpdesk.api.ticket.service;

import helpdesk.api.auth.AuthenticatedUser;
import helpdesk.api.auth.service.AuthenticatedUserService;
import helpdesk.api.ticket.entity.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TicketAuthorizationService {

    private final AuthenticatedUserService authenticatedUserService;

    public TicketAuthorizationService(AuthenticatedUserService authenticatedUserService) {
        this.authenticatedUserService = authenticatedUserService;
    }

    public AuthenticatedUser currentUser() {
        return authenticatedUserService.getAuthenticatedUser();
    }

    public boolean canListAllTickets() {
        return currentUser().isAdmin();
    }

    public void assertCanListAllTickets() {
        if (!canListAllTickets()) {
            throw forbidden();
        }
    }

    public boolean canAccess(Ticket ticket) {
        AuthenticatedUser user = currentUser();

        return user.isAdmin() || user.owns(ticket.getRequester().getId());
    }

    public void assertCanAccess(Ticket ticket) {
        if (!canAccess(ticket)) {
            throw forbidden();
        }
    }

    public void assertCanUpdate(Ticket ticket) {
        assertCanAccess(ticket);
    }

    public void assertCanUpdateAdminFields() {
        assertAdmin();
    }

    public void assertCanCancel(Ticket ticket) {
        assertCanAccess(ticket);
    }

    public void assertCanReassignResponsible() {
        assertAdmin();
    }

    public void assertCanCorrectClassification() {
        assertAdmin();
    }

    private void assertAdmin() {
        if (!currentUser().isAdmin()) {
            throw forbidden();
        }
    }

    private ResponseStatusException forbidden() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissao para esta operacao");
    }
}
