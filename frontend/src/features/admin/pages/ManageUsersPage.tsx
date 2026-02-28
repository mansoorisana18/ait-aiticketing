import React, { useMemo, useState } from "react";
import { Alert, Box, Button, Chip, Paper, Snackbar, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography, } from "@mui/material";
import { useAuth } from "../../../state/AuthContext";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import type { UserResponseBean } from "../../../api/types";
import { useMakeAgentByAdmin, useUsersForAdmin } from "../hooks";

function roleChipColor(role: string): "default" | "success" | "secondary" {
  if (role === "AGENT") return "success";
  if (role === "ADMIN") return "secondary";
  return "default";
}

export default function ManageUsersPage() {
  const { auth } = useAuth();
  const { data, isLoading } = useUsersForAdmin(Boolean(auth.token && auth.role === "ADMIN"));
  const makeAgent = useMakeAgentByAdmin();

  const [snack, setSnack] = useState<{ open: boolean; msg: string; severity: "success" | "error" }>({
    open: false,
    msg: "",
    severity: "success",
  });

  const users = useMemo(() => data ?? [], [data]);

  const onMakeAgent = async (userId: number) => {
    try {
      console.log("Make Agent clicked for:", userId);
      await makeAgent.mutateAsync(userId);
      setSnack({ open: true, msg: "User updated to AGENT.", severity: "success" });
    } catch (e: any) {
      setSnack({ open: true, msg: e?.message || "Failed to update user role.", severity: "error" });
    }
  };

  if (isLoading) return <LoadingSkeleton variant="list" count={6} />;

  return (
    <Stack spacing={2}>
      <Typography variant="h5" sx={{ fontWeight: 900 }}>
        Manage Users
      </Typography>

      <TableContainer component={Paper} variant="outlined">
        <Table>
          <TableHead>
            <TableRow>
              <TableCell sx={{ fontWeight: 900 }}>ID</TableCell>
              <TableCell sx={{ fontWeight: 900 }}>Name</TableCell>
              <TableCell sx={{ fontWeight: 900 }}>Email</TableCell>
              <TableCell sx={{ fontWeight: 900 }}>Role</TableCell>
              <TableCell sx={{ fontWeight: 900, width: 180 }}>Action</TableCell>
            </TableRow>
          </TableHead>

          <TableBody>
            {users.map((u: UserResponseBean) => {
              const disabled = u.role === "AGENT" || u.role === "ADMIN" || makeAgent.isPending;

              return (
                <TableRow key={u.userId} hover>
                  <TableCell>{u.userId}</TableCell>
                  <TableCell>
                    <Typography fontWeight={700}>{u.name}</Typography>
                  </TableCell>
                  <TableCell>{u.email}</TableCell>
                  <TableCell>
                    <Chip label={u.role} size="small" color={roleChipColor(u.role)} />
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: "flex", gap: 1 }}>
                      <Button
                        variant="contained"
                        size="small"
                        disabled={disabled}
                        onClick={() => onMakeAgent(u.userId)}
                      >
                        Make Agent
                      </Button>
                    </Box>
                  </TableCell>
                </TableRow>
              );
            })}

            {users.length === 0 && (
              <TableRow>
                <TableCell colSpan={5} sx={{ py: 4 }}>
                  <Typography color="text.secondary">No users found.</Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Snackbar open={snack.open} autoHideDuration={3500} onClose={() => setSnack((s) => ({ ...s, open: false }))}>
        <Alert
          onClose={() => setSnack((s) => ({ ...s, open: false }))}
          severity={snack.severity}
          variant="filled"
        >
          {snack.msg}
        </Alert>
      </Snackbar>
    </Stack>
  );
}