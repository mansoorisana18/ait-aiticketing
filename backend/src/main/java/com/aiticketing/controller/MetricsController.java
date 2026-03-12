package com.aiticketing.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aiticketing.bean.response.AiSummaryMetricsResponseBean;
import com.aiticketing.bean.response.ApiResponseBean;
import com.aiticketing.bean.response.TicketSummaryMetricsResponseBean;
import com.aiticketing.security.AuthUserPrincipal;
import com.aiticketing.service.MetricsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private static final Logger METRICS_CONTROLLER_LOG = LoggerFactory.getLogger(MetricsController.class);

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Operation(summary = "Admin AI summary metrics", description = "Returns grouped TRIAGE and ROUTING metrics for the admin analytics dashboard.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AI summary metrics fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/admin/ai-summary")
    public ResponseEntity<ApiResponseBean<AiSummaryMetricsResponseBean>> getAdminAiSummaryMetrics() {
        METRICS_CONTROLLER_LOG.info("MetricsController :: in getAdminAiSummaryMetrics()");

        AiSummaryMetricsResponseBean resp = metricsService.getAdminAiSummaryMetrics();

        METRICS_CONTROLLER_LOG.info("MetricsController :: exit getAdminAiSummaryMetrics()");
        return ResponseEntity.ok(ApiResponseBean.success(resp));
    }

    @Operation(summary = "Admin ticket summary metrics", description = "Returns operational summary ticket counts for the admin landing tickets page.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin ticket summary metrics fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/admin/ticket-summary")
    public ResponseEntity<ApiResponseBean<TicketSummaryMetricsResponseBean>> getAdminTicketSummaryMetrics() {
        METRICS_CONTROLLER_LOG.info("MetricsController :: in getAdminTicketSummaryMetrics()");

        TicketSummaryMetricsResponseBean resp = metricsService.getAdminTicketSummaryMetrics();

        METRICS_CONTROLLER_LOG.info("MetricsController :: exit getAdminTicketSummaryMetrics()");
        return ResponseEntity.ok(ApiResponseBean.success(resp));
    }

    @Operation(summary = "Agent ticket summary metrics", description = "Returns operational summary ticket counts for the logged-in agent's assigned tickets.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agent ticket summary metrics fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/agent/ticket-summary")
    public ResponseEntity<ApiResponseBean<TicketSummaryMetricsResponseBean>> getAgentTicketSummaryMetrics(
            @AuthenticationPrincipal AuthUserPrincipal principal) {
        METRICS_CONTROLLER_LOG.info("MetricsController :: in getAgentTicketSummaryMetrics()");

        Long agentUserId = principal.getUserId();
        TicketSummaryMetricsResponseBean resp = metricsService.getAgentTicketSummaryMetrics(agentUserId);

        METRICS_CONTROLLER_LOG.info("MetricsController :: exit getAgentTicketSummaryMetrics()");
        return ResponseEntity.ok(ApiResponseBean.success(resp));
    }
}