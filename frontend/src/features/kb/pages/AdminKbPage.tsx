import React from "react";
import {
  Alert,
  Box,
  Button,
  InputAdornment,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import AddCircleOutlineIcon from "@mui/icons-material/AddCircleOutline";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../state/AuthContext";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import { useAllKbArticlesAdmin } from "../hooks";
import KbArticleCard from "../components/KbArticleCard";

export default function AdminKbPage() {
  const { auth } = useAuth();
  const nav = useNavigate();
  const { data, isLoading, isError } = useAllKbArticlesAdmin(
    Boolean(auth.token && auth.role === "ADMIN")
  );

  const [search, setSearch] = React.useState("");

  const filtered = React.useMemo(() => {
    const articles = data ?? [];
    const q = search.trim().toLowerCase();

    if (!q) return articles;

    return articles.filter((a) => {
      const haystack = [
        a.title ?? "",
        a.body ?? "",
        a.status ?? "",
        a.createdByName ?? "",
        a.lastModifiedByName ?? "",
      ]
        .join(" ")
        .toLowerCase();

      return haystack.includes(q);
    });
  }, [data, search]);

  if (isLoading) return <LoadingSkeleton variant="list" count={6} />;
  if (isError) return <Alert severity="error">Failed to load KB library.</Alert>;

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
        <Stack spacing={1.2}>
          <Stack
            direction={{ xs: "column", md: "row" }}
            justifyContent="space-between"
            alignItems={{ xs: "flex-start", md: "center" }}
            spacing={1}
          >
            <Box>
              <Typography variant="h4" sx={{ fontWeight: 900 }}>
                KB Library
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 0.25 }}>
                Manage all knowledge base articles, including drafts, reviewed articles, and published content.
              </Typography>
            </Box>

            <Stack direction={{ xs: "column", sm: "row" }} spacing={0.8}>
              <TextField
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search KB library..."
                size="small"
                sx={{ width: { xs: "100%", sm: 280 } }}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon fontSize="small" />
                    </InputAdornment>
                  ),
                }}
              />

              <Button
                variant="contained"
                startIcon={<AddCircleOutlineIcon />}
                onClick={() => nav("/admin/kb/new")}
              >
                New Article
              </Button>
            </Stack>
          </Stack>
        </Stack>
      </Paper>

      {filtered.length === 0 ? (
        <Paper
          variant="outlined"
          sx={{
            p: 2,
            borderRadius: 2,
            borderStyle: "dashed",
            textAlign: "center",
          }}
        >
          <Typography sx={{ fontWeight: 800, mb: 0.35 }}>No KB articles found</Typography>
          <Typography variant="body2" color="text.secondary">
            Try a different search term or create a new article.
          </Typography>
        </Paper>
      ) : (
        <Stack spacing={1.1}>
          {filtered.map((article) => (
            <KbArticleCard
              key={article.kbId}
              article={article}
              onClick={() => {
                const status = (article.status ?? "").toUpperCase();
                if (status === "DRAFT" || status === "REJECTED") {
                  nav(`/admin/kb/${article.kbId}/edit`);
                } else if (status === "IN_REVIEW") {
                  nav(`/admin/kb/review?kbId=${article.kbId}`);
                } else {
                  nav(`/kb/${article.kbId}`);
                }
              }}
              showStatus
              showSourceTicket
            />
          ))}
        </Stack>
      )}
    </Stack>
  );
}