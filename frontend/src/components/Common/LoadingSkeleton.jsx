import React from 'react';
import { Box, Skeleton, Grid, Card, CardContent } from '@mui/material';

export const LoadingSkeleton = ({ count = 4, type = 'card' }) => {
  if (type === 'table') {
    return (
      <Box sx={{ width: '100%', p: 2 }}>
        {[...Array(5)].map((_, i) => (
          <Skeleton key={i} variant="rectangular" height={45} sx={{ mb: 1, borderRadius: 1, bgcolor: '#1E293B' }} />
        ))}
      </Box>
    );
  }

  return (
    <Grid container spacing={3}>
      {[...Array(count)].map((_, i) => (
        <Grid item xs={12} sm={6} md={12 / count} key={i}>
          <Card sx={{ bgcolor: '#111827' }}>
            <CardContent>
              <Skeleton variant="text" width="60%" height={24} sx={{ bgcolor: '#1E293B' }} />
              <Skeleton variant="rectangular" height={40} sx={{ my: 1, borderRadius: 1, bgcolor: '#1E293B' }} />
              <Skeleton variant="text" width="40%" height={20} sx={{ bgcolor: '#1E293B' }} />
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
};

export default LoadingSkeleton;
