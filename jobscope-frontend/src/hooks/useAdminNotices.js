import { useState, useEffect, useCallback } from 'react';
import {
  fetchAdminNotices,
  createAdminNotice,
  updateAdminNotice,
  deleteAdminNotice,
} from '../api/admin';

export function useAdminNotices() {
  const [notices, setNotices] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(() => {
    setLoading(true);
    fetchAdminNotices()
      .then((res) => setNotices(res.data.data))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { load(); }, [load]);

  const create = useCallback((data) => createAdminNotice(data).then(load), [load]);
  const update = useCallback((id, data) => updateAdminNotice(id, data).then(load), [load]);
  const remove = useCallback((id) => deleteAdminNotice(id).then(load), [load]);

  return { notices, loading, create, update, remove };
}