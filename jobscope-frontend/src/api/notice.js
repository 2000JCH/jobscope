import api from './axios';

export const fetchLatestNotice = () => api.get('/api/notices/latest');