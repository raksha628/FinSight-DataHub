import React, { useState, useEffect } from 'react';
import MainLayout from '../components/Layout/MainLayout';
import LoadingSkeleton from '../components/Common/LoadingSkeleton';
import EmptyState from '../components/Common/EmptyState';
import { analyticsService } from '../services/analyticsService';
import {
  Grid,
  Card,
  Typography,
  Box,
  TextField,
  MenuItem,
  Button,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Chip,
  IconButton,
  Tooltip as MuiTooltip,
} from '@mui/material';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import SearchIcon from '@mui/icons-material/Search';
import FilterAltIcon from '@mui/icons-material/FilterAlt';
import RefreshIcon from '@mui/icons-material/Refresh';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';

const SECTORS = ['All', 'Technology', 'Financial Services', 'Healthcare', 'Consumer Cyclical', 'Energy', 'Industrials'];

const MarketAnalyticsPage = () => {
  const [tabValue, setTabValue] = useState(0);
  const [symbol, setSymbol] = useState('');
  const [sector, setSector] = useState('All');
  const [date, setDate] = useState('');
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  const [data, setData] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [movingAverages, setMovingAverages] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAnalyticsData();
  }, [tabValue, page, pageSize, sector, date]);

  const fetchAnalyticsData = async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size: pageSize,
        sector: sector !== 'All' ? sector : undefined,
        symbol: symbol ? symbol : undefined,
        date: date ? date : undefined,
      };

      let response;
      if (tabValue === 0) response = await analyticsService.getTopGainers(params);
      else if (tabValue === 1) response = await analyticsService.getTopLosers(params);
      else if (tabValue === 2) response = await analyticsService.getHighestVolume(params);
      else if (tabValue === 3) response = await analyticsService.getDailyReturns(params);
      else if (tabValue === 4) response = await analyticsService.getHighestClose(params);

      if (response && response.success) {
        setData(response.data.content || []);
        setTotalElements(response.data.totalElements || 0);
      }

      // Fetch technical moving averages for chart
      const maRes = await analyticsService.getMovingAverages({ symbol: symbol || 'AAPL' });
      if (maRes && maRes.success) {
        setMovingAverages(maRes.data || []);
      }
    } catch (err) {
      console.error('Failed to fetch analytics', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchAnalyticsData();
  };

  return (
    <MainLayout pageTitle="Market Analytics Engine">
      {/* Search & Filter Bar */}
      <Card sx={{ p: 2.5, mb: 3 }}>
        <Box component="form" onSubmit={handleSearchSubmit}>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} sm={4} md={3}>
              <TextField
                fullWidth
                size="small"
                label="Ticker Symbol"
                placeholder="e.g. AAPL, MSFT"
                value={symbol}
                onChange={(e) => setSymbol(e.target.value)}
                InputProps={{
                  endAdornment: <SearchIcon sx={{ color: '#64748B' }} />,
                }}
              />
            </Grid>

            <Grid item xs={12} sm={4} md={3}>
              <TextField fullWidth select size="small" label="Sector" value={sector} onChange={(e) => setSector(e.target.value)}>
                {SECTORS.map((sec) => (
                  <MenuItem key={sec} value={sec}>
                    {sec}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>

            <Grid item xs={12} sm={4} md={3}>
              <TextField
                fullWidth
                size="small"
                label="Trade Date"
                type="date"
                value={date}
                onChange={(e) => setDate(e.target.value)}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            <Grid item xs={12} sm={12} md={3} sx={{ display: 'flex', gap: 1 }}>
              <Button type="submit" variant="contained" color="primary" fullWidth startIcon={<FilterAltIcon />}>
                Apply Filters
              </Button>
              <MuiTooltip title="Reset Filters">
                <IconButton
                  onClick={() => {
                    setSymbol('');
                    setSector('All');
                    setDate('');
                    setPage(0);
                  }}
                  sx={{ color: '#94A3B8' }}
                >
                  <RefreshIcon />
                </IconButton>
              </MuiTooltip>
            </Grid>
          </Grid>
        </Box>
      </Card>

      {/* Technical Moving Average SMA Chart */}
      <Card sx={{ p: 2.5, mb: 3 }}>
        <Typography variant="h6" sx={{ color: '#F8FAFC', fontWeight: 700, mb: 1 }}>
          Technical Moving Averages (SMA-20 vs SMA-50) — {symbol ? symbol.toUpperCase() : 'AAPL'}
        </Typography>
        <Box sx={{ height: 260 }}>
          {movingAverages.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={movingAverages}>
                <XAxis dataKey="tradeDate" stroke="#64748B" />
                <YAxis stroke="#64748B" domain={['auto', 'auto']} />
                <Tooltip contentStyle={{ backgroundColor: '#1E293B', borderColor: 'rgba(255,255,255,0.1)', borderRadius: 8 }} />
                <Legend wrapperStyle={{ color: '#94A3B8' }} />
                <Line type="monotone" dataKey="closePrice" stroke="#F8FAFC" name="Close Price" dot={false} strokeWidth={2} />
                <Line type="monotone" dataKey="sma20" stroke="#06B6D4" name="SMA-20" dot={false} strokeWidth={2} />
                <Line type="monotone" dataKey="sma50" stroke="#8B5CF6" name="SMA-50" dot={false} strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <EmptyState title="No Technical Data" description="Select a symbol to view technical moving averages." />
          )}
        </Box>
      </Card>

      {/* Analytics Tabs & Data Table */}
      <Card sx={{ p: 2.5 }}>
        <Tabs
          value={tabValue}
          onChange={(e, val) => {
            setTabValue(val);
            setPage(0);
          }}
          sx={{ borderBottom: '1px solid rgba(255, 255, 255, 0.08)', mb: 2 }}
        >
          <Tab label="Top Gainers" />
          <Tab label="Top Losers" />
          <Tab label="Highest Volume" />
          <Tab label="Daily Returns" />
          <Tab label="Peak Prices" />
        </Tabs>

        {loading ? (
          <LoadingSkeleton count={5} type="table" />
        ) : data.length > 0 ? (
          <>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Symbol</TableCell>
                    <TableCell>Company Name</TableCell>
                    <TableCell>Sector</TableCell>
                    <TableCell align="right">Trade Date</TableCell>
                    <TableCell align="right">Open</TableCell>
                    <TableCell align="right">High</TableCell>
                    <TableCell align="right">Low</TableCell>
                    <TableCell align="right">Close</TableCell>
                    <TableCell align="right">Volume</TableCell>
                    <TableCell align="right">Daily Return</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {data.map((row, idx) => {
                    const returnVal = row.dailyReturn || row.percentageReturn || 0;
                    const isPos = returnVal >= 0;

                    return (
                      <TableRow key={idx} hover>
                        <TableCell sx={{ fontWeight: 700, color: '#06B6D4' }}>{row.symbol}</TableCell>
                        <TableCell sx={{ color: '#F8FAFC' }}>{row.companyName || row.symbol}</TableCell>
                        <TableCell>
                          <Chip label={row.sector || 'General'} size="small" variant="outlined" sx={{ fontSize: '0.7rem' }} />
                        </TableCell>
                        <TableCell align="right" sx={{ color: '#94A3B8' }}>{row.tradeDate || row.startDate}</TableCell>
                        <TableCell align="right">${row.openPrice?.toFixed(2) || row.startPrice?.toFixed(2) || '0.00'}</TableCell>
                        <TableCell align="right">${row.highPrice?.toFixed(2) || '0.00'}</TableCell>
                        <TableCell align="right">${row.lowPrice?.toFixed(2) || '0.00'}</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 700, color: '#F8FAFC' }}>
                          ${row.closePrice?.toFixed(2) || row.endPrice?.toFixed(2) || '0.00'}
                        </TableCell>
                        <TableCell align="right" sx={{ color: '#94A3B8' }}>
                          {row.volume ? (row.volume / 1e6).toFixed(2) + 'M' : 'N/A'}
                        </TableCell>
                        <TableCell align="right">
                          <Box
                            sx={{
                              display: 'inline-flex',
                              alignItems: 'center',
                              px: 1,
                              py: 0.2,
                              borderRadius: 1,
                              fontSize: '0.75rem',
                              fontWeight: 700,
                              bgcolor: isPos ? 'rgba(16, 185, 129, 0.15)' : 'rgba(239, 68, 68, 0.15)',
                              color: isPos ? '#10B981' : '#EF4444',
                            }}
                          >
                            {isPos ? <TrendingUpIcon sx={{ fontSize: 14, mr: 0.3 }} /> : <TrendingDownIcon sx={{ fontSize: 14, mr: 0.3 }} />}
                            {(returnVal * (row.percentageReturn ? 1 : 100)).toFixed(2)}%
                          </Box>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </TableContainer>

            <TablePagination
              component="div"
              count={totalElements}
              page={page}
              onPageChange={(e, newPage) => setPage(newPage)}
              rowsPerPage={pageSize}
              onRowsPerPageChange={(e) => {
                setPageSize(parseInt(e.target.value, 10));
                setPage(0);
              }}
              sx={{ color: '#94A3B8' }}
            />
          </>
        ) : (
          <EmptyState title="No Analytics Records" description="No market records match your filter parameters." />
        )}
      </Card>
    </MainLayout>
  );
};

export default MarketAnalyticsPage;
