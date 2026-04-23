import { useState, useEffect, useCallback } from 'react';
import { fetchLatestNotice } from '../api/notice';

const STORAGE_KEY = 'dismissedNoticeId';

export function useNotice() {
  const [notice, setNotice] = useState(null);
  const [dismissedId, setDismissedId] = useState(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    return saved ? Number(saved) : null;
  });

  useEffect(() => {
    fetchLatestNotice()
      .then((res) => setNotice(res.data.data))
      .catch(() => {});
  }, []);

  const hasUnread = notice !== null && dismissedId !== notice.id;

  const markAsRead = useCallback(() => {
    if (!notice) return;
    localStorage.setItem(STORAGE_KEY, String(notice.id));
    setDismissedId(notice.id);
  }, [notice]);

  return { notice, hasUnread, markAsRead };
}