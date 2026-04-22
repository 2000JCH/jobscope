import styles from './StageDurationList.module.css';

function StageDurationList({ durations }) {
  if (!durations || durations.length === 0) {
    return <p className={styles.empty}>구간 소요 기간 데이터가 없습니다.</p>;
  }

  return (
    <ul className={styles.list}>
      {durations.map((d, idx) => (
        <li key={`${idx}-${d.fromStage}-${d.toStage}`} className={styles.item}>
          <span className={styles.stages}>
            {d.fromStage} → {d.toStage}
          </span>
          <span className={styles.avg}>평균 {d.avgDays}일</span>
          <span className={styles.sample}>{d.sampleCount}건 기준</span>
        </li>
      ))}
    </ul>
  );
}

export default StageDurationList;