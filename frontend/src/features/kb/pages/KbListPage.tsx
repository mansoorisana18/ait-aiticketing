import React from "react";
import {
  Alert,
  Box,
  InputAdornment,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import MenuBookOutlinedIcon from "@mui/icons-material/MenuBookOutlined";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../state/AuthContext";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import KbArticleCard from "../components/KbArticleCard";
import { usePublishedKbArticles } from "../hooks";

export default function KbListPage() {
  const { auth } = useAuth();
  const nav = useNavigate();
  const { data, isLoading, isError } = usePublishedKbArticles(
    Boolean(auth.token && (auth.role === "AGENT" || auth.role === "ADMIN"))
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
        a.createdByName ?? "",
        a.status ?? "",
      ]
        .join(" ")
        .toLowerCase();

      return haystack.includes(q);
    });
  }, [data, search]);

  if (isLoading) return <LoadingSkeleton variant="list" count={6} />;
  if (isError) return <Alert severity="error">Failed to load published knowledge base articles.</Alert>;

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
              <Stack direction="row" spacing={0.9} alignItems="center">
                <Box
                  sx={{
                    width: 36,
                    height: 36,
                    borderRadius: 2,
                    display: "grid",
                    placeItems: "center",
                    bgcolor: "rgba(33,158,188,0.12)",
                    color: "primary.main",
                  }}
                >
                  <MenuBookOutlinedIcon />
                </Box>
                <Box>
                  <Typography variant="h4" sx={{ fontWeight: 900 }}>
                    Knowledge Base
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 0.25 }}>
                    Browse published knowledge articles and reusable resolutions.
                  </Typography>
                </Box>
              </Stack>
            </Box>

            <TextField
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search articles..."
              size="small"
              sx={{ width: { xs: "100%", md: 320 } }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon fontSize="small" />
                  </InputAdornment>
                ),
              }}
            />
          </Stack>

          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: {
                xs: "1fr",
                md: "repeat(3, minmax(0, 1fr))",
              },
              gap: 1,
            }}
          >
            <Paper
              variant="outlined"
              sx={{
                p: 1.15,
                borderRadius: 2,
                border: "1px solid rgba(2,48,71,0.08)",
              }}
            >
              <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 800 }}>
                Published Articles
              </Typography>
              <Typography variant="h5" sx={{ fontWeight: 900, mt: 0.25 }}>
                {filtered.length}
              </Typography>
            </Paper>

            <Paper
              variant="outlined"
              sx={{
                p: 1.15,
                borderRadius: 2,
                border: "1px solid rgba(2,48,71,0.08)",
              }}
            >
              <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 800 }}>
                Search Query
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 900, mt: 0.25 }}>
                {search.trim() || "All Articles"}
              </Typography>
            </Paper>

            <Paper
              variant="outlined"
              sx={{
                p: 1.15,
                borderRadius: 2,
                border: "1px solid rgba(2,48,71,0.08)",
              }}
            >
              <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 800 }}>
                Audience
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 900, mt: 0.25 }}>
                {auth.role}
              </Typography>
            </Paper>
          </Box>
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
          <Typography sx={{ fontWeight: 800, mb: 0.35 }}>No knowledge articles found</Typography>
          <Typography variant="body2" color="text.secondary">
            Try a different search term or clear the filter to see all published articles.
          </Typography>
        </Paper>
      ) : (
        <Stack spacing={1.1}>
          {filtered.map((article) => (
            <KbArticleCard
              key={article.kbId}
              article={article}
              onClick={() => nav(`/kb/${article.kbId}`)}
              showStatus={true}
              showSourceTicket={true}
            />
          ))}
        </Stack>
      )}
    </Stack>
  );
}