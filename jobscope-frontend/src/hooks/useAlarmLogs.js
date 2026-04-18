import { useState, useEffect, useCallback } from 'react';
import { fetchAlarmLogs, deleteAlarmLogs } from '../api/alarm';

export function useAlarmLogs() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const load = useCallback((page = 0) => {
    setLoading(true);
    fetchAlarmLogs({ page, size: 20 })
      .then((res) => {
        const data = res.data.data;
        setLogs(data.content);
        setCurrentPage(data.currentPage);
        setTotalPages(data.totalPages);
      })
      .catch(() => {
        setLogs([]);
      })
      .finally(() => setLoading(false));
  }, []);

  const deleteAndReload = useCallback((ids, page = 0) => {
    return deleteAlarmLogs(ids).then(() => load(page));
  }, [load]);

  useEffect(() => {
    load(0);
  }, [load]);

  return { logs, loading, currentPage, totalPages, load, deleteAndReload };
}