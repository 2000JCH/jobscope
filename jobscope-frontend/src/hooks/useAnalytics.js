import { useState, useEffect, useRef } from 'react';
import { fetchAnalytics } from '../api/analytics';

export function useAnalytics() {
  const [jobPosition, setJobPosition] = useState('');
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const isFirstRender = useRef(true);

  useEffect(() => {
    let cancelled = false;
    const delay = isFirstRender.current ? 0 : 500;
    isFirstRender.current = false;

    const timerId = setTimeout(() => {
      const params = {};
      if (jobPosition.trim()) params.jobPosition = jobPosition.trim();

      setLoading(true);
      setError(null);
      fetchAnalytics(params)
        .then((res) => { if (!cancelled) setAnalytics(res.data.data); })
        .catch(() => { if (!cancelled) setError('분석 데이터를 불러오지 못했습니다.'); })
        .finally(() => { if (!cancelled) setLoading(false); });
    }, delay);

    return () => {
      cancelled = true;
      clearTimeout(timerId);
    };
  }, [jobPosition]);

  return { analytics, loading, error, jobPosition, setJobPosition };
}