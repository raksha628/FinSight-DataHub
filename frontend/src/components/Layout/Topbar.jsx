import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import {
  Box,
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  Avatar,
  Menu,
  MenuItem,
  Chip,
  Tooltip,
} from '@mui/material';
import LogoutIcon from '@mui/icons-material/Logout';
import PersonIcon from '@mui/icons-material/Person';
import NotificationsNoneIcon from '@mui/icons-material/NotificationsNone';
import RefreshIcon from '@mui/icons-material/Refresh';

const drawerWidth = 240;

const Topbar = ({ pageTitle = 'Dashboard' }) => {
  const { user, logout } = useAuth();
  const [anchorEl, setAnchorEl] = useState(null);

  const handleMenuOpen = (event) => setAnchorEl(event.currentTarget);
  const handleMenuClose = () => setAnchorEl(null);

  const handleLogout = () => {
    handleMenuClose();
    logout();
  };

  return (
    <AppBar
      position="fixed"
      sx={{
        width: `calc(100% - ${drawerWidth}px)`,
        ml: `${drawerWidth}px`,
        backgroundColor: 'rgba(11, 15, 25, 0.85)',
        backdropFilter: 'blur(12px)',
        boxShadow: 'none',
        borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
      }}
    >
      <Toolbar sx={{ justifyContent: 'space-between', px: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Typography variant="h5" sx={{ fontWeight: 700, color: '#F8FAFC' }}>
            {pageTitle}
          </Typography>
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Tooltip title="Refresh Dashboard Data">
            <IconButton size="small" onClick={() => window.location.reload()} sx={{ color: '#94A3B8' }}>
              <RefreshIcon />
            </IconButton>
          </Tooltip>

          <IconButton size="small" sx={{ color: '#94A3B8' }}>
            <NotificationsNoneIcon />
          </IconButton>

          <Box
            onClick={handleMenuOpen}
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1.5,
              cursor: 'pointer',
              p: 0.5,
              px: 1.5,
              borderRadius: 3,
              bgcolor: 'rgba(255, 255, 255, 0.05)',
              '&:hover': { bgcolor: 'rgba(255, 255, 255, 0.1)' },
            }}
          >
            <Avatar sx={{ width: 34, height: 34, bgcolor: '#06B6D4', fontSize: '0.875rem', fontWeight: 700 }}>
              {user?.username ? user.username.charAt(0).toUpperCase() : 'U'}
            </Avatar>
            <Box sx={{ display: { xs: 'none', sm: 'block' } }}>
              <Typography variant="subtitle2" sx={{ color: '#F8FAFC', fontWeight: 600, lineHeight: 1.1 }}>
                {user?.username || 'User'}
              </Typography>
              <Chip
                label={user?.role || 'ANALYST'}
                size="small"
                sx={{
                  height: 16,
                  fontSize: '0.625rem',
                  fontWeight: 800,
                  bgcolor: user?.role === 'ADMIN' ? 'rgba(239, 68, 68, 0.2)' : 'rgba(6, 182, 212, 0.2)',
                  color: user?.role === 'ADMIN' ? '#EF4444' : '#06B6D4',
                  mt: 0.2,
                }}
              />
            </Box>
          </Box>

          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleMenuClose}
            PaperProps={{
              sx: {
                mt: 1.5,
                bgcolor: '#111827',
                border: '1px solid rgba(255, 255, 255, 0.1)',
                minWidth: 160,
              },
            }}
          >
            <MenuItem onClick={handleMenuClose}>
              <PersonIcon sx={{ mr: 1.5, fontSize: 18, color: '#94A3B8' }} /> Profile
            </MenuItem>
            <MenuItem onClick={handleLogout} sx={{ color: '#EF4444' }}>
              <LogoutIcon sx={{ mr: 1.5, fontSize: 18, color: '#EF4444' }} /> Logout
            </MenuItem>
          </Menu>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Topbar;
