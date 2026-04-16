import { Alert, Box, Button, Paper, Stack, Typography } from "@mui/material";

export default function KbSuggestionPanel({
  title,
  preview,
  isSubmitting,
  onViewArticle,
  onAccept,
  onReject,
  isError,
  successMessage,
}: {
  title?: string | null;
  preview?: string | null;
  isSubmitting?: boolean;
  onViewArticle: () => void;
  onAccept: () => Promise<void> | void;
  onReject: () => Promise<void> | void;
  isError?: boolean;
  successMessage?: string | null;
}) {
  return (
    <Paper
      variant="outlined"
      sx={{
        p: 1.35,
        borderRadius: 2,
        border: "1px solid rgba(2,48,71,0.10)",
        boxShadow: "0 2px 10px rgba(2,48,71,0.05)",
      }}
    >
      <Stack spacing={1}>
        <Stack spacing={0.3}>
          <Typography sx={{ fontWeight: 1000 }}>Suggested Solution</Typography>
          <Typography variant="body2" color="text.secondary">
            The system found a knowledge base article that may solve your issue. Please review it
            and let us know whether it resolved the problem.
          </Typography>
        </Stack>

        <Box
          sx={{
            p: 1,
            borderRadius: 1.5,
            bgcolor: "rgba(255,255,255,0.72)",
            border: "1px solid rgba(2,48,71,0.08)",
          }}
        >
          <Stack spacing={0.35}>
            <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 800 }}>
              Suggested article
            </Typography>
            <Typography sx={{ fontWeight: 800 }}>{title ?? "KB Article"}</Typography>
            <Typography variant="body2" color="text.secondary" sx={{ whiteSpace: "pre-wrap" }}>
              {preview ?? "No preview available."}
            </Typography>
          </Stack>
        </Box>

        <Stack direction={{ xs: "column", sm: "row" }} spacing={0.8}>
          <Button variant="outlined" onClick={onViewArticle} disabled={isSubmitting}>
            View Full Article
          </Button>

          <Button variant="contained" onClick={onAccept} disabled={isSubmitting}>
            This solved my issue
          </Button>

          <Button variant="outlined" color="inherit" onClick={onReject} disabled={isSubmitting}>
            I still need help
          </Button>
        </Stack>

        {isError && <Alert severity="error">Failed to update your KB suggestion response.</Alert>}
        {successMessage ? <Alert severity="success">{successMessage}</Alert> : null}
      </Stack>
    </Paper>
  );
}