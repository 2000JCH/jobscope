import styles from './FunnelChart.module.css';

function FunnelChart({ stages }) {
  if (!stages || stages.length === 0) {
    return <p className={styles.empty}>히스토리를 입력하면 단계별 통과율이 표시됩니다.</p>;
  }

  const maxTotal = Math.max(...stages.map((s) => s.passCount + s.failCount + s.pendingCount), 1);

  return (
    <div className={styles.funnel}>
      {stages.map((stage, idx) => {
        const total = stage.passCount + stage.failCount + stage.pendingCount;
        const barWidth = Math.round((total / maxTotal) * 100);

        return (
          <div key={`${idx}-${stage.stageName}`}>
            {idx > 0 && stage.pendingCount > 0 && (
              <div className={styles.pendingNote}>↓ 진행중 {stage.pendingCount}건 제외</div>
            )}
            <div className={`${styles.stageRow} ${stage.maxDrop ? styles.maxDrop : ''}`}>
              <div className={styles.stageName}>{stage.stageName}</div>
              <div className={styles.barWrap}>
                <div className={styles.bar} style={{ width: `${barWidth}%` }} />
              </div>
              <div className={styles.stageStats}>
                {stage.passRate !== null ? (
                  <>
                    <span className={styles.rate}>{stage.passRate}%</span>
                    <span className={styles.count}>
                      ({stage.passCount}/{stage.passCount + stage.failCount}건)
                    </span>
                  </>
                ) : (
                  <span className={styles.pending}>결과 대기</span>
                )}
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}

export default FunnelChart;