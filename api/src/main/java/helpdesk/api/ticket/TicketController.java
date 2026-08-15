package helpdesk.api.ticket;

import helpdesk.api.ticket.dto.CreateTicketRequestDTO;
import helpdesk.api.ticket.dto.TicketResponseDTO;
import helpdesk.api.ticket.dto.TicketSummaryResponseDTO;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;
import helpdesk.api.ticket.service.TicketService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponseDTO create(@Valid @RequestBody CreateTicketRequestDTO request) {
        return ticketService.create(request);
    }

    @GetMapping
    public List<TicketSummaryResponseDTO> list(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketCategory category
    ) {
        return ticketService.list(status, priority, category);
    }
}
