import {
  Alert,
  Button,
  Paper,
  Stack,
  Typography,
} from "@mui/material";

export default function KbReviewDecisionPanel({
  onApprove,
  onReject,
  isSubmitting = false,
  isError = false,
}: {
  onApprove: () => void;
  onReject: () => void;
  isSubmitting?: boolean;
  isError?: boolean;
}) {
  return (
    <Paper
      variant="outlined"
      sx={{
        p: 1.25,
        borderRadius: 2,
        border: "1px solid rgba(2,48,71,0.10)",
        boxShadow: "0 2px 10px rgba(2,48,71,0.05)",
      }}
    >
      <Stack spacing={1}>
        <Typography sx={{ fontWeight: 900 }}>Review Decision</Typography>
        <Typography variant="body2" color="text.secondary">
          Approve this draft to publish it as knowledge base content, or reject it if it is not ready.
        </Typography>

        <Stack direction={{ xs: "column", sm: "row" }} spacing={0.9}>
          <Button
            variant="contained"
            onClick={onApprove}
            disabled={isSubmitting}
          >
            {isSubmitting ? "Submitting..." : "Approve"}
          </Button>

          <Button
            variant="outlined"
            color="inherit"
            onClick={onReject}
            disabled={isSubmitting}
          >
            {isSubmitting ? "Submitting..." : "Reject"}
          </Button>
        </Stack>

        {isError ? (
          <Alert severity="error">Failed to submit review decision.</Alert>
        ) : null}
      </Stack>
    </Paper>
  );
}