package helpdesk.api.ticket;

import helpdesk.api.ticket.dto.CreateTicketCommentRequestDTO;
import helpdesk.api.ticket.dto.CreateTicketRequestDTO;
import helpdesk.api.ticket.dto.TicketCommentResponseDTO;
import helpdesk.api.ticket.dto.TicketResponseDTO;
import helpdesk.api.ticket.dto.TicketSummaryResponseDTO;
import helpdesk.api.ticket.dto.UpdateTicketClassificationRequestDTO;
import helpdesk.api.ticket.dto.UpdateTicketRequestDTO;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;
import helpdesk.api.ticket.service.TicketService;
import helpdesk.api.error.ApiErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Chamados", description = "Criacao, consulta, atualizacao e historico de chamados.")
@SecurityRequirement(name = "bearer-jwt")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar chamado", description = "Cria um chamado para o usuario autenticado e classifica categoria/prioridade automaticamente.")
    @ApiResponse(responseCode = "201", description = "Chamado criado")
    @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Autenticacao necessaria", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    public TicketResponseDTO create(@Valid @RequestBody CreateTicketRequestDTO request) {
        return ticketService.create(request);
    }

    @GetMapping
    @Operation(summary = "Listar chamados", description = "Lista chamados com filtros opcionais. ADMIN ve todos; SOLICITANTE ve apenas os proprios.")
    @ApiResponse(responseCode = "200", description = "Chamados encontrados")
    @ApiResponse(responseCode = "400", description = "Filtro invalido", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Autenticacao necessaria", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    public List<TicketSummaryResponseDTO> list(
            @Parameter(description = "Filtra pelo status do chamado")
            @RequestParam(required = false) TicketStatus status,
            @Parameter(description = "Filtra pela prioridade do chamado")
            @RequestParam(required = false) TicketPriority priority,
            @Parameter(description = "Filtra pela categoria do chamado")
            @RequestParam(required = false) TicketCategory category
    ) {
        return ticketService.list(status, priority, category);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar chamado", description = "Retorna os dados completos de um chamado acessivel pelo usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Chamado encontrado")
    @ApiResponse(responseCode = "401", description = "Autenticacao necessaria", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Usuario sem acesso ao chamado", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Chamado nao encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    public TicketResponseDTO detail(@Parameter(description = "ID do chamado") @PathVariable Long id) {
        return ticketService.detail(id);
    }

    @PostMapping("/{ticketId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adicionar comentario", description = "Adiciona uma interacao ao historico de um chamado acessivel pelo usuario autenticado.")
    @ApiResponse(responseCode = "201", description = "Comentario criado")
    @ApiResponse(responseCode = "400", description = "Comentario invalido", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Autenticacao necessaria", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Usuario sem acesso ao chamado", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Chamado nao encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    public TicketCommentResponseDTO addComment(
            @Parameter(description = "ID do chamado") @PathVariable Long ticketId,
            @Valid @RequestBody CreateTicketCommentRequestDTO request
    ) {
        return ticketService.addComment(ticketId, request);
    }

    @GetMapping("/{ticketId}/comments")
    @Operation(summary = "Listar comentarios", description = "Lista comentarios e eventos de historico do chamado em ordem cronologica.")
    @ApiResponse(responseCode = "200", description = "Historico encontrado")
    @ApiResponse(responseCode = "401", description = "Autenticacao necessaria", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Usuario sem acesso ao chamado", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Chamado nao encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    public List<TicketCommentResponseDTO> listComments(@Parameter(description = "ID do chamado") @PathVariable Long ticketId) {
        return ticketService.listComments(ticketId);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar chamado", description = "Atualiza dados do chamado. Campos administrativos exigem perfil ADMIN.")
    @ApiResponse(responseCode = "200", description = "Chamado atualizado")
    @ApiResponse(responseCode = "400", description = "Dados invalidos ou transicao de status invalida", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Autenticacao necessaria", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Usuario sem permissao", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Chamado nao encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    public TicketResponseDTO update(
            @Parameter(description = "ID do chamado") @PathVariable Long id,
            @Valid @RequestBody UpdateTicketRequestDTO request
    ) {
        return ticketService.update(id, request);
    }

    @PatchMapping("/{id}/classification")
    @Operation(summary = "Corrigir classificacao", description = "Permite que ADMIN corrija categoria e prioridade sugeridas automaticamente.")
    @ApiResponse(responseCode = "200", description = "Classificacao corrigida")
    @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Autenticacao necessaria", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Usuario sem permissao", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Chamado nao encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    public TicketResponseDTO updateClassification(
            @Parameter(description = "ID do chamado") @PathVariable Long id,
            @Valid @RequestBody UpdateTicketClassificationRequestDTO request
    ) {
        return ticketService.updateClassification(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancelar chamado", description = "Cancela o chamado alterando seu status para FECHADO.")
    @ApiResponse(responseCode = "204", description = "Chamado cancelado")
    @ApiResponse(responseCode = "400", description = "Transicao de status invalida", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Autenticacao necessaria", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Usuario sem acesso ao chamado", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Chamado nao encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    public void cancel(@Parameter(description = "ID do chamado") @PathVariable Long id) {
        ticketService.cancel(id);
    }
}
