import React from "react";
import { Box, Skeleton, Stack, Card, CardContent } from "@mui/material";

type LoadingSkeletonProps = {
  variant?: "list" | "detail";
  count?: number;
};

export default function LoadingSkeleton({
  variant = "list",
  count = 3,
}: LoadingSkeletonProps) {
  if (variant === "detail") {
    return (
      <Box sx={{ maxWidth: 900 }}>
        <Skeleton variant="text" height={40} width="60%" />
        <Skeleton variant="text" height={24} width="30%" sx={{ mb: 2 }} />
        <Skeleton variant="rectangular" height={120} />
        <Skeleton variant="rectangular" height={80} sx={{ mt: 2 }} />
      </Box>
    );
  }

  // Default: list
  return (
    <Stack spacing={2}>
      {Array.from({ length: count }).map((_, index) => (
        <Card key={index} variant="outlined">
          <CardContent>
            <Skeleton variant="text" height={30} width="50%" />
            <Skeleton variant="text" height={20} width="80%" />
            <Skeleton variant="text" height={20} width="70%" />
          </CardContent>
        </Card>
      ))}
    </Stack>
  );
}