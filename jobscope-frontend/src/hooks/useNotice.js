import { useState, useEffect } from 'react';
import { fetchLatestNotice } from '../api/notice';

export function useNotice() {
  const [notice, setNotice] = useState(null);

  useEffect(() => {
    fetchLatestNotice()
      .then((res) => {
        setNotice(res.data.data);
      })
      .catch(() => {});
  }, []);

  return { notice };
}