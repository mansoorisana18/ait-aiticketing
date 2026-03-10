import React, { useMemo, useState } from "react";
import {
  Alert,
  Box,
  Button,
  Chip,
  Divider,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { formatDateTime } from "../../../utils/dateTime";
import type { CommentVisibility, TicketCommentResponseBean, UserRole } from "../../../api/types";
import { useCreateTicketComment, useTicketComments } from "../hooks";

export default function TicketDetailsComments({
  ticketId,
  role,
}: {
  ticketId: number;
  role: UserRole;
}) {
  const { data, isLoading, error } = useTicketComments(ticketId, true);
  const create = useCreateTicketComment(ticketId);

  const [body, setBody] = useState("");
  const [visibility, setVisibility] = useState<CommentVisibility>("PUBLIC");

  const comments = useMemo(() => data ?? [], [data]);
  const canPostInternal = role === "ADMIN" || role === "AGENT";

  const onSubmit = async () => {
    const trimmed = body.trim();
    if (!trimmed) return;

    await create.mutateAsync({
      body: trimmed,
      visibility: canPostInternal ? visibility : "PUBLIC",
    });

    setBody("");
    setVisibility("PUBLIC");
  };

  return (
    <Paper
      variant="outlined"
      sx={{
        p: 2,
        borderRadius: 2,
        border: "1px solid rgba(138,86,172,0.12)",
      }}
    >
      <Typography sx={{ fontWeight: 900, mb: 1 }}>Comments</Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          Failed to load comments.
        </Alert>
      )}

      <Box sx={{ maxHeight: 380, overflow: "auto", pr: 1, mb: 2 }}>
        <Stack spacing={1.25}>
          {isLoading && <Typography color="text.secondary">Loading comments…</Typography>}

          {!isLoading && comments.length === 0 && (
            <Typography color="text.secondary">No comments yet.</Typography>
          )}

          {comments.map((c: TicketCommentResponseBean) => (
            <Box
              key={c.commentId}
              sx={{
                p: 1.25,
                borderRadius: 2,
                border: "1px solid rgba(138,86,172,0.12)",
                bgcolor: c.visibility === "INTERNAL" ? "rgba(245, 158, 11, 0.08)" : "transparent",
              }}
            >
              <Stack direction="row" justifyContent="space-between" alignItems="center" spacing={1}>
                <Stack direction="row" spacing={1} alignItems="center">
                  <Typography sx={{ fontWeight: 800 }}>{c.authorName}</Typography>
                  <Chip
                    size="small"
                    label={c.visibility}
                    sx={{
                      height: 22,
                      fontWeight: 800,
                      ...(c.visibility === "INTERNAL"
                        ? {
                            bgcolor: "rgba(245, 158, 11, 0.18)",
                            color: "#92400E",
                            border: "1px solid rgba(245, 158, 11, 0.35)",
                          }
                        : {
                            bgcolor: "rgba(0, 180, 216, 0.12)",
                            color: "#0369A1",
                            border: "1px solid rgba(0, 180, 216, 0.25)",
                          }),
                    }}
                  />
                </Stack>

                <Typography variant="caption" color="text.secondary">
                  {formatDateTime(c.createdAt)}
                </Typography>
              </Stack>

              <Typography sx={{ whiteSpace: "pre-wrap", mt: 0.75 }}>{c.body}</Typography>
            </Box>
          ))}
        </Stack>
      </Box>

      <Divider sx={{ my: 2 }} />

      <Stack spacing={1.25}>
        <TextField
          label="Add a comment"
          value={body}
          onChange={(e) => setBody(e.target.value)}
          multiline
          minRows={3}
        />

        <FormControl fullWidth disabled={!canPostInternal}>
          <InputLabel id="comment-visibility-label">Visibility</InputLabel>
          <Select
            labelId="comment-visibility-label"
            label="Visibility"
            value={canPostInternal ? visibility : "PUBLIC"}
            onChange={(e) => setVisibility(e.target.value as CommentVisibility)}
          >
            <MenuItem value="PUBLIC">PUBLIC</MenuItem>
            <MenuItem value="INTERNAL">INTERNAL</MenuItem>
          </Select>
        </FormControl>

        <Button
          variant="contained"
          onClick={onSubmit}
          disabled={create.isPending || body.trim().length === 0}
        >
          {create.isPending ? "Posting…" : "Post Comment"}
        </Button>
      </Stack>
    </Paper>
  );
}