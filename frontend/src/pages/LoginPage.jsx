import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  Alert,
  CircularProgress,
  InputAdornment,
  IconButton,
  Chip,
} from '@mui/material';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import StorageIcon from '@mui/icons-material/Storage';
import LockIcon from '@mui/icons-material/Lock';
import PersonIcon from '@mui/icons-material/Person';

const LoginPage = () => {
  const navigate = useNavigate();
  const { login, logout } = useAuth();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    // Clear any stale tokens on login screen mount
    logout();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!username || !password) {
      setError('Please provide both username and password.');
      return;
    }

    setError('');
    setLoading(true);
    const result = await login(username, password);
    setLoading(false);

    if (result.success) {
      navigate('/');
    } else {
      setError(result.message);
    }
  };

  const handleQuickFill = (user, pass) => {
    setUsername(user);
    setPassword(pass);
    setError('');
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        bgcolor: '#0B0F19',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        p: 2,
        backgroundImage: 'radial-gradient(circle at 50% 30%, rgba(6, 182, 212, 0.12) 0%, transparent 60%)',
      }}
    >
      <Card
        sx={{
          maxWidth: 440,
          width: '100%',
          bgcolor: '#111827',
          border: '1px solid rgba(255, 255, 255, 0.1)',
          boxShadow: '0 20px 40px rgba(0, 0, 0, 0.6)',
          borderRadius: 3.5,
          p: 2,
        }}
      >
        <CardContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3 }}>
            <Box
              sx={{
                width: 54,
                height: 54,
                borderRadius: 3,
                background: 'linear-gradient(135deg, #06B6D4 0%, #3B82F6 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#FFF',
                boxShadow: '0 6px 20px rgba(6, 182, 212, 0.4)',
                mb: 1.5,
              }}
            >
              <StorageIcon sx={{ fontSize: 32 }} />
            </Box>
            <Typography variant="h4" sx={{ fontWeight: 800, color: '#F8FAFC', textAlign: 'center' }}>
              FinSight DataHub
            </Typography>
            <Typography variant="body2" sx={{ color: '#94A3B8', mt: 0.5, textAlign: 'center' }}>
              Enterprise Financial Data Warehouse & Analytics Platform
            </Typography>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 2.5, borderRadius: 2, bgcolor: 'rgba(239, 68, 68, 0.1)', color: '#F87171' }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Username"
              variant="outlined"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              sx={{ mb: 2.5 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <PersonIcon sx={{ color: '#64748B' }} />
                  </InputAdornment>
                ),
              }}
            />

            <TextField
              fullWidth
              label="Password"
              type={showPassword ? 'text' : 'password'}
              variant="outlined"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              sx={{ mb: 3 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <LockIcon sx={{ color: '#64748B' }} />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={() => setShowPassword(!showPassword)} edge="end" sx={{ color: '#64748B' }}>
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={loading}
              sx={{
                py: 1.5,
                borderRadius: 2.5,
                fontWeight: 700,
                fontSize: '1rem',
                background: 'linear-gradient(135deg, #06B6D4 0%, #3B82F6 100%)',
                boxShadow: '0 4px 15px rgba(6, 182, 212, 0.3)',
                '&:hover': {
                  background: 'linear-gradient(135deg, #0891B2 0%, #2563EB 100%)',
                },
              }}
            >
              {loading ? <CircularProgress size={26} color="inherit" /> : 'Sign In'}
            </Button>
          </Box>

          <Box sx={{ mt: 3.5, pt: 3, borderTop: '1px solid rgba(255, 255, 255, 0.08)' }}>
            <Typography variant="caption" sx={{ color: '#64748B', display: 'block', mb: 1.5, fontWeight: 700, letterSpacing: 0.5 }}>
              QUICK DEMO CREDENTIALS
            </Typography>
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
              <Chip
                label="Admin: admin / Admin@123"
                clickable
                onClick={() => handleQuickFill('admin', 'Admin@123')}
                sx={{
                  bgcolor: 'rgba(239, 68, 68, 0.1)',
                  color: '#EF4444',
                  border: '1px solid rgba(239, 68, 68, 0.2)',
                  fontWeight: 600,
                  fontSize: '0.75rem',
                }}
              />
              <Chip
                label="Analyst: analyst / Analyst@123"
                clickable
                onClick={() => handleQuickFill('analyst', 'Analyst@123')}
                sx={{
                  bgcolor: 'rgba(6, 182, 212, 0.1)',
                  color: '#06B6D4',
                  border: '1px solid rgba(6, 182, 212, 0.2)',
                  fontWeight: 600,
                  fontSize: '0.75rem',
                }}
              />
            </Box>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default LoginPage;
