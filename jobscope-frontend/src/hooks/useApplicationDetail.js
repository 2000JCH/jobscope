import { useState, useCallback, useRef } from 'react';
import { fetchApplication } from '../api/application';

export function useApplicationDetail() {
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const currentIdRef = useRef(null);

  const load = useCallback((applicationId) => {
    if (!applicationId) return;
    currentIdRef.current = applicationId;
    setLoading(true);
    setError(null);
    fetchApplication(applicationId)
      .then((res) => setDetail(res.data.data))
      .catch(() => setError('상세 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  const reload = useCallback(() => {
    if (currentIdRef.current) load(currentIdRef.current);
  }, [load]);

  const reset = useCallback(() => {
    currentIdRef.current = null;
    setDetail(null);
    setError(null);
  }, []);

  return { detail, loading, error, load, reload, reset };
}
