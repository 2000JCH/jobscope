import { formatDate, formatDDay } from '../../utils/dateFormat';
import ResultBadge from './ResultBadge';
import { GripVertical } from 'lucide-react';
import styles from './ApplicationCard.module.css';

function ApplicationCard({ application, onClick, isEditMode, isSelected, onToggle, dragHandleProps, dragRef, dragStyle }) {
  const { companyName, jobPosition, appliedAt, currentStage, result, dDay } = application;

  if (isEditMode) {
    return (
      <div
        className={`${styles.card} ${isSelected ? styles.selected : ''}`}
        onClick={onToggle}
      >
        <input
          type="checkbox"
          checked={isSelected}
          onChange={onToggle}
          onClick={(e) => e.stopPropagation()}
          className={styles.checkbox}
        />
        <div className={styles.content}>
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
        </div>
      </div>
    );
  }

  return (
    <div ref={dragRef} style={dragStyle} className={styles.card} onClick={onClick}>
      <span {...dragHandleProps} className={styles.dragHandle} onClick={(e) => e.stopPropagation()}>
        <GripVertical size={16} />
      </span>
      <div className={styles.content}>
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
      </div>
    </div>
  );
}

export default ApplicationCard;