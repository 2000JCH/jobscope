import { formatDateTime, formatDDay } from '../../utils/dateFormat';
import styles from './ImminentDeadlineList.module.css';

function ImminentDeadlineList({ deadlines, onClickApplication }) {
  if (!deadlines || deadlines.length === 0) {
    return <p className={styles.empty}>마감 임박 공고가 없습니다.</p>;
  }

  return (
    <ul className={styles.list}>
      {deadlines.map((d) => (
        <li key={d.applicationId} className={styles.item} onClick={() => onClickApplication(d.applicationId)}>
          <div className={styles.info}>
            <span className={styles.company}>{d.companyName}</span>
            <span className={styles.deadline}>{formatDateTime(d.deadlineAt)}</span>
          </div>
          <span className={`${styles.dday} ${d.dDay <= 1 ? styles.urgent : ''}`}>
            {formatDDay(d.dDay)}
          </span>
        </li>
      ))}
    </ul>
  );
}

export default ImminentDeadlineList;