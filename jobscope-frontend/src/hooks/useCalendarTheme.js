import { useState } from 'react';

export const CALENDAR_THEMES = [
  { id: 'ocean',    label: 'Ocean',    color: '#2563eb', todayBg: 'rgba(37,99,235,0.12)',   btn: '#2563eb' },
  { id: 'forest',   label: 'Forest',   color: '#16a34a', todayBg: 'rgba(22,163,74,0.12)',   btn: '#16a34a' },
  { id: 'lavender', label: 'Lavender', color: '#7c3aed', todayBg: 'rgba(124,58,237,0.12)',  btn: '#7c3aed' },
  { id: 'sunset',   label: 'Sunset',   color: '#ea580c', todayBg: 'rgba(234,88,12,0.12)',   btn: '#ea580c' },
  { id: 'slate',    label: 'Slate',    color: '#475569', todayBg: 'rgba(71,85,105,0.12)',   btn: '#475569' },
];

export function useCalendarTheme() {
  const [themeId, setThemeId] = useState(() => localStorage.getItem('cal-theme') || 'ocean');

  const updateTheme = (id) => {
    setThemeId(id);
    localStorage.setItem('cal-theme', id);
  };

  return { themeId, setTheme: updateTheme };
}