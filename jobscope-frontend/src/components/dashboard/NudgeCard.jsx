import { useNavigate } from 'react-router-dom';
import { ClipboardList } from 'lucide-react';
import styles from './NudgeCard.module.css';

function NudgeCard({ count }) {
  const navigate = useNavigate();

  if (!count || count === 0) return null;

  return (
    <button type="button" className={styles.card} onClick={() => navigate('/applications')}>
      <ClipboardList size={18} className={styles.icon} />
      <span className={styles.text}>
        히스토리 미입력 지원이 <strong>{count}건</strong> 있어요
      </span>
      <span className={styles.arrow}>›</span>
    </button>
  );
}

export default NudgeCard;