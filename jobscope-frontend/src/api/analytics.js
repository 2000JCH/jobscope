import instance from './axios';

export const fetchAnalytics = (params = {}) =>
  instance.get('/api/analytics', { params });