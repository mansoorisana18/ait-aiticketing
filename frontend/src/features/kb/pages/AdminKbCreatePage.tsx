import React from "react";
import { Alert, Box, Button, Paper, Stack, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { useCreateKbArticleAdmin } from "../hooks";
import KbArticleEditorForm from "../components/KbArticleEditorForm";
import ArrowBackOutlinedIcon from "@mui/icons-material/ArrowBackOutlined";

export default function AdminKbCreatePage() {
  const nav = useNavigate();
  const mutation = useCreateKbArticleAdmin();

  const [title, setTitle] = React.useState("");
  const [body, setBody] = React.useState("");

  const submit = async () => {
    const res = await mutation.mutateAsync({
      title: title.trim(),
      body: body.trim(),
    });

    nav(`/kb/${res.kbId}`, { replace: true });
  };

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
            Create KB Article
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.35 }}>
            Create a new knowledge base article directly as an administrator.
          </Typography>
        </Stack>
      </Paper>

      <KbArticleEditorForm
        title={title}
        body={body}
        onTitleChange={setTitle}
        onBodyChange={setBody}
        onSubmit={submit}
        submitLabel="Create Article"
        isSubmitting={mutation.isPending}
        footer={
          mutation.isError ? (
            <Alert severity="error">Failed to create KB article.</Alert>
          ) : null
        }
      />
    </Stack>
  );
}