import React from 'react';
import { Box, Typography, Button } from '@mui/material';
import QueryStatsIcon from '@mui/icons-material/QueryStats';

const EmptyState = ({ title = 'No Market Data Found', description = 'Upload CSV financial data or adjust your search filters.', actionText, onAction }) => {
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        p: 6,
        textAlign: 'center',
        borderRadius: 3,
        bgcolor: 'rgba(17, 24, 39, 0.6)',
        border: '1px dashed rgba(255, 255, 255, 0.12)',
        my: 2,
      }}
    >
      <Box
        sx={{
          width: 64,
          height: 64,
          borderRadius: '50%',
          bgcolor: 'rgba(6, 182, 212, 0.1)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#06B6D4',
          mb: 2,
        }}
      >
        <QueryStatsIcon sx={{ fontSize: 36 }} />
      </Box>

      <Typography variant="h5" sx={{ color: '#F8FAFC', fontWeight: 600, mb: 1 }}>
        {title}
      </Typography>

      <Typography variant="body2" sx={{ color: '#94A3B8', maxWidth: 400, mb: 3 }}>
        {description}
      </Typography>

      {actionText && onAction && (
        <Button variant="contained" color="primary" onClick={onAction} sx={{ borderRadius: 2 }}>
          {actionText}
        </Button>
      )}
    </Box>
  );
};

export default EmptyState;
