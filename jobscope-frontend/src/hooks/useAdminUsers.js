import { useState, useEffect, useCallback } from 'react';
import { fetchAdminUsers, deleteAdminUser } from '../api/admin';

export function useAdminUsers() {
  const [users, setUsers] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);

  const load = useCallback((page = 0) => {
    setLoading(true);
    fetchAdminUsers(page)
      .then((res) => {
        const { content, totalPages: tp } = res.data.data;
        setUsers(content);
        setTotalPages(tp);
        setCurrentPage(page);
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { load(0); }, [load]);

  const deleteUser = useCallback((userId) => {
    return deleteAdminUser(userId).then(() => load(currentPage));
  }, [currentPage, load]);

  return { users, loading, totalPages, currentPage, load, deleteUser };
}