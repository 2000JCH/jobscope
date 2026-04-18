import { formatDateTime } from '../../utils/dateFormat';
import styles from './WeekScheduleList.module.css';

function WeekScheduleList({ schedules, onClickApplication }) {
  if (!schedules || schedules.length === 0) {
    return <p className={styles.empty}>이번 주 예정된 일정이 없습니다.</p>;
  }

  return (
    <ul className={styles.list}>
      {schedules.map((s) => (
        <li
          key={`${s.applicationId}-${s.scheduledAt}`}
          className={styles.item}
          role="button"
          tabIndex={0}
          onClick={() => onClickApplication(s.applicationId)}
          onKeyDown={(e) => e.key === 'Enter' && onClickApplication(s.applicationId)}
        >
          <div className={styles.info}>
            <span className={styles.company}>{s.companyName}</span>
            <span className={styles.stage}>{s.stage}</span>
          </div>
          <span className={styles.time}>{formatDateTime(s.scheduledAt)}</span>
        </li>
      ))}
    </ul>
  );
}

export default WeekScheduleList;