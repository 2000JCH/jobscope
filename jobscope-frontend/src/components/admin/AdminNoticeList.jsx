import { useState } from 'react';
import { useAdminNotices } from '../../hooks/useAdminNotices';
import AdminNoticeForm from './AdminNoticeForm';
import styles from './AdminNoticeList.module.css';

function AdminNoticeList() {
  const { notices, loading, create, update, remove } = useAdminNotices();
  const [editingId, setEditingId] = useState(null);

  if (loading) return <div className={styles.empty}>불러오는 중...</div>;

  return (
    <div className={styles.wrapper}>
      <div className={styles.createSection}>
        <h4 className={styles.subTitle}>새 공지 등록</h4>
        <AdminNoticeForm onSubmit={create} />
      </div>

      <div className={styles.list}>
        <h4 className={styles.subTitle}>공지 목록</h4>
        {notices.length === 0 && <p className={styles.empty}>등록된 공지가 없습니다.</p>}
        {notices.map((n) => (
          <div key={n.id} className={styles.item}>
            {editingId === n.id ? (
              <AdminNoticeForm
                initial={n}
                onSubmit={(data) => update(n.id, data).then(() => setEditingId(null))}
                onCancel={() => setEditingId(null)}
              />
            ) : (
              <>
                <div className={styles.itemHeader}>
                  <span className={styles.itemTitle}>{n.title}</span>
                  <span className={`${styles.badge} ${n.isActive ? styles.badgeActive : styles.badgeInactive}`}>
                    {n.isActive ? '활성' : '비활성'}
                  </span>
                </div>
                <p className={styles.itemContent}>{n.content}</p>
                <div className={styles.itemMeta}>
                  <span>{n.createdAt?.slice(0, 10)}</span>
                  <div className={styles.itemActions}>
                    <button className={styles.editBtn} onClick={() => setEditingId(n.id)}>수정</button>
                    <button className={styles.deleteBtn} onClick={() => {
                      if (window.confirm('공지를 삭제하시겠습니까?')) remove(n.id);
                    }}>삭제</button>
                  </div>
                </div>
              </>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

export default AdminNoticeList;