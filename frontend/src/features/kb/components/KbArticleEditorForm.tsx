import React from "react";
import {
  Box,
  Button,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";

export default function KbArticleEditorForm({
  title,
  body,
  onTitleChange,
  onBodyChange,
  onSubmit,
  submitLabel,
  isSubmitting = false,
  footer,
}: {
  title: string;
  body: string;
  onTitleChange: (value: string) => void;
  onBodyChange: (value: string) => void;
  onSubmit: () => void;
  submitLabel: string;
  isSubmitting?: boolean;
  footer?: React.ReactNode;
}) {
  return (
    <Stack spacing={1.25}>
      <Paper
        variant="outlined"
        sx={{
          p: 1.4,
          borderRadius: 2,
          border: "1px solid rgba(2,48,71,0.10)",
          boxShadow: "0 2px 10px rgba(2,48,71,0.05)",
        }}
      >
        <Stack spacing={1.1}>
          <TextField
            label="Article Title"
            value={title}
            onChange={(e) => onTitleChange(e.target.value)}
            fullWidth
            size="small"
            inputProps={{ maxLength: 200 }}
            helperText={`${title.length}/200`}
          />

          <TextField
            label="Article Body"
            value={body}
            onChange={(e) => onBodyChange(e.target.value)}
            multiline
            minRows={18}
            fullWidth
          />

          <Stack
            direction={{ xs: "column", sm: "row" }}
            justifyContent="space-between"
            spacing={1}
            alignItems={{ xs: "flex-start", sm: "center" }}
          >
            <Typography variant="body2" color="text.secondary">
              Keep the article clear, reusable, and focused on the issue-resolution steps.
            </Typography>

            <Button
              variant="contained"
              onClick={onSubmit}
              disabled={isSubmitting || !title.trim() || !body.trim()}
            >
              {isSubmitting ? "Saving..." : submitLabel}
            </Button>
          </Stack>
        </Stack>
      </Paper>

      {footer ? <Box>{footer}</Box> : null}
    </Stack>
  );
}