import { useState, useEffect } from 'react';
import { fetchAdminStats } from '../api/admin';

export function useAdminStats() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchAdminStats()
      .then((res) => setStats(res.data.data))
      .catch(() => setError('통계를 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  return { stats, loading, error };
}