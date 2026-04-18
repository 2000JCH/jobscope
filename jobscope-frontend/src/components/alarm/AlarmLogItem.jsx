import styles from './AlarmLogItem.module.css';

const ALARM_TYPE_LABEL = {
  D7: 'D-7',
  D3: 'D-3',
  D1: 'D-1',
  DEADLINE: 'D-Day',
};

function AlarmLogItem({ log, isEditMode, isSelected, onToggle }) {
  const typeLabel = ALARM_TYPE_LABEL[log.alarmType] ?? log.alarmType;
  const sentDate = log.sentAt ? log.sentAt.slice(0, 10) : '';

  return (
    <div
      className={`${styles.item} ${isEditMode ? styles.clickable : ''} ${isEditMode && !isSelected ? styles.dimmed : ''}`}
      onClick={isEditMode ? onToggle : undefined}
    >
      {isEditMode && (
        <input
          type="checkbox"
          className={styles.checkbox}
          checked={isSelected}
          onChange={onToggle}
          onClick={(e) => e.stopPropagation()}
        />
      )}
      <div className={styles.content}>
        <span className={styles.company}>{log.companyName}</span>
        <span className={styles.meta}>{typeLabel} · {log.label}</span>
        <span className={styles.date}>{sentDate}</span>
      </div>
      {!isEditMode && (
        <span className={`${styles.status} ${log.isSuccess ? styles.statusSuccess : styles.statusFail}`}>
          {log.isSuccess ? '발송 완료' : '발송 실패'}
        </span>
      )}
    </div>
  );
}

export default AlarmLogItem;