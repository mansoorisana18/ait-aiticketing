import { useParams } from "react-router-dom";
import { Alert, Paper, Stack, Typography } from "@mui/material";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import { useKbArticle } from "../../tickets/hooks";
import { formatDateTime } from "../../../utils/dateTime";

export default function KbDetailsPage() {
  const { kbId } = useParams();
  const idNum = kbId ? Number(kbId) : null;

  const query = useKbArticle(idNum, typeof idNum === "number");

  if (!idNum) return <Typography>Invalid KB id.</Typography>;
  if (query.isLoading) return <LoadingSkeleton variant="detail" />;
  if (query.isError) return <Alert severity="error">Failed to load KB article.</Alert>;
  if (!query.data) return <Typography>KB article not found.</Typography>;

  return (
    <Stack spacing={1.25}>
      <Typography variant="h5" sx={{ fontWeight: 900 }}>
        Knowledge Base Article
      </Typography>

      <Paper
        variant="outlined"
        sx={{
          p: 1.5,
          borderRadius: 2,
          border: "1px solid rgba(2,48,71,0.10)",
          boxShadow: "0 2px 10px rgba(2,48,71,0.05)",
        }}
      >
        <Stack spacing={1}>
          <Typography variant="h6" sx={{ fontWeight: 900 }}>
            {query.data.title}
          </Typography>

          <Stack direction="row" spacing={1.2} flexWrap="wrap">
            {query.data.status ? (
              <Typography variant="caption" color="text.secondary">
                Status: {query.data.status}
              </Typography>
            ) : null}

            {query.data.sourceTicketId ? (
              <Typography variant="caption" color="text.secondary">
                Source: Ticket #{query.data.sourceTicketId}
              </Typography>
            ) : null}

            {query.data.updatedAt ? (
              <Typography variant="caption" color="text.secondary">
                Updated: {formatDateTime(query.data.updatedAt)}
              </Typography>
            ) : null}
          </Stack>

          <Typography sx={{ whiteSpace: "pre-wrap", lineHeight: 1.7 }}>
            {query.data.content}
          </Typography>
        </Stack>
      </Paper>
    </Stack>
  );
}