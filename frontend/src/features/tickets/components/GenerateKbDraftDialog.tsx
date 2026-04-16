import {
  Alert,
  Button,
  Checkbox,
  Dialog,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  IconButton,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import type { TicketCommentResponseBean } from "../../../api/types";
import { formatDateTime } from "../../../utils/dateTime";

export default function GenerateKbDraftDialog({
  open,
  onClose,
  comments,
  selectedIds,
  onSelectedIdsChange,
  onGenerate,
  isSubmitting,
  isError,
}: {
  open: boolean;
  onClose: () => void;
  comments: TicketCommentResponseBean[];
  selectedIds: number[];
  onSelectedIdsChange: (ids: number[]) => void;
  onGenerate: () => Promise<void> | void;
  isSubmitting?: boolean;
  isError?: boolean;
}) {
  const publicComments = comments.filter((c) => c.visibility === "PUBLIC");

  const toggle = (commentId: number) => {
    if (selectedIds.includes(commentId)) {
      onSelectedIdsChange(selectedIds.filter((id) => id !== commentId));
    } else {
      onSelectedIdsChange([...selectedIds, commentId]);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth scroll="paper">
      <DialogTitle sx={{ pr: 6, position: "relative" }}>
        Generate KB Draft
        <IconButton
          aria-label="close"
          onClick={onClose}
          sx={{ position: "absolute", right: 10, top: 10, color: "inherit" }}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent sx={{ px: 1.5, pt: 1.5, pb: 1.5 }}>
        <Stack spacing={1.1}>
          <Typography variant="body2" color="text.secondary">
            Select the PUBLIC comments that should be used as source material for the AI-generated
            KB draft.
          </Typography>

          {publicComments.length === 0 ? (
            <Alert severity="info">
              No PUBLIC comments are available yet. Add public resolution details before generating a
              KB draft.
            </Alert>
          ) : (
            <Stack spacing={0.9}>
              {publicComments.map((comment) => (
                <Paper key={comment.commentId} variant="outlined" sx={{ p: 1, borderRadius: 1.5 }}>
                  <Stack spacing={0.45}>
                    <Stack direction="row" justifyContent="space-between" spacing={1}>
                      <Typography sx={{ fontWeight: 800 }}>{comment.authorName}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        {formatDateTime(comment.createdAt)}
                      </Typography>
                    </Stack>

                    <Typography variant="body2" sx={{ whiteSpace: "pre-wrap" }}>
                      {comment.body}
                    </Typography>

                    <FormControlLabel
                      control={
                        <Checkbox
                          checked={selectedIds.includes(comment.commentId)}
                          onChange={() => toggle(comment.commentId)}
                        />
                      }
                      label="Use this comment in KB draft generation"
                    />
                  </Stack>
                </Paper>
              ))}
            </Stack>
          )}

          <Stack direction={{ xs: "column", sm: "row" }} spacing={0.8}>
            <Button variant="outlined" onClick={onClose} disabled={isSubmitting}>
              Cancel
            </Button>
            <Button
              variant="contained"
              onClick={onGenerate}
              disabled={isSubmitting || selectedIds.length === 0}
            >
              {isSubmitting ? "Generating..." : "Generate Draft"}
            </Button>
          </Stack>

          {isError && <Alert severity="error">Failed to request KB draft generation.</Alert>}
        </Stack>
      </DialogContent>
    </Dialog>
  );
}