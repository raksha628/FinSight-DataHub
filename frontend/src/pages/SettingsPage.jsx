import React from 'react';
import MainLayout from '../components/Layout/MainLayout';
import { useAuth } from '../context/AuthContext';
import {
  Grid,
  Card,
  CardContent,
  Typography,
  Box,
  Avatar,
  Button,
  Chip,
  Switch,
  FormControlLabel,
  Divider,
} from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import SecurityIcon from '@mui/icons-material/Security';
import PaletteIcon from '@mui/icons-material/Palette';
import LogoutIcon from '@mui/icons-material/Logout';

const SettingsPage = () => {
  const { user, logout } = useAuth();

  return (
    <MainLayout pageTitle="Account Settings & Preferences">
      <Grid container spacing={3}>
        {/* User Profile Card */}
        <Grid item xs={12} md={6}>
          <Card sx={{ p: 1, height: '100%' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
                <Avatar sx={{ width: 64, height: 64, bgcolor: '#06B6D4', fontSize: '1.5rem', fontWeight: 700 }}>
                  {user?.username ? user.username.charAt(0).toUpperCase() : 'U'}
                </Avatar>
                <Box>
                  <Typography variant="h5" sx={{ color: '#F8FAFC', fontWeight: 700 }}>
                    {user?.username || 'Authenticated User'}
                  </Typography>
                  <Typography variant="body2" sx={{ color: '#94A3B8' }}>
                    {user?.email || 'user@finsight.com'}
                  </Typography>
                  <Chip
                    label={user?.role || 'ANALYST'}
                    size="small"
                    sx={{
                      mt: 1,
                      fontWeight: 800,
                      bgcolor: user?.role === 'ADMIN' ? 'rgba(239, 68, 68, 0.2)' : 'rgba(6, 182, 212, 0.2)',
                      color: user?.role === 'ADMIN' ? '#EF4444' : '#06B6D4',
                    }}
                  />
                </Box>
              </Box>

              <Divider sx={{ borderColor: 'rgba(255,255,255,0.08)', mb: 2.5 }} />

              <Typography variant="subtitle2" sx={{ color: '#94A3B8', mb: 1 }}>
                Account Security Status
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
                <SecurityIcon sx={{ color: '#10B981', fontSize: 20 }} />
                <Typography variant="body2" sx={{ color: '#F8FAFC', fontWeight: 600 }}>
                  Stateless JWT Token Active (24h Expiry)
                </Typography>
              </Box>

              <Button variant="outlined" color="error" startIcon={<LogoutIcon />} onClick={logout}>
                Sign Out of Platform
              </Button>
            </CardContent>
          </Card>
        </Grid>

        {/* Platform Preferences */}
        <Grid item xs={12} md={6}>
          <Card sx={{ p: 1, height: '100%' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                <PaletteIcon sx={{ color: '#06B6D4' }} />
                <Typography variant="h6" sx={{ color: '#F8FAFC', fontWeight: 700 }}>
                  Platform Preferences
                </Typography>
              </Box>

              <Divider sx={{ borderColor: 'rgba(255,255,255,0.08)', mb: 2.5 }} />

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <FormControlLabel
                  control={<Switch defaultChecked color="primary" />}
                  label={
                    <Box>
                      <Typography variant="body2" sx={{ color: '#F8FAFC', fontWeight: 600 }}>
                        Dark Terminal Mode
                      </Typography>
                      <Typography variant="caption" sx={{ color: '#64748B' }}>
                        High contrast dark palette tailored for financial traders
                      </Typography>
                    </Box>
                  }
                />



                <FormControlLabel
                  control={<Switch defaultChecked color="primary" />}
                  label={
                    <Box>
                      <Typography variant="body2" sx={{ color: '#F8FAFC', fontWeight: 600 }}>
                        Realtime Recharts Animations
                      </Typography>
                      <Typography variant="caption" sx={{ color: '#64748B' }}>
                        Enable smooth chart transitions on market updates
                      </Typography>
                    </Box>
                  }
                />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </MainLayout>
  );
};

export default SettingsPage;
