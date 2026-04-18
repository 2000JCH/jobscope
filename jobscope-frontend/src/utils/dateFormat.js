export function formatDate(dateStr) {
  if (!dateStr) return '';
  return dateStr.slice(0, 10);
}

export function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return '';
  return dateTimeStr.slice(0, 16).replace('T', ' ');
}

export function formatTime(dateTimeStr) {
  if (!dateTimeStr) return '';
  return dateTimeStr.slice(11, 16);
}

export function formatDDay(dDay) {
  if (dDay === null || dDay === undefined) return '—';
  if (dDay === 0) return 'D-DAY';
  if (dDay > 0) return `D-${dDay}`;
  return `D+${Math.abs(dDay)}`;
}