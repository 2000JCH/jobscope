import { useState } from 'react';
import { useAdminUsers } from '../../hooks/useAdminUsers';
import styles from './AdminUserTable.module.css';

function AdminUserTable() {
  const { users, loading, totalPages, currentPage, load, deleteUser } = useAdminUsers();
  const [confirmId, setConfirmId] = useState(null);

  const handleDelete = (userId) => {
    deleteUser(userId)
      .then(() => setConfirmId(null))
      .catch(() => alert('삭제에 실패했습니다.'));
  };

  if (loading) return <div className={styles.empty}>불러오는 중...</div>;

  return (
    <div className={styles.wrapper}>
      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>닉네임</th>
              <th>이메일</th>
              <th>역할</th>
              <th>마지막 로그인</th>
              <th>가입일</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {users.length === 0 && (
              <tr><td colSpan={7} className={styles.empty}>사용자가 없습니다.</td></tr>
            )}
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.id}</td>
                <td>{u.nickname}</td>
                <td>{u.email ?? '—'}</td>
                <td>
                  <span className={`${styles.badge} ${u.role === 'ADMIN' ? styles.badgeAdmin : styles.badgeUser}`}>
                    {u.role}
                  </span>
                </td>
                <td>{u.lastLoginAt ? u.lastLoginAt.slice(0, 10) : '—'}</td>
                <td>{u.createdAt?.slice(0, 10)}</td>
                <td>
                  {confirmId === u.id ? (
                    <div className={styles.confirmRow}>
                      <button className={styles.confirmBtn} onClick={() => handleDelete(u.id)}>확인</button>
                      <button className={styles.cancelBtn} onClick={() => setConfirmId(null)}>취소</button>
                    </div>
                  ) : (
                    <button className={styles.deleteBtn} onClick={() => setConfirmId(u.id)}>삭제</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {totalPages > 1 && (
        <div className={styles.pagination}>
          <button onClick={() => load(currentPage - 1)} disabled={currentPage === 0}>이전</button>
          <span>{currentPage + 1} / {totalPages}</span>
          <button onClick={() => load(currentPage + 1)} disabled={currentPage >= totalPages - 1}>다음</button>
        </div>
      )}
    </div>
  );
}

export default AdminUserTable;