import { useJourney } from '../../hooks/useJourney';
import FunnelChart from '../analytics/FunnelChart';
import styles from './JourneySection.module.css';

const MONTH_LABELS = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'];

function MonthlyTimeline({ timeline }) {
  if (!timeline || timeline.length === 0) return null;

  const maxCount = Math.max(...timeline.map((m) => m.appliedCount + m.interviewCount), 1);

  return (
    <div className={styles.timeline}>
      {timeline.map((m) => {
        const [, month] = m.yearMonth.split('-');
        const barHeight = Math.round(((m.appliedCount + m.interviewCount) / maxCount) * 48);
        return (
          <div key={m.yearMonth} className={styles.monthCol}>
            <div className={styles.barArea} style={{ height: 48 }}>
              <div className={styles.bar} style={{ height: barHeight }}>
                {m.passedCount > 0 && <div className={styles.passedDot} />}
              </div>
            </div>
            <span className={styles.monthLabel}>{MONTH_LABELS[parseInt(month, 10) - 1]}</span>
            <span className={styles.monthCount}>{m.appliedCount}</span>
          </div>
        );
      })}
    </div>
  );
}

function JourneySection() {
  const { journey, loading, error } = useJourney();

  if (loading) return <div className={styles.empty}>불러오는 중...</div>;
  if (error) return <div className={styles.errorMsg}>{error}</div>;
  if (!journey) return null;

  const isPassed = journey.status === 'PASSED';
  const hasFunnel = journey.funnelStages && journey.funnelStages.length > 0;

  return (
    <div className={styles.wrap}>
      {/* 상태 헤더 */}
      <div className={styles.header}>
        <span className={`${styles.badge} ${isPassed ? styles.badgePassed : styles.badgeInProgress}`}>
          {isPassed ? '최종 합격' : '취준 중'}
        </span>
        <span className={styles.period}>
          {journey.startDate} ~ {isPassed ? journey.endDate : '현재'}
        </span>
        <span className={styles.days}>{journey.journeyDays}일째</span>
      </div>

      {/* 요약 */}
      <div className={styles.summaryRow}>
        <div className={styles.summaryItem}>
          <span className={styles.summaryValue}>{journey.totalCount}</span>
          <span className={styles.summaryLabel}>총 지원</span>
        </div>
        <div className={styles.summaryItem}>
          <span className={`${styles.summaryValue} ${styles.passed}`}>{journey.passedCount}</span>
          <span className={styles.summaryLabel}>합격</span>
        </div>
        <div className={styles.summaryItem}>
          <span className={`${styles.summaryValue} ${styles.failed}`}>{journey.failedCount}</span>
          <span className={styles.summaryLabel}>탈락</span>
        </div>
        <div className={styles.summaryItem}>
          <span className={styles.summaryValue}>{journey.inProgressCount}</span>
          <span className={styles.summaryLabel}>진행중</span>
        </div>
      </div>

      {/* 단계별 퍼널 */}
      {hasFunnel && (
        <div className={styles.block}>
          <p className={styles.blockTitle}>단계별 통과율</p>
          <FunnelChart stages={journey.funnelStages} />
        </div>
      )}

      {/* 월별 타임라인 */}
      {journey.monthlyTimeline && journey.monthlyTimeline.length > 0 && (
        <div className={styles.block}>
          <p className={styles.blockTitle}>월별 지원 현황</p>
          <MonthlyTimeline timeline={journey.monthlyTimeline} />
          <div className={styles.legend}>
            <span className={styles.legendItem}><span className={styles.dotBlue} />지원+면접</span>
            <span className={styles.legendItem}><span className={styles.dotGreen} />합격</span>
          </div>
        </div>
      )}

      {!hasFunnel && journey.totalCount === 0 && (
        <p className={styles.empty}>지원 데이터가 쌓이면 여정이 표시됩니다.</p>
      )}
    </div>
  );
}

export default JourneySection;