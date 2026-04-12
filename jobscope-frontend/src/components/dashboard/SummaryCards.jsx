import styles from './SummaryCards.module.css';

function SummaryCards({ summary }) {
  const items = [
    { label: '전체', value: summary.total, color: styles.total },
    { label: '진행중', value: summary.inProgress, color: styles.inProgress },
    { label: '합격', value: summary.passed, color: styles.passed },
    { label: '탈락', value: summary.failed, color: styles.failed },
  ];

  return (
    <div className={styles.grid}>
      {items.map((item) => (
        <div key={item.label} className={`${styles.card} ${item.color}`}>
          <span className={styles.value}>{item.value}</span>
          <span className={styles.label}>{item.label}</span>
        </div>
      ))}
    </div>
  );
}

export default SummaryCards;