import { formatDateTime } from '../../utils/dateFormat';
import StageResultBadge from './StageResultBadge';
import styles from './HistoryItem.module.css';

function HistoryItem({ history, onEdit, onDelete }) {
  return (
    <div className={styles.item}>
      <div className={styles.left}>
        <div className={styles.dot} />
        <div className={styles.line} />
      </div>
      <div className={styles.right}>
        <div className={styles.top}>
          <span className={styles.stage}>{history.stage}</span>
          <StageResultBadge stageResult={history.stageResult} />
        </div>
        {history.scheduledAt && (
          <p className={styles.meta}>예정 {formatDateTime(history.scheduledAt)}</p>
        )}
        {history.completedAt && (
          <p className={styles.meta}>완료 {formatDateTime(history.completedAt)}</p>
        )}
        {history.memo && <p className={styles.memo}>{history.memo}</p>}
        <div className={styles.actions}>
          <button className={styles.editBtn} onClick={() => onEdit(history)}>수정</button>
          <button className={styles.deleteBtn} onClick={() => onDelete(history.id)}>삭제</button>
        </div>
      </div>
    </div>
  );
}

export default HistoryItem;