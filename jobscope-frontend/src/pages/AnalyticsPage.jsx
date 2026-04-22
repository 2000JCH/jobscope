import { useAnalytics } from '../hooks/useAnalytics';
import FunnelChart from '../components/analytics/FunnelChart';
import StageDurationList from '../components/analytics/StageDurationList';
import styles from './AnalyticsPage.module.css';

function AnalyticsPage() {
  const { analytics, loading, error, jobPosition, setJobPosition } = useAnalytics();

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>분석</h1>

      <section className={styles.filterSection}>
        <input
          className={styles.jobInput}
          type="text"
          placeholder="직군으로 필터 (예: 마케팅, 개발)"
          value={jobPosition}
          onChange={(e) => setJobPosition(e.target.value)}
        />
      </section>

      {loading && <div className={styles.loading}>불러오는 중...</div>}
      {error && <div className={styles.error}>{error}</div>}

      {!loading && !error && analytics && (
        <>
          <section className={styles.section}>
            <div className={styles.trustChips}>
              <span className={styles.chip}>분석 대상 {analytics.trustMetrics.totalCount}건</span>
              {analytics.trustMetrics.noHistoryCount > 0 && (
                <span className={`${styles.chip} ${styles.chipWarn}`}>
                  미입력 {analytics.trustMetrics.noHistoryCount}건
                </span>
              )}
              {analytics.trustMetrics.inProgressCount > 0 && (
                <span className={`${styles.chip} ${styles.chipMuted}`}>
                  진행중 {analytics.trustMetrics.inProgressCount}건
                </span>
              )}
            </div>
          </section>

          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>단계별 통과율</h2>
            <FunnelChart stages={analytics.funnelStages} />
          </section>

          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>구간별 소요 기간</h2>
            <StageDurationList durations={analytics.stageDurations} />
          </section>
        </>
      )}
    </div>
  );
}

export default AnalyticsPage;