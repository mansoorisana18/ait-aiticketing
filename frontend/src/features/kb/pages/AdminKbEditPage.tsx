import React from "react";
import { Alert, Box, Button, Paper, Stack, Typography } from "@mui/material";
import { useNavigate, useParams } from "react-router-dom";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import { useKbArticleById, useUpdateKbArticleAdmin } from "../hooks";
import KbArticleEditorForm from "../components/KbArticleEditorForm";
import ArrowBackOutlinedIcon from "@mui/icons-material/ArrowBackOutlined";

export default function AdminKbEditPage() {
  const { kbId } = useParams();
  const nav = useNavigate();

  const idNum = kbId ? Number(kbId) : null;
  const articleQuery = useKbArticleById(idNum, typeof idNum === "number");
  const updateMutation = useUpdateKbArticleAdmin(idNum ?? -1);

  const [title, setTitle] = React.useState("");
  const [body, setBody] = React.useState("");

  React.useEffect(() => {
    if (!articleQuery.data) return;
    setTitle(articleQuery.data.title ?? "");
    setBody(articleQuery.data.body ?? "");
  }, [articleQuery.data]);

  const submit = async () => {
    if (!idNum) return;
    const res = await updateMutation.mutateAsync({
      title: title.trim(),
      body: body.trim(),
    });

    nav(`/kb/${res.kbId}`, { replace: true });
  };

  if (!idNum) return <Typography>Invalid KB id.</Typography>;
  if (articleQuery.isLoading) return <LoadingSkeleton variant="detail" />;
  if (articleQuery.isError || !articleQuery.data) {
    return <Alert severity="error">Failed to load KB article for editing.</Alert>;
  }

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
            Edit KB Article
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.35 }}>
            Update article content, title, and structure before publishing or maintaining it.
          </Typography>
        </Stack>
      </Paper>

      <KbArticleEditorForm
        title={title}
        body={body}
        onTitleChange={setTitle}
        onBodyChange={setBody}
        onSubmit={submit}
        submitLabel="Save Changes"
        isSubmitting={updateMutation.isPending}
        footer={
          updateMutation.isError ? (
            <Alert severity="error">Failed to update KB article.</Alert>
          ) : null
        }
      />
    </Stack>
  );
}