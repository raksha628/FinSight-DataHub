import api from './api';

export const analyticsService = {
  getTopGainers: async (params = {}) => {
    const response = await api.get('/analytics/top-gainers', { params });
    return response.data;
  },

  getTopLosers: async (params = {}) => {
    const response = await api.get('/analytics/top-losers', { params });
    return response.data;
  },

  getHighestVolume: async (params = {}) => {
    const response = await api.get('/analytics/volume', { params });
    return response.data;
  },

  getAveragePriceBySector: async (date) => {
    const response = await api.get('/analytics/sector-avg-price', { params: { date } });
    return response.data;
  },

  getDailyReturns: async (params = {}) => {
    const response = await api.get('/analytics/returns/daily', { params });
    return response.data;
  },

  getWeeklyReturns: async (params = {}) => {
    const response = await api.get('/analytics/returns/weekly', { params });
    return response.data;
  },

  getMonthlyReturns: async (params = {}) => {
    const response = await api.get('/analytics/returns/monthly', { params });
    return response.data;
  },

  getMovingAverages: async (params = {}) => {
    const response = await api.get('/analytics/moving-average', { params });
    return response.data;
  },

  getHighestClose: async (params = {}) => {
    const response = await api.get('/analytics/highest-close', { params });
    return response.data;
  },

  getLowestClose: async (params = {}) => {
    const response = await api.get('/analytics/lowest-close', { params });
    return response.data;
  },

  getMostActive: async (params = {}) => {
    const response = await api.get('/analytics/most-active', { params });
    return response.data;
  },
};
