import { useState, useEffect } from 'react';
import { getJourney } from '../api/user';

export function useJourney() {
  const [journey, setJourney] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    getJourney()
      .then((res) => { if (!cancelled) setJourney(res.data.data); })
      .catch(() => { if (!cancelled) setError('취준 여정을 불러오지 못했습니다.'); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, []);

  return { journey, loading, error };
}