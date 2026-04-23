import api from './axios';

export const fetchAdminStats = () => api.get('/api/admin/stats');

export const fetchAdminUsers = (page = 0, size = 20) =>
  api.get('/api/admin/users', { params: { page, size } });

export const deleteAdminUser = (userId) =>
  api.delete(`/api/admin/users/${userId}`);

export const fetchAdminNotices = () => api.get('/api/admin/notices');

export const createAdminNotice = (data) => api.post('/api/admin/notices', data);

export const updateAdminNotice = (id, data) =>
  api.patch(`/api/admin/notices/${id}`, data);

export const deleteAdminNotice = (id) => api.delete(`/api/admin/notices/${id}`);