import React from "react";
import { Box, Chip, Paper, Stack, Typography } from "@mui/material";

export default function MetricsSection({
  title,
  description,
  processSteps,
  highlight,
  cards,
}: {
  title: string;
  description: string;
  processSteps?: string[];
  highlight: React.ReactNode;
  cards: React.ReactNode;
}) {
  return (
    <Paper
      variant="outlined"
      sx={{
        p: 1.75,
        borderRadius: 2,
      }}
    >
      <Stack spacing={1.4}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 900 }}>
            {title}
          </Typography>

          {processSteps && processSteps.length > 0 && (
            <Stack
              direction="row"
              spacing={0.7}
              useFlexGap
              flexWrap="wrap"
              sx={{ mt: 0.6, mb: 0.45 }}
            >
              {processSteps.map((step) => (
                <Chip
                  key={step}
                  label={step}
                  size="small"
                  sx={{
                    fontWeight: 700,
                    bgcolor: "rgba(2,48,71,0.06)",
                    color: "text.primary",
                    border: "1px solid rgba(2,48,71,0.10)",
                  }}
                />
              ))}
            </Stack>
          )}

          <Typography variant="body2" color="text.secondary">
            {description}
          </Typography>
        </Box>

        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: { xs: "1fr", xl: "0.95fr 1.45fr" },
            gap: 1.25,
            alignItems: "start",
          }}
        >
          <Box>{highlight}</Box>
          <Box>{cards}</Box>
        </Box>
      </Stack>
    </Paper>
  );
}