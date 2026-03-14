import React from "react";
import { Alert, Box, Paper, Stack, Typography } from "@mui/material";
import { alpha } from "@mui/material/styles";
import { useAuth } from "../../../state/AuthContext";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import { useAdminAiSummary } from "../hooks";
import MetricsSection from "../components/MetricsSection";
import MetricCard from "../components/MetricCard";
import MetricsHighlightCard from "../components/MetricsHighlightCard";
import { formatConfidence, formatPercent, formatSeconds } from "../../../utils/metricsFormat";

export default function AnalyticsPage() {
  const { auth } = useAuth();
  const { data, isLoading, isError } = useAdminAiSummary(Boolean(auth.token && auth.role === "ADMIN"));

  if (isLoading) return <LoadingSkeleton variant="list" count={8} />;
  if (isError || !data) return <Alert severity="error">Failed to load analytics.</Alert>;

  const triage = data.triage;
  const routing = data.routing;

  const triageCards = (
    <Box
      sx={{
        display: "grid",
        gridTemplateColumns: {
          xs: "1fr",
          md: "repeat(2, minmax(0, 1fr))",
        },
        gap: 1,
      }}
    >
      <MetricCard
        title="Total Tickets Created"
        value={triage.totalTicketsCreated}
        summary="Total number of tickets created in the system."
        interpretation="This provides the overall activity context for all triage metrics."
        calculation="Count of created tickets in scope."
      />

      <MetricCard
        title="Triage Completed"
        value={triage.triageCompletedCount}
        summary="Number of tickets that completed AI triage and produced usable triage output."
        interpretation="This indicates how many tickets successfully moved through the AI triage phase."
        calculation="Count of tickets with successful AI triage output."
      />

      <MetricCard
        title="Average Triage Time"
        value={formatSeconds(triage.averageTriageTimeSeconds)}
        summary="Average time taken for a newly created ticket to complete AI triage."
        interpretation="Lower times indicate the system is understanding and classifying tickets more quickly."
        calculation="Average elapsed time from ticket creation to successful AI triage completion."
      />

      <MetricCard
        title="Average AI Confidence"
        value={formatConfidence(triage.averageAiConfidence)}
        summary="Average confidence level of the AI across completed triage decisions."
        interpretation="Higher values suggest the model is making triage decisions with stronger certainty."
        calculation="Average confidence score across completed triage results."
      />

      <MetricCard
        title="Manual Triage Override Rate"
        value={formatPercent(triage.manualTriageOverrideRate)}
        summary="Share of triaged tickets that later required manual correction to AI triage output."
        interpretation="A lower rate indicates higher AI usefulness and closer alignment with human judgment."
        calculation="Overrides affecting category, priority, or vague handling divided by triaged tickets."
      />
    </Box>
  );

  const routingCards = (
    <Box
      sx={{
        display: "grid",
        gridTemplateColumns: {
          xs: "1fr",
          md: "repeat(2, minmax(0, 1fr))",
        },
        gap: 1,
      }}
    >
      <MetricCard
        title="Routing Attempts"
        value={routing.routingAttempts}
        summary="Number of tickets for which the system attempted an automatic routing decision."
        interpretation="This sets the baseline for understanding routing automation success and staffing-related constraints."
        calculation="Count of routing decisions attempted in scope."
      />

      <MetricCard
        title="Average Time to Assignment"
        value={formatSeconds(routing.averageTimeToAssignmentFromTriageSeconds)}
        summary="Average time from completed triage to the first ticket assignment."
        interpretation="This shows how quickly actionable tickets move to eligible department-specific agents with the lowest workload."
        calculation="Average elapsed time from triage completion to first assignment."
      />

      <MetricCard
        title="Assignment Override Rate"
        value={formatPercent(routing.assignmentOverrideRate)}
        summary="Measures how often an automatically assigned ticket was later manually reassigned."
        interpretation="A lower value suggests the routing logic is selecting the right assignee more consistently."
        calculation="Manual assignment changes divided by auto-assigned tickets."
      />
    </Box>
  );

  return (
    <Stack spacing={1.75}>
            {/* Hero band */}
      <Paper
        variant="outlined"
        sx={{
          p: 1.6,
          borderRadius: 2,
          background:
            "linear-gradient(180deg, rgba(255,255,255,1), rgba(244,249,252,1))",
        }}
      >
        <Stack spacing={1.25}>
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 900 }}>
              AI Analytics Dashboard
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.35, maxWidth: 880 }}>
              This dashboard shows how effectively the AI pipeline completes triage and routes actionable work to department-specific agents.
            </Typography>
          </Box>

          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: {
                xs: "1fr",
                md: "repeat(3, minmax(0, 1fr))",
              },
              gap: 1,
            }}
          >
            <Paper
              variant="outlined"
              sx={{
                p: 1.2,
                borderRadius: 2,
                bgcolor: alpha("#219EBC", 0.06),
                border: "1px solid rgba(33,158,188,0.16)",
              }}
            >
              <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 800 }}>
                AI Triage Accuracy
              </Typography>
              <Typography variant="h5" sx={{ fontWeight: 900, mt: 0.25 }}>
                {formatPercent(triage.aiTriageAccuracy)}
              </Typography>
            </Paper>

            <Paper
              variant="outlined"
              sx={{
                p: 1.2,
                borderRadius: 2,
                bgcolor: alpha("#023047", 0.05),
                border: "1px solid rgba(2,48,71,0.10)",
              }}
            >
              <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 800 }}>
                Average Triage Time
              </Typography>
              <Typography variant="h5" sx={{ fontWeight: 900, mt: 0.25 }}>
                {formatSeconds(triage.averageTriageTimeSeconds)}
              </Typography>
            </Paper>

            <Paper
              variant="outlined"
              sx={{
                p: 1.2,
                borderRadius: 2,
                bgcolor: alpha("#15803D", 0.06),
                border: "1px solid rgba(21,128,61,0.14)",
              }}
            >
              <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 800 }}>
                Auto-routing Accuracy
              </Typography>
              <Typography variant="h5" sx={{ fontWeight: 900, mt: 0.25 }}>
                {formatPercent(routing.autoRoutingAccuracy)}
              </Typography>
            </Paper>
          </Box>
        </Stack>
      </Paper>

      <MetricsSection
        title="TRIAGE"
        processSteps={[
          "Classification",
          "Priority Prediction",
          "Vague Detection",
        ]}
        description="The TRIAGE phase is the first AI stage after ticket creation. It classifies the ticket, predicts priority, and checks whether the request is too vague to continue without more user input."
        highlight={
          <MetricsHighlightCard
            title="Overall Triage Performance"
            primaryLabel="AI Triage Accuracy"
            primaryValue={formatPercent(triage.aiTriageAccuracy)}
            primaryInfo={{
              title: "AI Triage Accuracy",
              summary: "Percentage of AI-triaged tickets that were accepted without manual correction.",
              interpretation:
                "A higher value means the AI's category, priority, and vague-ticket decisions are aligning more closely with human judgment.",
              calculation:
                "Calculated as the percentage of triaged tickets that did not later require manual admin override of category, priority, or a vague-to-non-vague status correction.",
            }}
            secondaryLabel="Triage Success Rate"
            secondaryValue={formatPercent(triage.triageSuccessRate)}
            secondaryInfo={{
              title: "Triage Success Rate",
              summary: "Measures the share of created tickets that successfully completed AI triage.",
              interpretation:
                "A higher value means the system is classifying and evaluating incoming tickets reliably.",
              calculation: "Triaged tickets divided by total created tickets.",
            }}
            tertiaryLabel="Vague Rate"
            tertiaryValue={formatPercent(triage.vagueRate)}
            tertiaryInfo={{
              title: "Vague Rate",
              summary: "Measures how often tickets require more information before they can continue through automation.",
              interpretation:
                "A higher value suggests more requests are arriving without enough detail for direct handling.",
              calculation: "Tickets marked vague divided by triaged tickets.",
            }}
            progressValue={triage.aiTriageAccuracy ?? null}
            progressLabel="Human acceptance of AI triage decisions"
          />
        }
        cards={triageCards}
      />

      <MetricsSection
        title="ROUTING"
        processSteps={[
          "Uses Category as Department",
          "Finds Eligible Agents",
          "Assigns Least-loaded Agent",
        ]}
        description="After successful triage, the ROUTING phase uses the ticket category as the department key, identifies eligible agents in that department, and assigns the ticket to the least-loaded available agent."
        highlight={
          <MetricsHighlightCard
            title="Overall Routing Performance"
            primaryLabel="Auto-routing Accuracy"
            primaryValue={formatPercent(routing.autoRoutingAccuracy)}
            primaryInfo={{
              title: "Auto-routing Accuracy",
              summary: "Percentage of auto-assigned routing decisions that were accepted without manual reassignment.",
              interpretation:
                "A higher value means the system is assigning actionable tickets to the right agent more reliably.",
              calculation:
                "Calculated as the percentage of auto-routed tickets with routing outcome ASSIGNED that were not later manually overridden through reassignment.",
            }}
            secondaryLabel="Auto-routing Success Rate"
            secondaryValue={formatPercent(routing.autoRoutingSuccessRate)}
            secondaryInfo={{
              title: "Auto-routing Success Rate",
              summary: "Measures the share of routing attempts that resulted in a successful automatic assignment.",
              interpretation:
                "A higher value means actionable tickets are reaching the right queues without human intervention.",
              calculation: "Successful auto-assignments divided by routing attempts.",
            }}
            tertiaryLabel="No Eligible Agent Rate"
            tertiaryValue={formatPercent(routing.noEligibleAgentRate)}
            tertiaryInfo={{
              title: "No Eligible Agent Rate",
              summary: "Measures how often routing could not assign a ticket because no suitable agent was available.",
              interpretation:
                "This helps separate staffing or coverage constraints from routing quality issues.",
              calculation: "No-eligible-agent outcomes divided by routing attempts.",
            }}
            progressValue={routing.autoRoutingAccuracy ?? null}
            progressLabel="Human acceptance of automatic routing decisions"
          />
        }
        cards={routingCards}
      />
    </Stack>
  );
}