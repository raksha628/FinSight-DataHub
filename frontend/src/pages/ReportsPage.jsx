import React, { useState } from 'react';
import MainLayout from '../components/Layout/MainLayout';
import api from '../services/api';
import {
  Grid,
  Card,
  CardContent,
  Typography,
  Box,
  Button,
  Chip,
  Alert,
  Divider,
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import AssessmentIcon from '@mui/icons-material/Assessment';

const REPORTS = [
  {
    title: 'Sector Performance Analytics Report',
    description: 'Detailed breakdown of average price, volume distribution, and sector market cap.',
    type: 'Financial Analytics',
  },
  {
    title: 'Top Gainers & Losers Daily Audit',
    description: 'Complete daily market return rankings including open, high, low, close prices.',
    type: 'Market Daily',
  },
  {
    title: 'Technical Moving Average Summary',
    description: '20-day and 50-day simple moving averages across all tracked equity symbols.',
    type: 'Technical Analysis',
  },
  {
    title: 'ETL Ingestion Audit Log',
    description: 'Full record of all CSV files processed, accepted row counts, and validation errors.',
    type: 'ETL Audit',
  },
];

const ReportsPage = () => {
  const [downloadMsg, setDownloadMsg] = useState(null);

  const handleDownload = async (reportTitle) => {
    setDownloadMsg({
      type: 'info',
      text: `Generating CSV report for "${reportTitle}"...`,
    });

    let endpoint = '';
    let filename = '';

    if (reportTitle === 'Sector Performance Analytics Report') {
      endpoint = '/reports/export/sector-performance';
      filename = 'sector_performance_report.csv';
    } else if (reportTitle === 'Top Gainers & Losers Daily Audit') {
      endpoint = '/reports/export/gainers-losers';
      filename = 'gainers_losers_report.csv';
    } else if (reportTitle === 'Technical Moving Average Summary') {
      endpoint = '/reports/export/moving-averages';
      filename = 'moving_averages_report.csv';
    } else if (reportTitle === 'ETL Ingestion Audit Log') {
      endpoint = '/reports/export/etl-audit';
      filename = 'etl_audit_log.csv';
    }

    if (!endpoint) {
      setDownloadMsg({ type: 'error', text: 'Unknown report selection.' });
      return;
    }

    try {
      const response = await api.get(endpoint, {
        responseType: 'blob',
      });

      const blob = new Blob([response.data], { type: response.headers['content-type'] });
      const downloadUrl = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = downloadUrl;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(downloadUrl);

      setDownloadMsg({
        type: 'success',
        text: 'CSV report downloaded successfully.',
      });
      setTimeout(() => setDownloadMsg(null), 3000);
    } catch (err) {
      console.error('Failed to download report', err);
      setDownloadMsg({
        type: 'error',
        text: 'Failed to download report. Make sure backend is running.',
      });
      setTimeout(() => setDownloadMsg(null), 4000);
    }
  };

  return (
    <MainLayout pageTitle="Reports & Analytics Export Center">
      {downloadMsg && (
        <Alert severity={downloadMsg.type} sx={{ mb: 3, borderRadius: 2 }}>
          {downloadMsg.text}
        </Alert>
      )}

      <Grid container spacing={3}>
        {REPORTS.map((report, idx) => (
          <Grid item xs={12} md={6} key={idx}>
            <Card sx={{ p: 1 }}>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1.5 }}>
                  <Typography variant="h6" sx={{ color: '#F8FAFC', fontWeight: 700 }}>
                    {report.title}
                  </Typography>
                  <Chip label={report.type} size="small" sx={{ bgcolor: 'rgba(6, 182, 212, 0.15)', color: '#06B6D4', fontWeight: 700 }} />
                </Box>

                <Typography variant="body2" sx={{ color: '#94A3B8', mb: 3 }}>
                  {report.description}
                </Typography>

                <Divider sx={{ borderColor: 'rgba(255, 255, 255, 0.08)', mb: 2 }} />

                <Box sx={{ display: 'flex', gap: 1.5 }}>
                  <Button
                    variant="contained"
                    color="primary"
                    size="small"
                    startIcon={<DownloadIcon />}
                    onClick={() => handleDownload(report.title)}
                  >
                    Download CSV
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </MainLayout>
  );
};

export default ReportsPage;
