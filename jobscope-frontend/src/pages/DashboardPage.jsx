import { useState } from 'react';
import { useDashboard } from '../hooks/useDashboard';
import SummaryCards from '../components/dashboard/SummaryCards';
import NudgeCard from '../components/dashboard/NudgeCard';
import WeekScheduleList from '../components/dashboard/WeekScheduleList';
import ImminentDeadlineList from '../components/dashboard/ImminentDeadlineList';
import ApplicationDetailBottomSheet from '../components/application/ApplicationDetailBottomSheet';
import styles from './DashboardPage.module.css';

function DashboardPage() {
  const { dashboard, loading, error } = useDashboard();
  const [selectedApplicationId, setSelectedApplicationId] = useState(null);

  if (loading) return <div className={styles.loading}>불러오는 중...</div>;
  if (error) return <div className={styles.error}>{error}</div>;
  if (!dashboard) return null;

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>대시보드</h1>

      <section className={styles.section}>
        <SummaryCards summary={dashboard.summary} />
        <NudgeCard count={dashboard.unstatedCount} />
      </section>

      <section className={styles.section}>
        <h2 className={styles.sectionTitle}>이번 주 면접 일정</h2>
        <WeekScheduleList
          schedules={dashboard.thisWeekSchedules}
          onClickApplication={setSelectedApplicationId}
        />
      </section>

      <section className={styles.section}>
        <h2 className={styles.sectionTitle}>마감 임박 (D-3 이내)</h2>
        <ImminentDeadlineList
          deadlines={dashboard.imminentDeadlines}
          onClickApplication={setSelectedApplicationId}
        />
      </section>

      <ApplicationDetailBottomSheet
        isOpen={!!selectedApplicationId}
        applicationId={selectedApplicationId}
        onClose={() => setSelectedApplicationId(null)}
        onDeleted={() => setSelectedApplicationId(null)}
        onUpdated={() => {}}
      />
    </div>
  );
}

export default DashboardPage;