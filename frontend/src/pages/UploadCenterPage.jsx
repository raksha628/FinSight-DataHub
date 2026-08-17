import React, { useState, useEffect } from 'react';
import MainLayout from '../components/Layout/MainLayout';
import StatusChip from '../components/Common/StatusChip';
import EmptyState from '../components/Common/EmptyState';
import LoadingSkeleton from '../components/Common/LoadingSkeleton';
import { uploadService } from '../services/uploadService';
import {
  Grid,
  Card,
  Typography,
  Box,
  Button,
  MenuItem,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Alert,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
} from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import HistoryIcon from '@mui/icons-material/History';
import AssessmentIcon from '@mui/icons-material/Assessment';
import DescriptionIcon from '@mui/icons-material/Description';

const ASSET_TYPES = [
  { value: 'STOCK', label: 'Stocks (US Equities OHLCV)' },
  { value: 'ETF', label: 'Exchange Traded Funds (NAV & AUM)' },
  { value: 'MUTUAL_FUND', label: 'Mutual Funds (Scheme NAV)' },
];

const UploadCenterPage = () => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [assetType, setAssetType] = useState('STOCK');
  const [uploading, setUploading] = useState(false);
  const [uploadMessage, setUploadMessage] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  // Validation report modal state
  const [selectedReport, setSelectedReport] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);

  useEffect(() => {
    fetchUploadHistory();
  }, []);

  const fetchUploadHistory = async () => {
    setLoading(true);
    try {
      const res = await uploadService.getUploadHistory({ page: 0, size: 50 });
      if (res && res.success) {
        setHistory(res.data.content || []);
      }
    } catch (err) {
      console.error('Failed to fetch upload history', err);
    } finally {
      setLoading(false);
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
      setUploadMessage(null);
    }
  };

  const handleUploadSubmit = async (e) => {
    e.preventDefault();
    if (!selectedFile) {
      setUploadMessage({ type: 'error', text: 'Please select a CSV file to upload.' });
      return;
    }

    setUploading(true);
    setUploadMessage(null);

    try {
      const res = await uploadService.uploadCsv(selectedFile, assetType);
      if (res && res.success) {
        setUploadMessage({ type: 'success', text: res.message || 'File ingested successfully!' });
        setSelectedFile(null);
        fetchUploadHistory();
      } else {
        setUploadMessage({ type: 'error', text: res.message || 'Upload failed.' });
      }
    } catch (err) {
      const msg = err.response?.data?.message || 'Ingestion failed due to server error.';
      setUploadMessage({ type: 'error', text: msg });
    } finally {
      setUploading(false);
    }
  };

  const handleOpenReport = (row) => {
    let reportList = [];
    if (row.validationReport) {
      try {
        reportList = JSON.parse(row.validationReport);
      } catch (e) {
        console.error('Failed to parse report JSON', e);
      }
    }
    setSelectedReport({ filename: row.originalFilename, report: reportList, errorMessage: row.errorMessage });
    setModalOpen(true);
  };

  return (
    <MainLayout pageTitle="ETL Ingestion & Upload Center">
      <Grid container spacing={3}>
        {/* Left Column: CSV Dropzone & Strategy Selector */}
        <Grid item xs={12} md={5}>
          <Card sx={{ p: 3 }}>
            <Typography variant="h6" sx={{ color: '#F8FAFC', fontWeight: 700, mb: 1 }}>
              Upload Financial Market CSV
            </Typography>
            <Typography variant="body2" sx={{ color: '#94A3B8', mb: 3 }}>
              Select asset type strategy and upload structured CSV financial data into the warehouse.
            </Typography>

            {uploadMessage && (
              <Alert severity={uploadMessage.type} sx={{ mb: 2.5, borderRadius: 2 }}>
                {uploadMessage.text}
              </Alert>
            )}

            <Box component="form" onSubmit={handleUploadSubmit}>
              <TextField
                fullWidth
                select
                size="small"
                label="Asset Strategy Type"
                value={assetType}
                onChange={(e) => setAssetType(e.target.value)}
                sx={{ mb: 3 }}
              >
                {ASSET_TYPES.map((type) => (
                  <MenuItem key={type.value} value={type.value}>
                    {type.label}
                  </MenuItem>
                ))}
              </TextField>

              {/* Drag & Drop File Selector Box */}
              <Box
                component="label"
                sx={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  justifyContent: 'center',
                  p: 4,
                  border: '2px dashed rgba(6, 182, 212, 0.4)',
                  borderRadius: 3,
                  bgcolor: 'rgba(6, 182, 212, 0.05)',
                  cursor: 'pointer',
                  transition: 'all 0.2s ease',
                  mb: 3,
                  '&:hover': {
                    bgcolor: 'rgba(6, 182, 212, 0.1)',
                    borderColor: '#06B6D4',
                  },
                }}
              >
                <input type="file" accept=".csv" hidden onChange={handleFileChange} />
                <CloudUploadIcon sx={{ fontSize: 48, color: '#06B6D4', mb: 1 }} />
                <Typography variant="body1" sx={{ color: '#F8FAFC', fontWeight: 600 }}>
                  {selectedFile ? selectedFile.name : 'Click to Browse or Drag & Drop CSV'}
                </Typography>
                <Typography variant="caption" sx={{ color: '#64748B', mt: 0.5 }}>
                  {selectedFile ? `${(selectedFile.size / 1024).toFixed(1)} KB` : 'Supports .CSV files up to 50MB'}
                </Typography>
              </Box>

              <Button
                type="submit"
                fullWidth
                variant="contained"
                size="large"
                disabled={uploading || !selectedFile}
                startIcon={uploading ? <CircularProgress size={20} color="inherit" /> : <CloudUploadIcon />}
                sx={{ py: 1.4, fontWeight: 700 }}
              >
                {uploading ? 'Processing Strategy ETL...' : 'Ingest CSV File'}
              </Button>
            </Box>
          </Card>
        </Grid>

        {/* Right Column: Ingestion Audit History List */}
        <Grid item xs={12} md={7}>
          <Card sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" sx={{ color: '#F8FAFC', fontWeight: 700 }}>
                ETL Ingestion Audit History
              </Typography>
              <Chip icon={<HistoryIcon />} label="Audit Trail" size="small" sx={{ bgcolor: 'rgba(139, 92, 246, 0.2)', color: '#A78BFA' }} />
            </Box>

            {loading ? (
              <LoadingSkeleton count={4} type="table" />
            ) : history.length > 0 ? (
              <TableContainer sx={{ maxHeight: 420 }}>
                <Table size="small" stickyHeader>
                  <TableHead>
                    <TableRow>
                      <TableCell>File</TableCell>
                      <TableCell>Asset Type</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell align="right">Success/Total</TableCell>
                      <TableCell align="right">Report</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {history.map((row) => (
                      <TableRow key={row.id} hover>
                        <TableCell sx={{ color: '#F8FAFC', fontWeight: 600 }}>{row.originalFilename}</TableCell>
                        <TableCell>
                          <Chip label={row.assetType} size="small" variant="outlined" sx={{ fontSize: '0.65rem' }} />
                        </TableCell>
                        <TableCell>
                          <StatusChip status={row.status} />
                        </TableCell>
                        <TableCell align="right" sx={{ color: '#94A3B8' }}>
                          <span style={{ color: '#10B981', fontWeight: 700 }}>{row.acceptedRows}</span> / {row.totalRows}
                        </TableCell>
                        <TableCell align="right">
                          <Button size="small" startIcon={<AssessmentIcon />} onClick={() => handleOpenReport(row)}>
                            View
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            ) : (
              <EmptyState title="No Upload Runs Yet" description="Upload CSV data to trigger the ETL ingestion pipeline." />
            )}
          </Card>
        </Grid>
      </Grid>

      {/* Validation Report Modal Dialog */}
      <Dialog open={modalOpen} onClose={() => setModalOpen(false)} maxWidth="md" fullWidth PaperProps={{ sx: { bgcolor: '#111827' } }}>
        <DialogTitle sx={{ color: '#F8FAFC', fontWeight: 700, borderBottom: '1px solid rgba(255,255,255,0.08)' }}>
          Validation Report — {selectedReport?.filename}
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          {selectedReport?.errorMessage && (
            <Alert severity="error" sx={{ mb: 2 }}>
              Top-Level Error: {selectedReport.errorMessage}
            </Alert>
          )}

          {selectedReport?.report && selectedReport.report.length > 0 ? (
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Row #</TableCell>
                    <TableCell>Rejection Reason</TableCell>
                    <TableCell>Row Values</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {selectedReport.report.map((item, i) => (
                    <TableRow key={i}>
                      <TableCell sx={{ color: '#EF4444', fontWeight: 700 }}>Row {item.rowNumber}</TableCell>
                      <TableCell sx={{ color: '#F8FAFC' }}>{item.reason}</TableCell>
                      <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.75rem', color: '#94A3B8' }}>
                        {item.rowData ? JSON.stringify(item.rowData) : 'N/A'}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          ) : (
            <Typography variant="body2" sx={{ color: '#10B981', fontWeight: 600 }}>
              ✓ All records were validated and imported successfully without errors.
            </Typography>
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2, borderTop: '1px solid rgba(255,255,255,0.08)' }}>
          <Button onClick={() => setModalOpen(false)} variant="contained">
            Close Report
          </Button>
        </DialogActions>
      </Dialog>
    </MainLayout>
  );
};

export default UploadCenterPage;
