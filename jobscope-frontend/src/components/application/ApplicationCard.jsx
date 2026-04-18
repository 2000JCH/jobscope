import { formatDate, formatDDay } from '../../utils/dateFormat';
import ResultBadge from './ResultBadge';
import styles from './ApplicationCard.module.css';

function ApplicationCard({ application, onClick }) {
  const { companyName, jobPosition, appliedAt, currentStage, result, dDay } = application;

  return (
    <button className={styles.card} onClick={onClick}>
      <div className={styles.top}>
        <span className={styles.company}>{companyName}</span>
        <ResultBadge result={result} />
      </div>
      <div className={styles.middle}>
        <span className={styles.position}>{jobPosition}</span>
        <span className={styles.stage}>{currentStage}</span>
      </div>
      <div className={styles.bottom}>
        <span className={styles.appliedAt}>지원일 {formatDate(appliedAt)}</span>
        {dDay !== null && dDay !== undefined && (
          <span className={`${styles.dday} ${dDay <= 0 ? styles.urgent : ''}`}>
            {formatDDay(dDay)}
          </span>
        )}
      </div>
    </button>
  );
}

export default ApplicationCard;
