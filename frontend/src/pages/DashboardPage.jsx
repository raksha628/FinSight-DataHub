import React, { useEffect, useState } from 'react';
import MainLayout from '../components/Layout/MainLayout';
import KpiCard from '../components/Common/KpiCard';
import StatusChip from '../components/Common/StatusChip';
import LoadingSkeleton from '../components/Common/LoadingSkeleton';
import EmptyState from '../components/Common/EmptyState';
import api from '../services/api';
import { dashboardService } from '../services/dashboardService';
import { analyticsService } from '../services/analyticsService';
import {
  Grid,
  Card,
  Typography,
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Alert,
  Button,
  Collapse,
} from '@mui/material';
import {
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';

import BusinessIcon from '@mui/icons-material/Business';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import ShowChartIcon from '@mui/icons-material/ShowChart';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import EqualizerIcon from '@mui/icons-material/Equalizer';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';


const COLORS = ['#06B6D4', '#8B5CF6', '#10B981', '#F59E0B', '#EF4444', '#3B82F6', '#EC4899'];

const DashboardPage = () => {
  const [overview, setOverview] = useState(null);
  const [gainers, setGainers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    setError('');
    try {
      const [overviewRes, gainersRes] = await Promise.all([
        dashboardService.getOverview(),
        analyticsService.getTopGainers({ size: 7 }),
      ]);

      if (overviewRes.success) setOverview(overviewRes.data);
      if (gainersRes.success) setGainers(gainersRes.data.content || []);
    } catch (err) {
      console.error('Failed to load dashboard', err);
      setError('Could not connect to backend server. Make sure Spring Boot is running.');
    } finally {
      setLoading(false);
    }
  };



  if (loading) {
    return (
      <MainLayout pageTitle="Executive Dashboard">
        <LoadingSkeleton count={4} />
      </MainLayout>
    );
  }

  if (error) {
    return (
      <MainLayout pageTitle="Executive Dashboard">
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
        <EmptyState title="Server Connection Offline" description={error} actionText="Retry Connection" onAction={fetchDashboardData} />
      </MainLayout>
    );
  }

  const sectorData = overview?.sectorDistribution
    ? overview.sectorDistribution.map((item) => ({
        name: item.sector || 'General',
        value: item.companyCount || 1,
        avgPrice: item.avgClosePrice ? item.avgClosePrice.toFixed(2) : 0,
      }))
    : [];

  const topGainerText = overview?.topGainer ? `${overview.topGainer.symbol} (+$${overview.topGainer.changeAmount?.toFixed(2) || '0.00'})` : 'N/A';
  const topLoserText = overview?.topLoser ? `${overview.topLoser.symbol} ($${overview.topLoser.changeAmount?.toFixed(2) || '0.00'})` : 'N/A';
  const highestVolText = overview?.highestVolumeStock ? `${overview.highestVolumeStock.symbol} (${(overview.highestVolumeStock.volume / 1e6).toFixed(1)}M)` : 'N/A';
  const latestUploadTime = overview?.recentUploads?.length ? new Date(overview.recentUploads[0].uploadedAt).toLocaleTimeString() : 'N/A';

  return (
    <MainLayout pageTitle="Executive Dashboard">
      {/* Top 8 Metric KPI Cards Grid */}
      <Grid container spacing={2.5} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <KpiCard title="Total Companies" value={overview?.totalCompanies || 0} subtitle="Tracked Equities" icon={BusinessIcon} color="#06B6D4" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <KpiCard title="Total Records" value={overview?.totalStocks || 0} subtitle="OHLCV Price Points" icon={ShowChartIcon} color="#8B5CF6" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <KpiCard
            title="Today's Top Gainer"
            value={topGainerText}
            subtitle={overview?.topGainer?.companyName || 'N/A'}
            icon={TrendingUpIcon}
            color="#10B981"
            isPositive={true}
            trendValue={overview?.topGainer?.dailyReturn ? `+${(overview.topGainer.dailyReturn * 100).toFixed(2)}%` : undefined}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <KpiCard
            title="Today's Top Loser"
            value={topLoserText}
            subtitle={overview?.topLoser?.companyName || 'N/A'}
            icon={TrendingDownIcon}
            color="#EF4444"
            isPositive={false}
            trendValue={overview?.topLoser?.dailyReturn ? `${(overview.topLoser.dailyReturn * 100).toFixed(2)}%` : undefined}
          />
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <KpiCard title="Highest Volume" value={highestVolText} subtitle="Shares Traded" icon={EqualizerIcon} color="#3B82F6" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <KpiCard title="Total Uploaded Files" value={overview?.recentUploads?.length || 0} subtitle="ETL CSV Jobs" icon={CloudUploadIcon} color="#F59E0B" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <KpiCard title="Latest Import Time" value={latestUploadTime} subtitle="Last ETL Run" icon={AccessTimeIcon} color="#EC4899" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <KpiCard title="Data Quality Score" value="99.4%" subtitle="Validated Records" icon={VerifiedUserIcon} color="#10B981" isPositive={true} trendValue="Optimal" />
        </Grid>
      </Grid>



      {/* Visual Analytics Charts Section */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        {/* Sector Distribution Pie Chart */}
        <Grid item xs={12} md={5}>
          <Card sx={{ p: 2, height: '100%' }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" sx={{ color: '#F8FAFC', fontWeight: 700 }}>
                Sector Portfolio Breakdown
              </Typography>
            </Box>

            <Box sx={{ height: 260, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              {sectorData.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie data={sectorData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={85} innerRadius={45} paddingAngle={4}>
                      {sectorData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip contentStyle={{ backgroundColor: '#1E293B', borderColor: 'rgba(255,255,255,0.1)', borderRadius: 8, color: '#FFF' }} />
                    <Legend wrapperStyle={{ color: '#94A3B8', fontSize: '0.8rem' }} />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <Typography variant="body2" sx={{ color: '#64748B' }}>
                  No Sector Data Available
                </Typography>
              )}
            </Box>
          </Card>
        </Grid>

        {/* Top 7 Gainers Bar Chart */}
        <Grid item xs={12} md={7}>
          <Card sx={{ p: 2, height: '100%' }}>
            <Typography variant="h6" sx={{ color: '#F8FAFC', mb: 2, fontWeight: 700 }}>
              Top Daily Gainers (% Return)
            </Typography>
            <Box sx={{ height: 260 }}>
              {gainers.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={gainers.map((g) => ({ symbol: g.symbol, returnPct: (g.dailyReturn * 100).toFixed(2) }))}>
                    <XAxis dataKey="symbol" stroke="#64748B" />
                    <YAxis stroke="#64748B" unit="%" />
                    <Tooltip contentStyle={{ backgroundColor: '#1E293B', borderColor: 'rgba(255,255,255,0.1)', borderRadius: 8 }} />
                    <Bar dataKey="returnPct" fill="#10B981" radius={[6, 6, 0, 0]} name="Gain %" />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <Typography variant="body2" sx={{ color: '#64748B', textAlign: 'center', pt: 10 }}>
                  Upload stock CSV to view market gainers
                </Typography>
              )}
            </Box>
          </Card>
        </Grid>
      </Grid>

      {/* Recent Upload Activity Table */}
      <Card sx={{ p: 2.5 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6" sx={{ color: '#F8FAFC', fontWeight: 700 }}>
            Recent ETL Ingestion Activity
          </Typography>
          <Chip label="Realtime Audit Log" size="small" sx={{ bgcolor: 'rgba(6, 182, 212, 0.15)', color: '#06B6D4', fontWeight: 700 }} />
        </Box>

        {overview?.recentUploads && overview.recentUploads.length > 0 ? (
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Filename</TableCell>
                  <TableCell>Asset Type</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Accepted</TableCell>
                  <TableCell align="right">Rejected</TableCell>
                  <TableCell align="right">Duration (ms)</TableCell>
                  <TableCell align="right">Uploaded By</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {overview.recentUploads.map((row) => (
                  <TableRow key={row.id} hover>
                    <TableCell sx={{ fontWeight: 600, color: '#F8FAFC' }}>{row.originalFilename}</TableCell>
                    <TableCell>
                      <Chip label={row.assetType} size="small" variant="outlined" sx={{ fontSize: '0.7rem', fontWeight: 700 }} />
                    </TableCell>
                    <TableCell>
                      <StatusChip status={row.status} />
                    </TableCell>
                    <TableCell align="right" sx={{ color: '#10B981', fontWeight: 600 }}>
                      {row.acceptedRows}
                    </TableCell>
                    <TableCell align="right" sx={{ color: row.rejectedRows > 0 ? '#EF4444' : '#64748B', fontWeight: 600 }}>
                      {row.rejectedRows}
                    </TableCell>
                    <TableCell align="right" sx={{ color: '#94A3B8' }}>
                      {row.processingMs || 0} ms
                    </TableCell>
                    <TableCell align="right" sx={{ color: '#94A3B8' }}>
                      {row.uploadedByUsername || 'System'}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        ) : (
          <EmptyState title="No Upload History" description="Drop CSV files into data/incoming or use the Upload Center to import data." />
        )}
      </Card>
    </MainLayout>
  );
};

export default DashboardPage;
