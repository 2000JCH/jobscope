import instance from './axios';

export const updateAlarmEnabled = (applicationId, data) =>
  instance.patch(`/api/applications/${applicationId}/alarm`, data);

export const fetchAlarmLogs = () =>
  instance.get('/api/alarms');