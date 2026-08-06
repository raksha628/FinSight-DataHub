import api from './api';

export const uploadService = {
  uploadCsv: async (file, assetType) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('assetType', assetType);

    const response = await api.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  getUploadHistory: async () => {
    const response = await api.get('/upload/history');
    return response.data;
  },

  getUploadDetails: async (id) => {
    const response = await api.get(`/upload/${id}`);
    return response.data;
  },
};
