import { useState, useEffect, useCallback } from 'react';
import { fetchApplications } from '../api/application';

export function useApplicationList() {
  const [applications, setApplications] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [search, setSearch] = useState('');
  const [results, setResults] = useState([]);
  const [sort, setSort] = useState('LATEST');
  const [page, setPage] = useState(0);
  const size = 20;

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    const params = { sort, page, size };
    if (search) { params.search = search; }
    if (results.length > 0) { params.result = results; }

    fetchApplications(params)
      .then((res) => {
        const data = res.data.data;
        setApplications(data.content);
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      })
      .catch(() => setError('목록을 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, [search, results, sort, page, size]);

  useEffect(() => {
    load();
  }, [load]);

  return {
    applications, totalPages, totalElements, loading, error,
    search, setSearch,
    results, setResults,
    sort, setSort,
    page, setPage,
    reload: load,
  };
}
