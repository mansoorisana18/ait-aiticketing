import {
  Alert,
  Dialog,
  DialogContent,
  DialogTitle,
  IconButton,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import { useKbArticleById } from "../../kb/hooks";
import { formatDateTime } from "../../../utils/dateTime";

export default function KbArticleDialog({
  kbId,
  open,
  onClose,
  titleOverride,
}: {
  kbId: number | null;
  open: boolean;
  onClose: () => void;
  titleOverride?: string;
}) {
  const query = useKbArticleById(kbId, open && typeof kbId === "number");

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth scroll="paper">
      <DialogTitle sx={{ pr: 6, position: "relative" }}>
        {titleOverride ?? "Knowledge Base Article"}
        <IconButton
          aria-label="close"
          onClick={onClose}
          sx={{ position: "absolute", right: 10, top: 10, color: "inherit" }}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent sx={{ px: 1.5, pt: 1.5, pb: 1.5 }}>
        {query.isLoading ? (
          <LoadingSkeleton variant="list" count={3} />
        ) : query.isError ? (
          <Alert severity="error">Failed to load KB article.</Alert>
        ) : query.data ? (
          <Paper variant="outlined" sx={{ p: 1.5, borderRadius: 2 }}>
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
                {query.data.updatedAt ? (
                  <Typography variant="caption" color="text.secondary">
                    Updated: {formatDateTime(query.data.updatedAt)}
                  </Typography>
                ) : null}
              </Stack>

              <Typography sx={{ whiteSpace: "pre-wrap", lineHeight: 1.7 }}>
                {query.data.body}
              </Typography>
            </Stack>
          </Paper>
        ) : (
          <Alert severity="info">No KB article found.</Alert>
        )}
      </DialogContent>
    </Dialog>
  );
}