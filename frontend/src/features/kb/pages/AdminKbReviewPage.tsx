import React from "react";
import {
  Alert,
  Box,
  Button,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../../../state/AuthContext";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import { formatDateTime } from "../../../utils/dateTime";
import {
  useKbDraftsInReviewAdmin,
  useReviewKbDraftDecisionAdmin,
} from "../hooks";
import KbArticleCard from "../components/KbArticleCard";
import KbReviewDecisionPanel from "../components/KbReviewDecisionPanel";
import ArrowBackOutlinedIcon from "@mui/icons-material/ArrowBackOutlined";

function MetaBlock({
  label,
  value,
}: {
  label: string;
  value: React.ReactNode;
}) {
  return (
    <Box
      sx={{
        p: 1,
        borderRadius: 1.5,
        border: "1px solid rgba(2,48,71,0.08)",
        backgroundColor: "rgba(255,255,255,0.72)",
      }}
    >
      <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 800 }}>
        {label}
      </Typography>
      <Typography variant="body2" sx={{ fontWeight: 800, mt: 0.25, wordBreak: "break-word" }}>
        {value}
      </Typography>
    </Box>
  );
}

export default function AdminKbReviewPage() {
  const nav = useNavigate();
  const { auth } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();

  const { data, isLoading, isError } = useKbDraftsInReviewAdmin(
    Boolean(auth.token && auth.role === "ADMIN")
  );

  const drafts = data ?? [];
  const initialKbId = searchParams.get("kbId");
  const initialSelected =
    initialKbId != null ? drafts.find((d) => d.kbId === Number(initialKbId)) : undefined;

  const [selectedKbId, setSelectedKbId] = React.useState<number | null>(
    initialSelected?.kbId ?? drafts[0]?.kbId ?? null
  );

  React.useEffect(() => {
    if (selectedKbId == null && drafts.length > 0) {
      setSelectedKbId(drafts[0].kbId);
    }
  }, [selectedKbId, drafts]);

  const selected = drafts.find((d) => d.kbId === selectedKbId) ?? null;
  const reviewMutation = useReviewKbDraftDecisionAdmin(selected?.kbId ?? -1);

  const submitDecision = async (action: "APPROVE" | "REJECT") => {
    if (!selected) return;
    await reviewMutation.mutateAsync({ action });
  };

  if (isLoading) return <LoadingSkeleton variant="list" count={6} />;
  if (isError) return <Alert severity="error">Failed to load KB review queue.</Alert>;

  return (
    <Stack spacing={1.5}>
      <Paper
        variant="outlined"
        sx={{
          p: 1.5,
          borderRadius: 2,
          background: "linear-gradient(180deg, rgba(255,255,255,1), rgba(244,249,252,1))",
        }}
      >
        <Stack spacing={0.6}>
          <Box>
            <Button
              variant="text"
              startIcon={<ArrowBackOutlinedIcon />}
              onClick={() => nav(-1)}
              sx={{ px: 0, minWidth: 0 }}
            >
              Back
            </Button>
          </Box>
          <Typography variant="h4" sx={{ fontWeight: 900 }}>
            KB Review Queue
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.35 }}>
            Review agent-submitted KB drafts and decide whether they should be approved or rejected.
          </Typography>
        </Stack>
      </Paper>

      {drafts.length === 0 ? (
        <Paper
          variant="outlined"
          sx={{
            p: 2,
            borderRadius: 2,
            borderStyle: "dashed",
            textAlign: "center",
          }}
        >
          <Typography sx={{ fontWeight: 800, mb: 0.35 }}>No drafts in review</Typography>
          <Typography variant="body2" color="text.secondary">
            The review queue is currently empty.
          </Typography>
        </Paper>
      ) : (
        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: {
              xs: "1fr",
              xl: "0.95fr 1.3fr",
            },
            gap: 1.2,
            alignItems: "start",
          }}
        >
          <Stack spacing={1}>
            {drafts.map((draft) => (
              <KbArticleCard
                key={draft.kbId}
                article={draft}
                onClick={() => {
                  setSelectedKbId(draft.kbId);
                  setSearchParams({ kbId: String(draft.kbId) });
                }}
                showStatus
                showSourceTicket
              />
            ))}
          </Stack>

          {selected ? (
            <Stack spacing={1.1}>
              <Paper
                variant="outlined"
                sx={{
                  p: 1.5,
                  borderRadius: 2,
                  border: "1px solid rgba(2,48,71,0.10)",
                }}
              >
                <Stack spacing={1}>
                  <Typography variant="h5" sx={{ fontWeight: 900 }}>
                    {selected.title}
                  </Typography>

                  <Box
                    sx={{
                      display: "grid",
                      gridTemplateColumns: {
                        xs: "1fr",
                        md: "repeat(2, minmax(0, 1fr))",
                      },
                      gap: 0.9,
                    }}
                  >
                    <MetaBlock label="KB ID" value={selected.kbId} />
                    <MetaBlock
                      label="Source Ticket"
                      value={selected.sourceTicketId ? `Ticket #${selected.sourceTicketId}` : "—"}
                    />
                    <MetaBlock label="Created By" value={selected.createdByName ?? "—"} />
                    <MetaBlock label="Submitted At" value={formatDateTime(selected.agentSubmittedAt)} />
                    <MetaBlock label="Last Modified By" value={selected.lastModifiedByName ?? "—"} />
                    <MetaBlock label="Last Updated" value={formatDateTime(selected.updatedAt)} />
                  </Box>

                  <Paper
                    variant="outlined"
                    sx={{
                      p: 1.4,
                      borderRadius: 2,
                      border: "1px solid rgba(2,48,71,0.08)",
                    }}
                  >
                    <Typography
                      sx={{
                        whiteSpace: "pre-wrap",
                        lineHeight: 1.8,
                      }}
                    >
                      {selected.body}
                    </Typography>
                  </Paper>
                </Stack>
              </Paper>

              <KbReviewDecisionPanel
                onApprove={() => submitDecision("APPROVE")}
                onReject={() => submitDecision("REJECT")}
                isSubmitting={reviewMutation.isPending}
                isError={reviewMutation.isError}
              />
            </Stack>
          ) : null}
        </Box>
      )}
    </Stack>
  );
}