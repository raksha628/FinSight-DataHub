import React from 'react';
import { Card, CardContent, Typography, Box, Avatar } from '@mui/material';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';

const KpiCard = ({ title, value, subtitle, icon: IconComponent, color = '#06B6D4', isPositive = true, trendValue }) => {
  return (
    <Card sx={{ height: '100%', background: 'linear-gradient(135deg, #111827 0%, #1E293B 100%)', position: 'relative', overflow: 'hidden' }}>
      <Box
        sx={{
          position: 'absolute',
          top: -10,
          right: -10,
          width: 80,
          height: 80,
          borderRadius: '50%',
          background: `${color}15`,
          filter: 'blur(20px)',
        }}
      />
      <CardContent sx={{ p: 2.5 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1.5 }}>
          <Typography variant="subtitle2" sx={{ color: '#94A3B8', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            {title}
          </Typography>
          {IconComponent && (
            <Avatar sx={{ bgcolor: `${color}20`, color: color, width: 42, height: 42, borderRadius: 2 }}>
              <IconComponent fontSize="small" />
            </Avatar>
          )}
        </Box>

        <Typography variant="h3" sx={{ color: '#F8FAFC', fontWeight: 700, mb: 1 }}>
          {value}
        </Typography>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8 }}>
          {trendValue !== undefined && trendValue !== null && (
            <Box
              sx={{
                display: 'inline-flex',
                alignItems: 'center',
                px: 1,
                py: 0.2,
                borderRadius: 1.5,
                fontSize: '0.75rem',
                fontWeight: 700,
                bgcolor: isPositive ? 'rgba(16, 185, 129, 0.15)' : 'rgba(239, 68, 68, 0.15)',
                color: isPositive ? '#10B981' : '#EF4444',
              }}
            >
              {isPositive ? <TrendingUpIcon sx={{ fontSize: 14, mr: 0.3 }} /> : <TrendingDownIcon sx={{ fontSize: 14, mr: 0.3 }} />}
              {trendValue}
            </Box>
          )}
          {subtitle && (
            <Typography variant="caption" sx={{ color: '#64748B' }}>
              {subtitle}
            </Typography>
          )}
        </Box>
      </CardContent>
    </Card>
  );
};

export default KpiCard;
