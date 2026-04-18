import instance from './axios';

export const fetchApplications = (params) =>
  instance.get('/api/applications', {
    params,
    paramsSerializer: (p) => {
      const sp = new URLSearchParams();
      Object.entries(p).forEach(([k, v]) => {
        if (Array.isArray(v)) v.forEach((item) => sp.append(k, item));
        else if (v != null) sp.append(k, v);
      });
      return sp.toString();
    },
  });

export const fetchApplication = (applicationId) =>
  instance.get(`/api/applications/${applicationId}`);

export const createApplication = (data) =>
  instance.post('/api/applications', data);

export const updateApplication = (applicationId, data) =>
  instance.patch(`/api/applications/${applicationId}`, data);

export const deleteApplication = (applicationId) =>
  instance.delete(`/api/applications/${applicationId}`);

export const fetchDashboard = () =>
  instance.get('/api/applications/dashboard');

export const fetchCalendar = (params) =>
  instance.get('/api/applications/calendar', { params });

export const fetchHistories = (applicationId) =>
  instance.get(`/api/applications/${applicationId}/histories`);

export const createHistory = (applicationId, data) =>
  instance.post(`/api/applications/${applicationId}/histories`, data);

export const updateHistory = (applicationId, historyId, data) =>
  instance.patch(`/api/applications/${applicationId}/histories/${historyId}`, data);

export const deleteHistory = (applicationId, historyId) =>
  instance.delete(`/api/applications/${applicationId}/histories/${historyId}`);