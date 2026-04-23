import { useState } from 'react';
import { X } from 'lucide-react';
import { useNotice } from '../../hooks/useNotice';
import styles from './NoticeBanner.module.css';

function NoticeBanner() {
  const { notice } = useNotice();
  const [dismissed, setDismissed] = useState(() => {
    const saved = localStorage.getItem('dismissedNoticeId');
    return saved ? Number(saved) : null;
  });

  const handleDismiss = () => {
    localStorage.setItem('dismissedNoticeId', String(notice.id));
    setDismissed(notice.id);
  };

  if (!notice || dismissed === notice.id) return null;

  return (
    <div className={styles.banner}>
      <div className={styles.body}>
        <strong className={styles.title}>{notice.title}</strong>
        <p className={styles.content}>{notice.content}</p>
      </div>
      <button className={styles.close} onClick={handleDismiss} aria-label="공지 닫기">
        <X size={16} />
      </button>
    </div>
  );
}

export default NoticeBanner;