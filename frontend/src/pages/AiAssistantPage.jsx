import React, { useState, useEffect } from 'react';
import MainLayout from '../components/Layout/MainLayout';
import api from '../services/api';
import {
  Grid,
  Card,
  Typography,
  Box,
  TextField,
  IconButton,
  Chip,
  Avatar,
  Paper,
  Divider,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  CircularProgress,
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import PersonIcon from '@mui/icons-material/Person';
import HistoryIcon from '@mui/icons-material/History';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import TerminalIcon from '@mui/icons-material/Terminal';

const SAMPLE_PROMPTS = [
  'Show top 5 gainers in Technology sector',
  'Which stock had highest trading volume today?',
  'Calculate average price by sector',
  'Show top 5 losing stocks today',
];

const INITIAL_MESSAGES = [
  {
    sender: 'ai',
    text: "Hello! I am your FinSight AI Copilot. Ask me natural language questions about your market data warehouse, stock returns, or sector performance.",
    timestamp: '10:00 AM',
  },
];

const AiAssistantPage = () => {
  const [inputQuery, setInputQuery] = useState('');
  const [messages, setMessages] = useState(INITIAL_MESSAGES);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    try {
      const res = await api.get('/ai/history');
      if (res.data && res.data.success) {
        setHistory(res.data.data || []);
      }
    } catch (e) {
      console.error('Failed to fetch AI history', e);
    }
  };

  const handleSend = async (queryToSend) => {
    const text = queryToSend || inputQuery;
    if (!text.trim()) return;

    const userMsg = {
      sender: 'user',
      text,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMsg]);
    setInputQuery('');
    setLoading(true);

    try {
      const res = await api.post('/ai/query', { question: text });
      if (res.data && res.data.success) {
        const aiData = res.data.data;
        const aiMsg = {
          sender: 'ai',
          text: aiData.explanation || `Retrieved ${aiData.rowCount} record(s) in ${aiData.executionTimeMs} ms.`,
          sql: aiData.generatedSql,
          results: aiData.results,
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        };
        setMessages((prev) => [...prev, aiMsg]);
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Failed to execute AI query.';
      setMessages((prev) => [
        ...prev,
        {
          sender: 'ai',
          text: `⚠️ ${errorMsg}`,
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        },
      ]);
    } finally {
      fetchHistory();
      setLoading(false);
    }
  };

  return (
    <MainLayout pageTitle="AI Market Copilot (Gemini Integration)">
      <Grid container spacing={3}>
        {/* Left Column: Terminal Chat Window */}
        <Grid item xs={12} md={8}>
          <Card sx={{ p: 3, display: 'flex', flexDirection: 'column', height: '78vh' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, pb: 2, borderBottom: '1px solid rgba(255, 255, 255, 0.08)' }}>
              <Avatar sx={{ bgcolor: 'rgba(139, 92, 246, 0.2)', color: '#A78BFA' }}>
                <SmartToyIcon />
              </Avatar>
              <Box>
                <Typography variant="h6" sx={{ color: '#F8FAFC', fontWeight: 700 }}>
                  FinSight Natural Language Query Terminal
                </Typography>
                <Typography variant="caption" sx={{ color: '#06B6D4', fontWeight: 700 }}>
                  Google Gemini Engine + AST JSQLParser Security
                </Typography>
              </Box>
            </Box>

            {/* Prompt Chips */}
            <Box sx={{ py: 1.5, display: 'flex', gap: 1, overflowX: 'auto' }}>
              {SAMPLE_PROMPTS.map((prompt, i) => (
                <Chip
                  key={i}
                  icon={<AutoAwesomeIcon style={{ fontSize: 12 }} />}
                  label={prompt}
                  clickable
                  onClick={() => handleSend(prompt)}
                  sx={{
                    bgcolor: 'rgba(6, 182, 212, 0.1)',
                    color: '#38BDF8',
                    fontSize: '0.75rem',
                    borderRadius: 2,
                    border: '1px solid rgba(6, 182, 212, 0.2)',
                  }}
                />
              ))}
            </Box>

            <Divider sx={{ borderColor: 'rgba(255, 255, 255, 0.06)' }} />

            {/* Chat Message Stream */}
            <Box sx={{ flexGrow: 1, overflowY: 'auto', py: 2.5, px: 1 }}>
              {messages.map((msg, index) => (
                <Box
                  key={index}
                  sx={{
                    display: 'flex',
                    flexDirection: msg.sender === 'user' ? 'row-reverse' : 'row',
                    gap: 1.5,
                    mb: 2.5,
                  }}
                >
                  <Avatar
                    sx={{
                      width: 34,
                      height: 34,
                      bgcolor: msg.sender === 'user' ? '#06B6D4' : '#8B5CF6',
                      fontSize: '0.8rem',
                    }}
                  >
                    {msg.sender === 'user' ? <PersonIcon fontSize="small" /> : <SmartToyIcon fontSize="small" />}
                  </Avatar>

                  <Paper
                    sx={{
                      p: 2,
                      maxWidth: '85%',
                      borderRadius: 3,
                      bgcolor: msg.sender === 'user' ? '#06B6D4' : '#1E293B',
                      color: '#F8FAFC',
                      border: msg.sender === 'user' ? 'none' : '1px solid rgba(255,255,255,0.08)',
                    }}
                  >
                    <Typography variant="body2" sx={{ lineHeight: 1.6 }}>
                      {msg.text}
                    </Typography>

                    {/* Display Generated Executed SQL */}
                    {msg.sql && (
                      <Box sx={{ mt: 1.5, p: 1.5, borderRadius: 2, bgcolor: '#0B0F19', border: '1px solid rgba(6, 182, 212, 0.3)' }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8, color: '#06B6D4', mb: 0.5 }}>
                          <TerminalIcon sx={{ fontSize: 16 }} />
                          <Typography variant="caption" sx={{ fontWeight: 800 }}>
                            GENERATED SQL QUERY
                          </Typography>
                        </Box>
                        <Typography variant="caption" sx={{ fontFamily: 'monospace', color: '#38BDF8', display: 'block', wordBreak: 'break-all' }}>
                          {msg.sql}
                        </Typography>
                      </Box>
                    )}

                    {/* Display Tabular Query Results */}
                    {msg.results && msg.results.length > 0 && (
                      <TableContainer sx={{ mt: 1.5, maxHeight: 180 }}>
                        <Table size="small">
                          <TableHead>
                            <TableRow>
                              {Object.keys(msg.results[0]).map((key) => (
                                <TableCell key={key} sx={{ color: '#94A3B8', fontSize: '0.7rem' }}>
                                  {key.toUpperCase()}
                                </TableCell>
                              ))}
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            {msg.results.map((row, rIdx) => (
                              <TableRow key={rIdx}>
                                {Object.values(row).map((val, cIdx) => (
                                  <TableCell key={cIdx} sx={{ fontSize: '0.75rem', color: '#F8FAFC' }}>
                                    {String(val)}
                                  </TableCell>
                                ))}
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>
                      </TableContainer>
                    )}

                    <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.5)', display: 'block', mt: 0.8, fontSize: '0.65rem' }}>
                      {msg.timestamp}
                    </Typography>
                  </Paper>
                </Box>
              ))}

              {loading && (
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, color: '#06B6D4', p: 1 }}>
                  <CircularProgress size={20} color="inherit" />
                  <Typography variant="body2">AI Engine translating Natural Language to SQL...</Typography>
                </Box>
              )}
            </Box>

            {/* Input Box */}
            <Box
              component="form"
              onSubmit={(e) => {
                e.preventDefault();
                handleSend();
              }}
              sx={{ display: 'flex', gap: 1.5, pt: 2, borderTop: '1px solid rgba(255, 255, 255, 0.08)' }}
            >
              <TextField
                fullWidth
                size="small"
                placeholder="Ask AI anything about market data, stocks, or sectors..."
                value={inputQuery}
                onChange={(e) => setInputQuery(e.target.value)}
                disabled={loading}
              />
              <IconButton type="submit" disabled={loading || !inputQuery.trim()} sx={{ bgcolor: '#06B6D4', color: '#FFF', '&:hover': { bgcolor: '#0891B2' } }}>
                <SendIcon />
              </IconButton>
            </Box>
          </Card>
        </Grid>

        {/* Right Column: Query History Side Panel */}
        <Grid item xs={12} md={4}>
          <Card sx={{ p: 3, height: '78vh', overflowY: 'auto' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
              <HistoryIcon sx={{ color: '#A78BFA' }} />
              <Typography variant="h6" sx={{ color: '#F8FAFC', fontWeight: 700 }}>
                AI Query Audit History
              </Typography>
            </Box>

            <Divider sx={{ borderColor: 'rgba(255, 255, 255, 0.08)', mb: 2 }} />

            {history.length > 0 ? (
              history.map((item) => (
                <Paper
                  key={item.id}
                  onClick={() => handleSend(item.question)}
                  sx={{
                    p: 1.8,
                    mb: 1.5,
                    cursor: 'pointer',
                    bgcolor: '#1E293B',
                    borderRadius: 2,
                    transition: 'all 0.2s ease',
                    '&:hover': { bgcolor: 'rgba(6, 182, 212, 0.15)', borderColor: '#06B6D4' },
                  }}
                >
                  <Typography variant="body2" sx={{ color: '#F8FAFC', fontWeight: 600 }}>
                    "{item.question}"
                  </Typography>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 1 }}>
                    <Typography variant="caption" sx={{ color: '#06B6D4' }}>
                      {item.rowCount} row(s) in {item.executionTimeMs} ms
                    </Typography>
                    <Typography variant="caption" sx={{ color: '#64748B' }}>
                      {new Date(item.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </Typography>
                  </Box>
                </Paper>
              ))
            ) : (
              <Typography variant="body2" sx={{ color: '#64748B', textAlign: 'center', pt: 4 }}>
                No past AI queries recorded yet.
              </Typography>
            )}
          </Card>
        </Grid>
      </Grid>
    </MainLayout>
  );
};

export default AiAssistantPage;
