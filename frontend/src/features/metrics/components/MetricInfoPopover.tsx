import React from "react";
import { Box, IconButton, Popover, Stack, Typography } from "@mui/material";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";

export default function MetricInfoPopover({
  title,
  summary,
  interpretation,
  calculation,
}: {
  title: string;
  summary: string;
  interpretation: string;
  calculation: string;
}) {
  const [anchorEl, setAnchorEl] = React.useState<HTMLElement | null>(null);

  return (
    <>
      <IconButton
        size="small"
        onClick={(e) => setAnchorEl(e.currentTarget)}
        sx={{ p: 0.25, color: "text.secondary" }}
      >
        <InfoOutlinedIcon fontSize="inherit" />
      </IconButton>

      <Popover
        open={Boolean(anchorEl)}
        anchorEl={anchorEl}
        onClose={() => setAnchorEl(null)}
        anchorOrigin={{ vertical: "bottom", horizontal: "left" }}
        slotProps={{
          paper: {
            sx: {
              borderRadius: 2,
              maxWidth: 320,
            },
          },
        }}
      >
        <Box sx={{ p: 1.5 }}>
          <Stack spacing={1}>
            <Typography sx={{ fontWeight: 900 }}>{title}</Typography>

            <Typography variant="body2" color="text.primary">
              {summary}
            </Typography>

            <Typography variant="body2" color="text.secondary">
              {interpretation}
            </Typography>

            <Box
              sx={{
                px: 1,
                py: 0.85,
                borderRadius: 1.5,
                bgcolor: "rgba(2,48,71,0.04)",
                border: "1px solid rgba(2,48,71,0.08)",
              }}
            >
              <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 800, display: "block", mb: 0.2 }}>
                Calculation
              </Typography>
              <Typography variant="body2">{calculation}</Typography>
            </Box>
          </Stack>
        </Box>
      </Popover>
    </>
  );
}