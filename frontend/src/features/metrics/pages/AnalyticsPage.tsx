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
  const duplicate = data.duplicate;
  const kbSuggestion = data.kbSuggestion;
  const kbDraft = data.kbDraft;

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
        summary="Average time taken for the current AI triage attempt to complete."
        interpretation="Lower times indicate the system is processing and classifying the latest ticket version more quickly."
        calculation="Average elapsed time from the start of the current triage attempt to successful AI triage completion."
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

  const duplicateCards = (
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
        title="Duplicate Checks Attempted"
        value={duplicate.duplicateChecksAttempted}
        summary="Number of tickets processed by duplicate detection."
        interpretation="Higher means the system is actively detecting duplicates."
        calculation="Count of tickets for which duplicate detection was attempted."
      />

      <MetricCard
        title="Duplicate Check Success Rate"
        value={formatPercent(duplicate.duplicateCheckSuccessRate)}
        summary="Percentage of duplicate checks completed successfully."
        interpretation="Shows the reliability of the duplicate pipeline."
        calculation="Successful duplicate checks divided by duplicate detection attempts."
      />

      <MetricCard
        title="Average Duplicate Check Time"
        value={formatSeconds(duplicate.averageDuplicateCheckTimeSeconds)}
        summary="Time taken to complete duplicate detection."
        interpretation="Lower is better."
        calculation="Average elapsed time to complete the duplicate detection stage."
      />

      {/* <MetricCard
        title="Auto-Confirmed Rate"
        value={formatPercent(duplicate.autoConfirmedRate)}
        summary="Percentage of tickets automatically marked as duplicates."
        interpretation="Higher means more automation."
        calculation="Auto-confirmed duplicates divided by duplicate checks attempted."
      /> */}

      <MetricCard
        title="Duplicate Work Saved Count"
        value={duplicate.duplicateWorkSavedCount}
        summary="Number of duplicate tickets avoided from independent processing."
        interpretation="Direct productivity gain."
        calculation="Count of duplicate tickets consolidated instead of independently processed."
      />

      <MetricCard
        title="Auto-Resolved Through Primary Count"
        value={duplicate.resolvedThroughPrimaryCount}
        summary="Duplicates auto-resolved via primary ticket lifecycle."
        interpretation="Shows successful consolidation."
        calculation="Count of duplicate tickets resolved through linked primary resolution."
      />
    </Box>
  );

  const kbSuggestionCards = (
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
        title="Suggestion Attempts"
        value={kbSuggestion.suggestionAttempts}
        summary="Number of tickets evaluated for a knowledge base suggestion."
        interpretation="Higher values indicate the system is actively attempting self-service resolution before routing."
        calculation="Count of KB suggestion attempts."
      />

      <MetricCard
        title="Average Suggestion Confidence"
        value={formatConfidence(kbSuggestion.averageSuggestionConfidence)}
        summary="Average confidence of the AI when making KB suggestion decisions."
        interpretation="Higher values suggest the model is more certain that the suggested article is relevant."
        calculation="Average confidence across KB suggestion decisions."
      />

      <MetricCard
        title="Average Suggestion Similarity"
        value={kbSuggestion.averageSuggestionSimilarity?.toFixed(3) ?? "—"}
        summary="Average similarity score between the ticket content and the suggested KB article."
        interpretation="Higher values suggest stronger semantic alignment between issues and suggested articles."
        calculation="Average similarity score across KB suggestions."
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

  const kbDraftCards = (
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
        title="Draft Generation Attempts"
        value={kbDraft.draftGenerationAttempts}
        summary="Number of agent-triggered KB draft generation attempts."
        interpretation="Higher values indicate more resolved tickets are being converted into draft knowledge assets."
        calculation="Count of KB draft generation attempts."
      />

      <MetricCard
        title="Draft Generation Success Rate"
        value={formatPercent(kbDraft.draftGenerationSuccessRate)}
        summary="Percentage of KB draft generation attempts completed successfully."
        interpretation="Higher values indicate the AI drafting pipeline is operating reliably."
        calculation="Successful KB draft generations divided by total generation attempts."
      />

      <MetricCard
        title="Average Review Turnaround"
        value={
          kbDraft.averageReviewTurnaroundHours == null
            ? "—"
            : `${kbDraft.averageReviewTurnaroundHours.toFixed(1)} hrs`
        }
        summary="Average time taken for KB drafts to move through the admin review process."
        interpretation="Lower values indicate a faster review and publishing workflow."
        calculation="Average turnaround time in hours for reviewed KB drafts."
      />

      <MetricCard
        title="Published AI Draft Count"
        value={kbDraft.publishedAiDraftCount}
        summary="Number of AI-generated KB drafts that ultimately became published KB articles."
        interpretation="Higher values indicate stronger long-term knowledge capture from resolved tickets."
        calculation="Count of AI-generated KB drafts that reached published status."
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
              This dashboard shows how effectively the AI pipeline completes triage, detects duplicates, and routes actionable work to department-specific agents.
            </Typography>
          </Box>

          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: {
                xs: "1fr",
                md: "repeat(4, minmax(0, 1fr))",
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
                bgcolor: alpha("#023047", 0.05),
                border: "1px solid rgba(2,48,71,0.10)",
              }}
            >
              <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 800 }}>
                Auto-routing Accuracy
              </Typography>
              <Typography variant="h5" sx={{ fontWeight: 900, mt: 0.25 }}>
                {formatPercent(routing.autoRoutingAccuracy)}
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
                Duplicate Work Saved Count
              </Typography>
              <Typography variant="h5" sx={{ fontWeight: 900, mt: 0.25 }}>
                {duplicate.duplicateWorkSavedCount}
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
              summary: "Percentage of created tickets for which AI triage completed successfully.",
              interpretation:
                "A higher value means the system is classifying and evaluating incoming tickets reliably.",
              calculation: "Triage completed count divided by total tickets created in scope.",
            }}
            tertiaryLabel="Vague Rate"
            tertiaryValue={formatPercent(triage.vagueRate)}
            tertiaryInfo={{
              title: "Vague Rate",
              summary: "Percentage of tickets that required clarification after triage.",
              interpretation:
                "Higher values suggest the system is frequently detecting missing or unclear ticket details.",
              calculation: "Tickets marked as vague divided by triaged tickets.",
            }}
            progressValue={triage.aiTriageAccuracy ?? null}
            progressLabel="Human acceptance of AI triage decisions"
          />
        }
        cards={triageCards}
      />

      <MetricsSection
        title="DUPLICATE CHECK"
        description="After successful triage, the DUPLICATE CHECK phase evaluates each ticket for similarity, maks them as NONE, POTENTIAL for admin review, or CONFIRMED, facilitating the consolidation of repeated issues."
        processSteps={["Duplicate Check", "Potential Review", "Confirmed Link"]}
        highlight={
          <MetricsHighlightCard
            title="Duplicate Detection Overview"
            primaryLabel="Auto-Confirmed Acceptance Rate"
            primaryValue={formatPercent(duplicate.autoConfirmedAcceptanceRate)}
            primaryInfo={{
              title: "Auto-Confirmed Acceptance Rate",
              summary: "Percentage of auto-confirmed duplicate decisions that were accepted without later manual override.",
              interpretation:
                "A higher value means the duplicate-detection system is making confirmed-duplicate decisions that align more closely with human judgment.",
              calculation:
                "Calculated as the percentage of auto-confirmed duplicate decisions that were not later overturned through admin review or override.",
            }}
            secondaryLabel="Duplicate Review Queue Size"
            secondaryValue={duplicate.duplicateReviewQueueSize}
            secondaryInfo={{
              title: "Duplicate Review Queue Size",
              summary: "Tickets waiting for admin review.",
              interpretation: "Higher values indicate a growing backlog.",
              calculation: "Count of tickets currently waiting in duplicate review.",
            }}
            tertiaryLabel="Potential Confirmation Rate"
            tertiaryValue={formatPercent(duplicate.potentialConfirmationRate)}
            tertiaryInfo={{
              title: "Potential Confirmation Rate",
              summary: "Percentage of POTENTIAL tickets confirmed by admin.",
              interpretation: "Shows usefulness of AI duplicate suggestions.",
              calculation: "Confirmed potential duplicates divided by reviewed potential duplicates.",
            }}
            progressValue={duplicate.autoConfirmedAcceptanceRate ?? null}
            progressLabel="Human acceptance of duplicate decisions"
          />
        }
        cards={duplicateCards}
      />

      <MetricsSection
        title="KB SUGGESTION"
        processSteps={[
          "Similarity Search",
          "User-facing Suggestion",
          "User Decision",
        ]}
        description="After successful duplicate check, the KB SUGGESTION phase identifies a potentially relevant knowledge article, presents it to the user, and waits for the user to accept the suggestion or request further help."
        highlight={
          <MetricsHighlightCard
            title="KB Suggestion Performance"
            primaryLabel="Auto Suggestion Acceptance Rate"
            primaryValue={formatPercent(kbSuggestion.autoSuggestionAcceptanceRate)}
            primaryInfo={{
              title: "Auto Suggestion Acceptance Rate",
              summary: "Percentage of automatically suggested KB articles that were accepted by users.",
              interpretation:
                "A higher value means the system is surfacing articles that are more effective at resolving issues without agent intervention.",
              calculation:
                "Accepted automatic KB suggestions divided by automatic KB suggestions reviewed by users.",
            }}
            secondaryLabel="Auto Suggestion Rejection Rate"
            secondaryValue={formatPercent(kbSuggestion.autoSuggestionRejectionRate)}
            secondaryInfo={{
              title: "Auto Suggestion Rejection Rate",
              summary: "Percentage of automatically suggested KB articles that were rejected by users.",
              interpretation:
                "A higher value suggests the automatic suggestion engine is surfacing less relevant or less helpful articles.",
              calculation:
                "Rejected automatic KB suggestions divided by automatic KB suggestions reviewed by users.",
            }}
            tertiaryLabel="Manual Suggestion Count"
            tertiaryValue={kbSuggestion.manualSuggestionCount}
            tertiaryInfo={{
              title: "Manual Suggestion Count",
              summary: "Number of KB suggestions manually triggered by agents.",
              interpretation:
                "Higher values indicate more fallback to agent-selected knowledge rather than automatic article suggestion.",
              calculation:
                "Count of manual KB suggestions triggered by agents.",
            }}
            progressValue={kbSuggestion.autoSuggestionAcceptanceRate ?? null}
            progressLabel="User acceptance of automatic KB suggestions"
          />
        }
        cards={kbSuggestionCards}
      />

      <MetricsSection
        title="ROUTING"
        processSteps={[
          "Uses Category as Department",
          "Finds Eligible Agents",
          "Assigns Least-loaded Agent",
        ]}
        description="After successful duplicate check, the ROUTING phase uses the ticket category as the department key, identifies eligible agents in that department, and assigns the NON-DUPLICATE ticket to the least-loaded available agent."
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
              summary: "Percentage of routing attempts that successfully resulted in an automatic assignment.",
              interpretation:
                "Higher values indicate the routing engine is successfully finding and assigning eligible agents more often.",
              calculation: "Successful automaticassignments divided by routing attempts.",
            }}
            tertiaryLabel="No Eligible Agent Rate"
            tertiaryValue={formatPercent(routing.noEligibleAgentRate)}
            tertiaryInfo={{
              title: "No Eligible Agent Rate",
              summary: "Percentage of routing attempts where no eligible agent could be assigned.",
              interpretation:
                "Higher values suggest staffing or department-coverage gaps are preventing assignment.",
              calculation: "Routing attempts with no eligible agent divided by total routing attempts.",
            }}
            progressValue={routing.autoRoutingAccuracy ?? null}
            progressLabel="Human acceptance of automatic routing decisions"
          />
        }
        cards={routingCards}
      />

      <MetricsSection
        title="KB DRAFT"
        processSteps={[
          "Public Comment Selection",
          "Draft Generation",
          "Review / Publish",
        ]}
        description="After ticket resolution, the KB DRAFT phase allows agents to convert public ticket resolution details into reusable knowledge base draft articles for later review and publication."
        highlight={
          <MetricsHighlightCard
            title="KB Drafting Performance"
            primaryLabel="Draft Approval Rate"
            primaryValue={formatPercent(kbDraft.draftApprovalRate)}
            primaryInfo={{
              title: "Draft Approval Rate",
              summary: "Percentage of submitted KB drafts that were approved during admin review.",
              interpretation:
                "A higher value means generated drafts are more often strong enough to become approved knowledge assets.",
              calculation:
                "Approved KB drafts divided by reviewed KB drafts.",
            }}
            secondaryLabel="Draft Rejection Rate"
            secondaryValue={formatPercent(kbDraft.draftRejectionRate)}
            secondaryInfo={{
              title: "Draft Rejection Rate",
              summary: "Percentage of submitted KB drafts that were rejected during review.",
              interpretation:
                "A higher value suggests more problems with draft quality, structure, or usefulness before publication.",
              calculation:
                "Rejected KB drafts divided by reviewed KB drafts.",
            }}
            tertiaryLabel="Submitted For Review"
            tertiaryValue={kbDraft.submittedForReviewCount}
            tertiaryInfo={{
              title: "Submitted For Review Count",
              summary: "Number of KB drafts sent into the admin review workflow.",
              interpretation:
                "Higher values indicate more generated knowledge is progressing beyond the draft stage.",
              calculation:
                "Count of KB drafts submitted for review.",
            }}
            progressValue={kbDraft.draftApprovalRate ?? null}
            progressLabel="Approval success of submitted KB drafts"
          />
        }
        cards={kbDraftCards}
      />
    </Stack>
  );
}