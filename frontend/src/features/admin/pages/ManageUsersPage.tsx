import React, { useMemo, useState } from "react";
import {
  Alert,
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Paper,
  Snackbar,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import { useAuth } from "../../../state/AuthContext";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import type { Department, UserResponseBean } from "../../../api/types";
import { DEPARTMENTS } from "../../../api/types";
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

  const [selectedUser, setSelectedUser] = useState<UserResponseBean | null>(null);
  const [department, setDepartment] = useState<Department>("TECHNICAL SUPPORT");

  const users = useMemo(() => data ?? [], [data]);

  const openPromoteDialog = (user: UserResponseBean) => {
    setSelectedUser(user);
    setDepartment("TECHNICAL SUPPORT");
  };

  const closePromoteDialog = () => {
    setSelectedUser(null);
  };

  const onMakeAgent = async () => {
    if (!selectedUser) return;

    try {
      await makeAgent.mutateAsync({
        userId: selectedUser.userId,
        body: { department },
      });
      setSnack({ open: true, msg: "User updated to AGENT.", severity: "success" });
      closePromoteDialog();
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
              <TableCell sx={{ fontWeight: 900 }}>Department</TableCell>
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
                  <TableCell>{u.department ?? "-"}</TableCell>
                  <TableCell>
                    <Box sx={{ display: "flex", gap: 1 }}>
                      <Button
                        variant="contained"
                        size="small"
                        disabled={disabled}
                        onClick={() => openPromoteDialog(u)}
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
                <TableCell colSpan={6} sx={{ py: 4 }}>
                  <Typography color="text.secondary">No users found.</Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={Boolean(selectedUser)} onClose={closePromoteDialog} fullWidth maxWidth="sm">
        <DialogTitle>Promote User to Agent</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            <Typography variant="body2">
              <strong>Name:</strong> {selectedUser?.name}
            </Typography>
            <Typography variant="body2">
              <strong>Email:</strong> {selectedUser?.email}
            </Typography>

            <TextField
              select
              label="Department"
              value={department}
              onChange={(e) => setDepartment(e.target.value as Department)}
              fullWidth
            >
              {DEPARTMENTS.map((d) => (
                <MenuItem key={d} value={d}>
                  {d}
                </MenuItem>
              ))}
            </TextField>
          </Stack>
        </DialogContent>

        <DialogActions>
          <Button onClick={closePromoteDialog}>Cancel</Button>
          <Button variant="contained" onClick={onMakeAgent} disabled={makeAgent.isPending}>
            {makeAgent.isPending ? "Updating..." : "Confirm"}
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={snack.open}
        autoHideDuration={3500}
        onClose={() => setSnack((s) => ({ ...s, open: false }))}
      >
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