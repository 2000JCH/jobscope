import instance from './axios';

export const updateAlarmEnabled = (applicationId, data) =>
  instance.patch(`/api/applications/${applicationId}`, data);

export const fetchAlarmLogs = (params = {}) =>
  instance.get('/api/alarms', { params });

export const deleteAlarmLogs = (ids) =>
  instance.delete('/api/alarms', { data: ids });