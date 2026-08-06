import React from 'react';
import { Chip } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import WarningIcon from '@mui/icons-material/Warning';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';

const StatusChip = ({ status }) => {
  let color = 'default';
  let icon = null;
  let label = status || 'UNKNOWN';

  switch (status) {
    case 'SUCCESS':
      color = 'success';
      icon = <CheckCircleIcon style={{ fontSize: 14 }} />;
      break;
    case 'PARTIAL':
      color = 'warning';
      icon = <WarningIcon style={{ fontSize: 14 }} />;
      break;
    case 'FAILED':
      color = 'error';
      icon = <ErrorIcon style={{ fontSize: 14 }} />;
      break;
    case 'PROCESSING':
      color = 'info';
      icon = <HourglassEmptyIcon style={{ fontSize: 14 }} />;
      break;
    default:
      break;
  }

  return (
    <Chip
      size="small"
      icon={icon}
      label={label}
      color={color}
      sx={{
        fontWeight: 700,
        fontSize: '0.72rem',
        height: 24,
        px: 0.5,
        borderRadius: 1.5,
      }}
    />
  );
};

export default StatusChip;
