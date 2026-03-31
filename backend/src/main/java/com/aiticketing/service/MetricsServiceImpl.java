package com.aiticketing.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.aiticketing.bean.response.AiSummaryMetricsResponseBean;
import com.aiticketing.bean.response.DuplicateMetricsResponseBean;
import com.aiticketing.bean.response.RoutingMetricsResponseBean;
import com.aiticketing.bean.response.TicketSummaryMetricsResponseBean;
import com.aiticketing.bean.response.TriageMetricsResponseBean;

@Service
public class MetricsServiceImpl implements MetricsService {

    private static final Logger METRICS_SERVICE_LOG = LoggerFactory.getLogger(MetricsServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public MetricsServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AiSummaryMetricsResponseBean getAdminAiSummaryMetrics() {
        METRICS_SERVICE_LOG.info("MetricsServiceImpl :: in getAdminAiSummaryMetrics()");

        AiSummaryMetricsResponseBean resp = new AiSummaryMetricsResponseBean();
        resp.triage = buildTriageMetrics();
        resp.routing = buildRoutingMetrics();
        resp.duplicate = buildDuplicateMetrics();

        METRICS_SERVICE_LOG.info("MetricsServiceImpl :: exit getAdminAiSummaryMetrics()");
        return resp;
    }

    @Override
    public TicketSummaryMetricsResponseBean getAdminTicketSummaryMetrics() {
        METRICS_SERVICE_LOG.info("MetricsServiceImpl :: in getAdminTicketSummaryMetrics()");
        TicketSummaryMetricsResponseBean resp = buildAdminTicketSummary();
        METRICS_SERVICE_LOG.info("MetricsServiceImpl :: exit getAdminTicketSummaryMetrics()");
        return resp;
    }

    @Override
    public TicketSummaryMetricsResponseBean getAgentTicketSummaryMetrics(Long agentUserId) {
        METRICS_SERVICE_LOG.info("MetricsServiceImpl :: in getAgentTicketSummaryMetrics() :: agentUserId={}", agentUserId);
        TicketSummaryMetricsResponseBean resp = buildAgentTicketSummary(agentUserId);
        METRICS_SERVICE_LOG.info("MetricsServiceImpl :: exit getAgentTicketSummaryMetrics()");
        return resp;
    }

    private TriageMetricsResponseBean buildTriageMetrics() {
        TriageMetricsResponseBean triage = new TriageMetricsResponseBean();

        Map<String, Object> row = queryForSingleRow("""
                SELECT
                  COUNT(*) AS total_tickets_created,
                  COUNT(ticket_ai_triaged_at) AS triage_completed_count,
                  COALESCE(AVG(EXTRACT(EPOCH FROM (ticket_ai_triaged_at - ticket_current_triage_started_at))), 0) AS avg_triage_time_seconds,
                  COALESCE(AVG(ticket_ai_confidence), 0) AS avg_ai_confidence,
                  COUNT(*) FILTER (
                    WHERE ticket_status = 'VAGUE'
                      AND ticket_ai_triaged_at IS NOT NULL
                  ) AS vague_count
                FROM tickets
                """);

        long totalTicketsCreated = getLong(row, "total_tickets_created");
        long triageCompletedCount = getLong(row, "triage_completed_count");
        double avgTriageTimeSeconds = getDouble(row, "avg_triage_time_seconds");
        double avgAiConfidence = getDouble(row, "avg_ai_confidence");
        long vagueCount = getLong(row, "vague_count");

        long manualTriageOverrideCount = queryForLongValue("""
                SELECT COUNT(*)
				FROM admin_overrides
				WHERE ao_override_type IN ('CATEGORY', 'PRIORITY')
				   OR (ao_override_type = 'STATUS' AND ao_old_value = 'VAGUE')
                """);
        
        //Using all override decisions per distinct ticket because one ticket can have multiple triage override decisions but we are calculating the accuracy per last version triaged ticket and not per ticket version
        long triageOverriddenTicketCount = queryForLongValue("""
                SELECT COUNT(DISTINCT ao_ticket_id)
                FROM admin_overrides
                WHERE ao_override_type IN ('CATEGORY', 'PRIORITY')
                   OR (ao_override_type = 'STATUS' AND ao_old_value = 'VAGUE')
                """);
        
        triage.totalTicketsCreated = totalTicketsCreated;
        triage.triageCompletedCount = triageCompletedCount;
        triage.triageSuccessRate = calculatePercentage(triageCompletedCount, totalTicketsCreated);
        triage.averageTriageTimeSeconds = roundToTwoDecimals(avgTriageTimeSeconds);
        triage.vagueRate = calculatePercentage(vagueCount, triageCompletedCount);
        triage.averageAiConfidence = roundToFourDecimals(avgAiConfidence);
        triage.manualTriageOverrideRate = calculatePercentage(manualTriageOverrideCount, triageCompletedCount);
        triage.aiTriageAccuracy = calculatePercentage(triageCompletedCount - triageOverriddenTicketCount, triageCompletedCount);
        

        return triage;
    }

    private RoutingMetricsResponseBean buildRoutingMetrics() {
        RoutingMetricsResponseBean routing = new RoutingMetricsResponseBean();

        List<Map<String, Object>> outcomeRows = queryForRowList("""
                SELECT
                  ad_output_json ->> 'outcome' AS outcome,
                  COUNT(*) AS cnt
                FROM ai_decisions
                WHERE ad_decision_type = 'ROUTING'
                GROUP BY ad_output_json ->> 'outcome'
                """);

        long routingAttempts = 0L;
        long autoAssignedCount = 0L;
        long noEligibleAgentCount = 0L;

        for (Map<String, Object> row : outcomeRows) {
            String outcome = row.get("outcome") == null ? null : row.get("outcome").toString();
            long cnt = getLong(row, "cnt");
            routingAttempts += cnt;

            if ("ASSIGNED".equalsIgnoreCase(outcome)) {
                autoAssignedCount = cnt;
            } else if ("NO_ELIGIBLE_AGENT".equalsIgnoreCase(outcome)) {
                noEligibleAgentCount = cnt;
            }
        }

        double avgTimeToAssignmentFromTriageSeconds = queryForDoubleValue("""
                SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (ticket_first_assigned_at - ticket_ai_triaged_at))), 0)
                FROM tickets
                WHERE ticket_first_assigned_at IS NOT NULL
                	AND ticket_ai_triaged_at IS NOT NULL
                """);

        long assignmentOverrideCount = queryForLongValue("""
                SELECT COUNT(*)
                FROM admin_overrides
                WHERE ao_override_type = 'ASSIGNMENT'
                """);

        //Using all override decisions per distinct ticket because one ticket can have multiple assignment override decisions but we are calculating the accuracy per last version routed ticket and not per ticket version
        long routingOverriddenTicketCount = queryForLongValue("""
                SELECT COUNT(DISTINCT ao_ticket_id)
                FROM admin_overrides
                WHERE ao_override_type = 'ASSIGNMENT'
                """);
        
        routing.routingAttempts = routingAttempts;
        routing.autoRoutingSuccessRate = calculatePercentage(autoAssignedCount, routingAttempts);
        routing.noEligibleAgentRate = calculatePercentage(noEligibleAgentCount, routingAttempts);
        routing.averageTimeToAssignmentFromTriageSeconds = roundToTwoDecimals(avgTimeToAssignmentFromTriageSeconds);
        routing.assignmentOverrideRate = calculatePercentage(assignmentOverrideCount, autoAssignedCount);
        routing.autoRoutingAccuracy = calculatePercentage(autoAssignedCount - routingOverriddenTicketCount, autoAssignedCount);
        
        return routing;
    }

    private DuplicateMetricsResponseBean buildDuplicateMetrics() {
        DuplicateMetricsResponseBean duplicate = new DuplicateMetricsResponseBean();

        long duplicateChecksAttempted = queryForLongValue("""
                SELECT COUNT(*)
                FROM outbox_events
                WHERE oe_event_type = 'DUPLICATE_CHECK_REQUESTED'
                """);

        long duplicateChecksSucceeded = queryForLongValue("""
                SELECT COUNT(*)
                FROM outbox_events
                WHERE oe_event_type = 'DUPLICATE_CHECK_REQUESTED'
                  AND oe_status = 'DONE'
                """);

        double avgDuplicateCheckTimeSeconds = queryForDoubleValue("""
                SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (ticket_duplicate_checked_at - ticket_current_duplicate_check_started_at))), 0)
                FROM tickets
                WHERE ticket_duplicate_checked_at IS NOT NULL
                  AND ticket_current_duplicate_check_started_at IS NOT NULL
                """);

        long autoConfirmedCount = queryForLongValue("""
                SELECT COUNT(*)
                FROM ai_decisions
                WHERE ad_decision_type = 'DUPLICATE_CHECK'
                  AND ad_output_json ->> 'duplicateState' = 'CONFIRMED'
                """);

        long autoConfirmedOverturnedCount = queryForLongValue("""
                SELECT COUNT(DISTINCT ao_ticket_id)
                FROM admin_overrides
                WHERE ao_override_type = 'DUPLICATE_LINK'
                  AND ao_old_value = 'CONFIRMED'
                  AND ao_new_value = 'NONE'
                """);

        long duplicateReviewQueueSize = queryForLongValue("""
                SELECT COUNT(*)
                FROM tickets
                WHERE ticket_status::text = 'DUPLICATE_REVIEW'
                """);

        double avgPotentialReviewTimeMinutes = queryForDoubleValue("""
                SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (ao.ao_created_at - t.ticket_duplicate_checked_at)) / 60.0), 0)
                FROM admin_overrides ao
                JOIN tickets t
                  ON t.ticket_id = ao.ao_ticket_id
                WHERE ao.ao_override_type = 'DUPLICATE_LINK'
                  AND ao.ao_old_value = 'POTENTIAL'
                  AND ao.ao_new_value IN ('CONFIRMED', 'NONE')
                  AND t.ticket_duplicate_checked_at IS NOT NULL
                """);

        long reviewedPotentialCount = queryForLongValue("""
                SELECT COUNT(DISTINCT ao_ticket_id)
                FROM admin_overrides
                WHERE ao_override_type = 'DUPLICATE_LINK'
                  AND ao_old_value = 'POTENTIAL'
                  AND ao_new_value IN ('CONFIRMED', 'NONE')
                """);

        long potentialConfirmedCount = queryForLongValue("""
                SELECT COUNT(DISTINCT ao_ticket_id)
                FROM admin_overrides
                WHERE ao_override_type = 'DUPLICATE_LINK'
                  AND ao_old_value = 'POTENTIAL'
                  AND ao_new_value = 'CONFIRMED'
                """);

        long duplicateWorkSavedCount = queryForLongValue("""
                SELECT COUNT(*)
                FROM tickets
                WHERE ticket_duplicate_state = 'CONFIRMED'
                """);

        long resolvedThroughPrimarCount = queryForLongValue("""
                SELECT COUNT(*)
                FROM ticket_duplicate_links tdl
                JOIN tickets t
        		  ON t.ticket_id = tdl.tdl_duplicate_ticket_id
                WHERE tdl.tdl_duplicate)type = 'CONFIRMED'
                  AND tdl.tdl_link_status = 'ACTIVE'
                  AND tdl.tdl_propagate_resolution = true
                  AND t.tickt_status::text IN ('RESOLVED', 'CLOSED')                                    
        		""");
        
        duplicate.duplicateChecksAttempted = duplicateChecksAttempted;
        duplicate.duplicateCheckSuccessRate = calculatePercentage(duplicateChecksSucceeded, duplicateChecksAttempted);
        duplicate.averageDuplicateCheckTimeSeconds = roundToTwoDecimals(avgDuplicateCheckTimeSeconds);

        duplicate.autoConfirmedRate = calculatePercentage(autoConfirmedCount, duplicateChecksSucceeded);
        duplicate.autoConfirmedAcceptanceRate =
                calculatePercentage(autoConfirmedCount - autoConfirmedOverturnedCount, autoConfirmedCount);

        duplicate.duplicateReviewQueueSize = duplicateReviewQueueSize;
        duplicate.averagePotentialReviewTimeMinutes = roundToTwoDecimals(avgPotentialReviewTimeMinutes);
        duplicate.potentialConfirmationRate = calculatePercentage(potentialConfirmedCount, reviewedPotentialCount);

        duplicate.duplicateWorkSavedCount = duplicateWorkSavedCount;
        duplicate.resolvedThroughPrimaryCount = resolvedThroughPrimarCount;

        return duplicate;
    }
    
    private TicketSummaryMetricsResponseBean buildAdminTicketSummary() {
        TicketSummaryMetricsResponseBean resp = new TicketSummaryMetricsResponseBean();

        List<Map<String, Object>> activeStatusRows = queryForRowList("""
                SELECT ticket_status, COUNT(*) AS cnt
                FROM tickets
                WHERE ticket_status NOT IN ('RESOLVED', 'CLOSED')
                GROUP BY ticket_status
                """);

        Map<String, Long> activeStatusCounts = toStatusCountMap(activeStatusRows);

        Map<String, Object> activeSummaryRow = queryForSingleRow("""
                SELECT
                  COUNT(*) AS total_tickets,
                  COUNT(ticket_assigned_to) AS assigned_count,
                  COUNT(*) FILTER (WHERE ticket_assigned_to IS NULL) AS unassigned_count,
                  COUNT(*) FILTER (WHERE ticket_ai_priority = 'HIGH' ) AS high_priority_count,
        		  COUNT(*) FILTER (WHERE ticket_ai_priority = 'URGENT' ) AS urgent_priority_count
                FROM tickets
                WHERE ticket_status NOT IN ('RESOLVED', 'CLOSED')
                """);
        
        Map<String, Object> completedSummaryRow = queryForSingleRow("""
                SELECT                                    
                  COUNT(*) FILTER (WHERE ticket_status = 'RESOLVED' ) AS resolved_count,
        		  COUNT(*) FILTER (WHERE ticket_status = 'CLOSED' ) AS closed_count
                FROM tickets                
                """);

        resp.totalTickets = getLong(activeSummaryRow, "total_tickets");
        resp.newCount = activeStatusCounts.getOrDefault("NEW", 0L);
        resp.aiProcessingCount = activeStatusCounts.getOrDefault("AI_PROCESSING", 0L);
        resp.vagueCount = activeStatusCounts.getOrDefault("VAGUE", 0L);
        resp.readyCount = activeStatusCounts.getOrDefault("READY", 0L);
        resp.inProgressCount = activeStatusCounts.getOrDefault("IN_PROGRESS", 0L);

        resp.assignedCount = getLong(activeSummaryRow, "assigned_count");
        resp.unassignedCount = getLong(activeSummaryRow, "unassigned_count");
        resp.highPriorityCount = getLong(activeSummaryRow, "high_priority_count");
        resp.urgentPriorityCount = getLong(activeSummaryRow, "urgent_priority_count");

        resp.resolvedCount = getLong(completedSummaryRow, "resolved_count");
        resp.closedCount = getLong(completedSummaryRow, "closed_count");
        
        return resp;
    }

    private TicketSummaryMetricsResponseBean buildAgentTicketSummary(Long agentUserId) {
        TicketSummaryMetricsResponseBean resp = new TicketSummaryMetricsResponseBean();

        List<Map<String, Object>> activeStatusRows = queryForRowList("""
                SELECT ticket_status, COUNT(*) AS cnt
                FROM tickets
                WHERE ticket_assigned_to = ?
                	AND ticket_status NOT IN ('RESOLVED', 'CLOSED')
                GROUP BY ticket_status
                """, agentUserId);

        Map<String, Long> activeStatusCounts = toStatusCountMap(activeStatusRows);
        
        Map<String, Object> activeSummaryRow = queryForSingleRow("""
        		SELECT
        		  COUNT(*) AS total_tickets,
        		  COUNT(*) FILTER (WHERE ticket_ai_priority = 'HIGH' ) AS high_priority_count,
        		  COUNT(*) FILTER (WHERE ticket_ai_priority = 'URGENT' ) AS urgent_priority_count
        		FROM tickets
        		WHERE ticket_assigned_to = ?
        		    AND ticket_status NOT IN ('RESOLVED', 'CLOSED')
        		""", agentUserId);

        Map<String, Object> completedSummaryRow = queryForSingleRow("""
                SELECT                                    
                  COUNT(*) FILTER (WHERE ticket_status = 'RESOLVED' ) AS resolved_count,
        		  COUNT(*) FILTER (WHERE ticket_status = 'CLOSED' ) AS closed_count
                FROM tickets 
                WHERE ticket_assigned_to = ?           
                """, agentUserId);
        
        long totalActiveTickets = activeStatusCounts.values().stream().mapToLong(Long::longValue).sum();

        resp.totalTickets = totalActiveTickets;
        resp.newCount = activeStatusCounts.getOrDefault("NEW", 0L);
        resp.aiProcessingCount = activeStatusCounts.getOrDefault("AI_PROCESSING", 0L);
        resp.vagueCount = activeStatusCounts.getOrDefault("VAGUE", 0L);
        resp.readyCount = activeStatusCounts.getOrDefault("READY", 0L);
        resp.inProgressCount = activeStatusCounts.getOrDefault("IN_PROGRESS", 0L);
        
        resp.assignedCount = totalActiveTickets;
        resp.unassignedCount = 0L;
        resp.highPriorityCount = getLong(activeSummaryRow, "high_priority_count");
        resp.urgentPriorityCount = getLong(activeSummaryRow, "urgent_priority_count");

        resp.resolvedCount = getLong(completedSummaryRow, "resolved_count");
        resp.closedCount = getLong(completedSummaryRow, "closed_count");
        
        return resp;
    }

    private Map<String, Long> toStatusCountMap(List<Map<String, Object>> rows) {
        Map<String, Long> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object statusObj = row.get("ticket_status");
            if (statusObj != null) {
                map.put(statusObj.toString(), getLong(row, "cnt"));
            }
        }
        return map;
    }

    private Map<String, Object> queryForSingleRow(String sql, Object... args) {
        try {
            return jdbcTemplate.queryForMap(sql, args);
        } catch (Exception ex) {
            METRICS_SERVICE_LOG.error("Metrics queryForSingleRow failed. SQL={}", sql, ex);
            return Map.of();
        }
    }

    private List<Map<String, Object>> queryForRowList(String sql, Object... args) {
        try {
            return jdbcTemplate.queryForList(sql, args);
        } catch (Exception ex) {
            METRICS_SERVICE_LOG.error("Metrics queryForRowList failed. SQL={}", sql, ex);
            return List.of();
        }
    }

    private long queryForLongValue(String sql, Object... args) {
        try {
            Long value = jdbcTemplate.queryForObject(sql, Long.class, args);
            return value == null ? 0L : value;
        } catch (Exception ex) {
            METRICS_SERVICE_LOG.error("Metrics queryForLongValue failed. SQL={}", sql, ex);
            return 0L;
        }
    }

    private double queryForDoubleValue(String sql, Object... args) {
        try {
            Double value = jdbcTemplate.queryForObject(sql, Double.class, args);
            return value == null ? 0.0 : value;
        } catch (Exception ex) {
            METRICS_SERVICE_LOG.error("Metrics queryForDoubleValue failed. SQL={}", sql, ex);
            return 0.0;
        }
    }

    private long getLong(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof Number n) {
            return n.longValue();
        }
        return 0L;
    }

    private double getDouble(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return 0.0;
    }

    private double calculatePercentage(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0.0;
        }
        return roundToTwoDecimals((numerator * 100.0) / denominator);
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double roundToFourDecimals(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}