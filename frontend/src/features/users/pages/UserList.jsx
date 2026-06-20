import { useEffect, useState, useRef } from 'react';
import { userApi } from '../userApi';
import { usePermission } from '../../../hooks/usePermission';
import {
  Box, Typography, Alert, CircularProgress, Paper,
  Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, TablePagination, Chip,
} from '@mui/material';

const roleColors = {
  SUPER_ADMIN: 'error',
  OWNER: 'secondary',
  BRANCH_MANAGER: 'primary',
  SALES_EXECUTIVE: 'info',
  CASHIER: 'default',
  PURCHASE_MANAGER: 'warning',
  WAREHOUSE_MANAGER: 'success',
  ACCOUNTANT: 'default',
};

export default function UserList() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);

  const canView = usePermission('USER_VIEW');
  const isMounted = useRef(true);
  const fetchInProgress = useRef(false);

  useEffect(() => {
    isMounted.current = true;
    return () => { isMounted.current = false; };
  }, []);

  useEffect(() => {
    if (!canView || fetchInProgress.current) return;
    const fetchUsers = async () => {
      fetchInProgress.current = true;
      setLoading(true);
      setError(null);
      try {
        const response = await userApi.listUsers(page, rowsPerPage);
        const data = response.data; // ✅ unwrap ApiResponse -> PageResponse
        if (isMounted.current) {
          setUsers(data.content || []);
          setTotalElements(data.totalElements || 0);
        }
      } catch (err) {
        if (isMounted.current) {
          setError(err.response?.data?.message || 'Failed to load users');
        }
      } finally {
        if (isMounted.current) setLoading(false);
        fetchInProgress.current = false;
      }
    };
    fetchUsers();
  }, [page, rowsPerPage, canView]);

  if (!canView) {
    return (
      <Alert severity="warning">
        You don't have permission to view users. (USER_VIEW required)
      </Alert>
    );
  }

  if (loading && users.length === 0) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) return <Alert severity="error">{error}</Alert>;

  return (
    <Box>
      <Typography variant="h4" gutterBottom fontWeight={700}>
        Users
      </Typography>
      <Paper elevation={2} sx={{ borderRadius: 2 }}>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow sx={{ '& th': { fontWeight: 700, bgcolor: 'action.hover' } }}>
                <TableCell>#</TableCell>
                <TableCell>Username</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Role</TableCell>
                <TableCell>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 4 }}>
                    No users found.
                  </TableCell>
                </TableRow>
              ) : (
                users.map((u, idx) => (
                  <TableRow key={u.id} hover>
                    <TableCell>{page * rowsPerPage + idx + 1}</TableCell>
                    <TableCell sx={{ fontWeight: 500 }}>{u.username}</TableCell>
                    <TableCell>{u.email}</TableCell>
                    <TableCell>
                      <Chip
                        label={u.role}
                        color={roleColors[u.role] || 'default'}
                        size="small"
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={u.active ? 'Active' : 'Inactive'}
                        color={u.active ? 'success' : 'error'}
                        size="small"
                      />
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          rowsPerPageOptions={[5, 10, 25]}
          component="div"
          count={totalElements}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={(e, newPage) => setPage(newPage)}
          onRowsPerPageChange={(e) => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
        />
      </Paper>
    </Box>
  );
}