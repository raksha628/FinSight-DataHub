import React from 'react';
import { Box, Toolbar } from '@mui/material';
import Sidebar from './Sidebar';
import Topbar from './Topbar';

const drawerWidth = 240;

const MainLayout = ({ children, pageTitle = 'Dashboard' }) => {
  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: '#0B0F19' }}>
      <Sidebar />
      <Topbar pageTitle={pageTitle} />

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: `calc(100% - ${drawerWidth}px)`,
          bgcolor: '#0B0F19',
          minHeight: '100vh',
        }}
      >
        <Toolbar />
        {children}
      </Box>
    </Box>
  );
};

export default MainLayout;
