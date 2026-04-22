import React from "react";
import {
  Alert,
  Box,
  Button,
  Chip,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import FactCheckOutlinedIcon from "@mui/icons-material/FactCheckOutlined";
import DescriptionOutlinedIcon from "@mui/icons-material/DescriptionOutlined";
import ArrowBackOutlinedIcon from "@mui/icons-material/ArrowBackOutlined";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../../state/AuthContext";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import { formatDateTime } from "../../../utils/dateTime";
import { useKbArticleById } from "../hooks";

function statusChipSx(status?: string | null) {
  const s = (status ?? "").toUpperCase();

  switch (s) {
    case "PUBLISHED":
      return {
        bgcolor: "#97CC04",
        color: "#1F2D00",
      };

    case "IN_REVIEW":
      return {
        bgcolor: "#FFB703",
        color: "#5C4300",
      };

    case "DRAFT":
      return {
        bgcolor: "#219EBC",
        color: "#FFFFFF",
      };

    case "REJECTED":
      return {
        bgcolor: "#F45D01",
        color: "#FFFFFF",
      };

    default:
      return {
        bgcolor: "#E5E7EB",
        color: "#374151",
      };
  }
}

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

export default function KbDetailsPage() {
  const { kbId } = useParams();
  const { auth } = useAuth();
  const nav = useNavigate();

  const idNum = kbId ? Number(kbId) : null;
  const query = useKbArticleById(idNum, typeof idNum === "number");

  const isAdmin = auth.role === "ADMIN";
  const isAgent = auth.role === "AGENT";

  if (!idNum) return <Typography>Invalid KB id.</Typography>;
  if (query.isLoading) return <LoadingSkeleton variant="detail" />;
  if (query.isError) return <Alert severity="error">Failed to load knowledge base article.</Alert>;
  if (!query.data) return <Typography>Knowledge base article not found.</Typography>;

  const article = query.data;

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
        <Stack spacing={1.1}>
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
          <Stack
            direction={{ xs: "column", lg: "row" }}
            justifyContent="space-between"
            alignItems={{ xs: "flex-start", lg: "flex-start" }}
            spacing={1}
          >
            <Box sx={{ minWidth: 0 }}>
              <Stack direction="row" spacing={0.8} alignItems="center" sx={{ mb: 0.5 }}>
                <Box
                  sx={{
                    width: 36,
                    height: 36,
                    borderRadius: 2,
                    display: "grid",
                    placeItems: "center",
                    bgcolor: "rgba(33,158,188,0.12)",
                    color: "primary.main",
                  }}
                >
                  <DescriptionOutlinedIcon />
                </Box>
                <Typography variant="h4" sx={{ fontWeight: 900, lineHeight: 1.15 }}>
                  {article.title}
                </Typography>
              </Stack>

              <Stack direction="row" spacing={0.75} flexWrap="wrap" useFlexGap>
                <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 700 }}>
                  KB #{article.kbId}
                </Typography>

                {article.sourceTicketId ? (
                  <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 700 }}>
                    Source Ticket #{article.sourceTicketId}
                  </Typography>
                ) : null}

                {article.isAiGenerated != null ? (
                  <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 700 }}>
                    {article.isAiGenerated ? "AI Generated" : "Human Authored"}
                  </Typography>
                ) : null}
              </Stack>
            </Box>

            <Stack direction="row" spacing={0.8} flexWrap="wrap" useFlexGap>
              <Chip
                label={article.status ?? "—"}
                size="small"
                sx={{ fontWeight: 800, ...statusChipSx(article.status) }}
              />

              {isAdmin ? (
                <Button
                  variant="outlined"
                  startIcon={<EditOutlinedIcon />}
                  onClick={() => nav(`/admin/kb/${article.kbId}/edit`)}
                >
                  Edit Article
                </Button>
              ) : null}

              {isAdmin && (article.status ?? "").toUpperCase() === "IN_REVIEW" ? (
                <Button
                  variant="contained"
                  startIcon={<FactCheckOutlinedIcon />}
                  onClick={() => nav(`/admin/kb/review`)}
                >
                  Open Review Queue
                </Button>
              ) : null}

              {isAgent && (article.status ?? "").toUpperCase() === "DRAFT" ? (
                <Button
                  variant="contained"
                  startIcon={<EditOutlinedIcon />}
                  onClick={() => nav(`/agent/kb/${article.kbId}/draft`)}
                >
                  Edit Draft
                </Button>
              ) : null}
            </Stack>
          </Stack>

          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: {
                xs: "1fr",
                lg: "2fr 1fr",
              },
              gap: 1.25,
              alignItems: "start",
            }}
          >
            <Paper
              variant="outlined"
              sx={{
                p: 1.5,
                borderRadius: 2,
                border: "1px solid rgba(2,48,71,0.10)",
              }}
            >
              <Typography
                sx={{
                  whiteSpace: "pre-wrap",
                  lineHeight: 1.85,
                  fontSize: "1rem",
                }}
              >
                {article.body}
              </Typography>
            </Paper>

            <Stack spacing={0.9}>
              <MetaBlock label="Created By" value={article.createdByName ?? "—"} />
              <MetaBlock label="Created At" value={formatDateTime(article.createdAt)} />
              <MetaBlock label="Last Modified By" value={article.lastModifiedByName ?? "—"} />
              <MetaBlock label="Last Updated" value={formatDateTime(article.updatedAt)} />
              <MetaBlock label="Approved By" value={article.approvedByName ?? "—"} />
              <MetaBlock label="Approved At" value={formatDateTime(article.approvedAt)} />
              <MetaBlock
                label="Submitted For Review"
                value={formatDateTime(article.agentSubmittedAt)}
              />
            </Stack>
          </Box>
        </Stack>
      </Paper>
    </Stack>
  );
}