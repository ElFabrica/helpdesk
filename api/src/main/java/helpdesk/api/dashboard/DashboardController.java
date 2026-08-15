package helpdesk.api.dashboard;

import helpdesk.api.dashboard.dto.DashboardIndicatorsResponseDTO;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/dashboard")
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
    public DashboardIndicatorsResponseDTO indicators() {
        return dashboardService.indicators();
    }

    @GetMapping(path = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public SseEmitter events() {
        return dashboardRealtimeService.connect();
    }
}
