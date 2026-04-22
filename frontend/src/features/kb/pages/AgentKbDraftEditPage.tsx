import React from "react";
import { Alert, Box, Button, Paper, Stack, Typography } from "@mui/material";
import { useNavigate, useParams } from "react-router-dom";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import {
  useKbArticleById,
  useSubmitKbDraftForReviewAgent,
  useUpdateKbDraftAgent,
} from "../hooks";
import KbArticleEditorForm from "../components/KbArticleEditorForm";
import ArrowBackOutlinedIcon from "@mui/icons-material/ArrowBackOutlined";

export default function AgentKbDraftEditPage() {
  const { kbId } = useParams();
  const nav = useNavigate();

  const idNum = kbId ? Number(kbId) : null;
  const articleQuery = useKbArticleById(idNum, typeof idNum === "number");
  const updateMutation = useUpdateKbDraftAgent(idNum ?? -1);
  const submitReviewMutation = useSubmitKbDraftForReviewAgent(idNum ?? -1);

  const [title, setTitle] = React.useState("");
  const [body, setBody] = React.useState("");

  React.useEffect(() => {
    if (!articleQuery.data) return;
    setTitle(articleQuery.data.title ?? "");
    setBody(articleQuery.data.body ?? "");
  }, [articleQuery.data]);

  const saveDraft = async () => {
    if (!idNum) return;
    await updateMutation.mutateAsync({
      title: title.trim(),
      body: body.trim(),
    });
  };

  const submitForReview = async () => {
    if (!idNum) return;
    await submitReviewMutation.mutateAsync();
    nav(`/kb/${idNum}`, { replace: true });
  };

  if (!idNum) return <Typography>Invalid KB id.</Typography>;
  if (articleQuery.isLoading) return <LoadingSkeleton variant="detail" />;
  if (articleQuery.isError || !articleQuery.data) {
    return <Alert severity="error">Failed to load KB draft.</Alert>;
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
            Edit KB Draft
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.35 }}>
            Refine the draft generated from ticket resolution details before submitting it for admin review.
          </Typography>
        </Stack>
      </Paper>

      <KbArticleEditorForm
        title={title}
        body={body}
        onTitleChange={setTitle}
        onBodyChange={setBody}
        onSubmit={saveDraft}
        submitLabel="Save Draft"
        isSubmitting={updateMutation.isPending}
        footer={
          <Stack spacing={1}>
            <Button
              variant="contained"
              onClick={submitForReview}
              disabled={
                submitReviewMutation.isPending ||
                !title.trim() ||
                !body.trim()
              }
            >
              {submitReviewMutation.isPending ? "Submitting..." : "Submit for Review"}
            </Button>

            {updateMutation.isError ? (
              <Alert severity="error">Failed to save draft changes.</Alert>
            ) : null}

            {submitReviewMutation.isError ? (
              <Alert severity="error">Failed to submit draft for review.</Alert>
            ) : null}
          </Stack>
        }
      />
    </Stack>
  );
}