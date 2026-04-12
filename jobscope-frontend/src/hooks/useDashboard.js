import { useState, useEffect } from 'react';
import { fetchDashboard } from '../api/application';

export function useDashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    fetchDashboard()
      .then((res) => setDashboard(res.data.data))
      .catch(() => setError('대시보드를 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  return { dashboard, loading, error };
}
