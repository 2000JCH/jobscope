import HistoryItem from './HistoryItem';
import styles from './HistoryTimeline.module.css';

function HistoryTimeline({ histories, onEdit, onDelete }) {
  if (!histories || histories.length === 0) {
    return <p className={styles.empty}>전형 단계가 없습니다.</p>;
  }

  return (
    <div className={styles.timeline}>
      {histories.map((h) => (
        <HistoryItem key={h.id} history={h} onEdit={onEdit} onDelete={onDelete} />
      ))}
    </div>
  );
}

export default HistoryTimeline;