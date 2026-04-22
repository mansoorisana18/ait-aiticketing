import {
  Box,
  Chip,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import { alpha } from "@mui/material/styles";
import type { KbArticleResponseBean } from "../../../api/types";
import { formatDateTime } from "../../../utils/dateTime";

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

export default function KbArticleCard({
  article,
  onClick,
  showStatus = true,
  showSourceTicket = true,
}: {
  article: KbArticleResponseBean;
  onClick?: () => void;
  showStatus?: boolean;
  showSourceTicket?: boolean;
}) {
  const preview =
    article.body?.length > 220
      ? `${article.body.slice(0, 220)}...`
      : article.body ?? "";

  return (
    <Paper
      variant="outlined"
      onClick={onClick}
      sx={{
        p: 1.35,
        borderRadius: 2,
        border: "1px solid rgba(2,48,71,0.10)",
        boxShadow: "0 2px 10px rgba(2,48,71,0.05)",
        cursor: onClick ? "pointer" : "default",
        transition: "transform 120ms ease, box-shadow 120ms ease, border-color 120ms ease",
        "&:hover": onClick
          ? {
              transform: "translateY(-1px)",
              boxShadow: "0 6px 16px rgba(2,48,71,0.08)",
              borderColor: "rgba(33,158,188,0.26)",
            }
          : undefined,
      }}
    >
      <Stack spacing={0.9}>
        <Stack
          direction={{ xs: "column", sm: "row" }}
          justifyContent="space-between"
          alignItems={{ xs: "flex-start", sm: "flex-start" }}
          spacing={0.8}
        >
          <Box sx={{ minWidth: 0, flex: 1 }}>
            <Typography
              variant="h6"
              sx={{
                fontWeight: 900,
                lineHeight: 1.2,
                mb: 0.25,
                wordBreak: "break-word",
              }}
            >
              {article.title}
            </Typography>

            <Stack direction="row" spacing={0.75} flexWrap="wrap" useFlexGap>
              <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 700 }}>
                KB #{article.kbId}
              </Typography>

              {showSourceTicket && article.sourceTicketId ? (
                <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 700 }}>
                  Source Ticket #{article.sourceTicketId}
                </Typography>
              ) : null}

              {article.isAiGenerated != null ? (
                <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 700 }}>
                  {article.isAiGenerated ? "AI Generated" : "Human Authored"}
                </Typography>
              ) : null}
            </Stack>
          </Box>

          {showStatus ? (
            <Chip
              size="small"
              label={article.status ?? "—"}
              sx={{ fontWeight: 800, ...statusChipSx(article.status) }}
            />
          ) : null}
        </Stack>

        <Typography
          variant="body2"
          sx={{
            color: "text.secondary",
            lineHeight: 1.6,
            whiteSpace: "pre-wrap",
            display: "-webkit-box",
            WebkitLineClamp: 3,
            WebkitBoxOrient: "vertical",
            overflow: "hidden",
          }}
        >
          {preview || "No article content available."}
        </Typography>

        <Stack
          direction={{ xs: "column", sm: "row" }}
          justifyContent="space-between"
          alignItems={{ xs: "flex-start", sm: "center" }}
          spacing={0.7}
          sx={{
            pt: 0.6,
            borderTop: "1px solid rgba(2,48,71,0.06)",
          }}
        >
          <Stack direction="row" spacing={0.7} flexWrap="wrap" useFlexGap>
            {article.createdByName ? (
              <Chip
                size="small"
                label={`Author: ${article.createdByName}`}
                sx={{
                  fontWeight: 700,
                  bgcolor: alpha("#023047", 0.05),
                  color: "#023047",
                }}
              />
            ) : null}

            {article.approvedByName ? (
              <Chip
                size="small"
                label={`Approved by: ${article.approvedByName}`}
                sx={{
                  fontWeight: 700,
                  bgcolor: alpha("#15803D", 0.08),
                  color: "#15803D",
                }}
              />
            ) : null}
          </Stack>

          <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 700 }}>
            Updated {formatDateTime(article.updatedAt)}
          </Typography>
        </Stack>
      </Stack>
    </Paper>
  );
}