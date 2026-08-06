package com.finsight.datahub.controller;

import com.finsight.datahub.dto.response.ApiResponse;
import com.finsight.datahub.dto.response.DashboardOverviewDto;
import com.finsight.datahub.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Dashboard", description = "Executive Dashboard Backend Operations")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard/overview")
    @Operation(summary = "Get executive dashboard overview", description = "Aggregates total stocks, companies, market average price, top gainers, top losers, highest volume, sector distribution, and recent upload activity.")
    public ResponseEntity<ApiResponse<DashboardOverviewDto>> getDashboardOverview() {
        DashboardOverviewDto overview = dashboardService.getDashboardOverview();
        return ResponseEntity.ok(ApiResponse.success("Dashboard overview retrieved successfully", overview));
    }

    @GetMapping("/analytics/summary")
    @Operation(summary = "Get analytics market summary", description = "High-level summary metric overview of the platform.")
    public ResponseEntity<ApiResponse<DashboardOverviewDto>> getAnalyticsSummary() {
        DashboardOverviewDto overview = dashboardService.getDashboardOverview();
        return ResponseEntity.ok(ApiResponse.success("Analytics summary retrieved successfully", overview));
    }
}
