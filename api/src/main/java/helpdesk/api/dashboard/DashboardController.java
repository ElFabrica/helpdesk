package helpdesk.api.dashboard;

import helpdesk.api.dashboard.dto.DashboardIndicatorsResponseDTO;
import helpdesk.api.error.ApiErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Indicadores e eventos em tempo real para administradores.")
@SecurityRequirement(name = "bearer-jwt")
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardRealtimeService dashboardRealtimeService;

    public DashboardController(
            DashboardService dashboardService,
            DashboardRealtimeService dashboardRealtimeService
    ) {
        this.dashboardService = dashboardService;
        this.dashboardRealtimeService = dashboardRealtimeService;
    }

    @GetMapping("/indicators")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Consultar indicadores", description = "Retorna totais de chamados por status e prioridade. Requer ADMIN.")
    @ApiResponse(responseCode = "200", description = "Indicadores retornados")
    @ApiResponse(responseCode = "401", description = "Autenticacao necessaria", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Perfil sem permissao", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    public DashboardIndicatorsResponseDTO indicators() {
        return dashboardService.indicators();
    }

    @GetMapping(path = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Conectar eventos do dashboard", description = "Abre stream SSE com indicadores atualizados e alertas de chamados de prioridade ALTA. Requer ADMIN.")
    @ApiResponse(responseCode = "200", description = "Stream SSE conectado")
    @ApiResponse(responseCode = "401", description = "Autenticacao necessaria", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Perfil sem permissao", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    public SseEmitter events() {
        return dashboardRealtimeService.connect();
    }
}
