import { useAdminStats } from '../../hooks/useAdminStats';
import styles from './AdminStatsSection.module.css';

function AdminStatsSection() {
  const { stats, loading, error } = useAdminStats();

  if (loading) return <div className={styles.loading}>불러오는 중...</div>;
  if (error) return <div className={styles.error}>{error}</div>;
  if (!stats) return null;

  return (
    <div className={styles.wrapper}>
      <section className={styles.section}>
        <h3 className={styles.sectionTitle}>사용자 현황</h3>
        <div className={styles.cards}>
          <div className={styles.card}>
            <span className={styles.label}>전체 가입자</span>
            <strong className={styles.value}>{stats.totalUsers.toLocaleString()}명</strong>
          </div>
          <div className={styles.card}>
            <span className={styles.label}>오늘 신규</span>
            <strong className={styles.value}>{stats.newUsersToday}명</strong>
          </div>
          <div className={styles.card}>
            <span className={styles.label}>활성 (30일)</span>
            <strong className={styles.value}>{stats.activeUsers.toLocaleString()}명</strong>
          </div>
        </div>

        {stats.dailyNewUsers.length > 0 && (
          <div className={styles.trendTable}>
            <h4 className={styles.trendTitle}>최근 7일 신규 가입</h4>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>날짜</th>
                  <th>가입자</th>
                </tr>
              </thead>
              <tbody>
                {stats.dailyNewUsers.map((d) => (
                  <tr key={d.date}>
                    <td>{d.date}</td>
                    <td>{d.count}명</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className={styles.section}>
        <h3 className={styles.sectionTitle}>오늘 알림 발송</h3>
        <div className={styles.cards}>
          <div className={styles.card}>
            <span className={styles.label}>발송 수</span>
            <strong className={styles.value}>{stats.totalAlarmsSentToday}건</strong>
          </div>
          <div className={`${styles.card} ${stats.totalAlarmsFailedToday > 0 ? styles.cardDanger : ''}`}>
            <span className={styles.label}>실패 수</span>
            <strong className={styles.value}>{stats.totalAlarmsFailedToday}건</strong>
          </div>
        </div>
      </section>

      <section className={styles.section}>
        <h3 className={styles.sectionTitle}>지원 현황 통계</h3>
        <div className={styles.cards}>
          <div className={styles.card}>
            <span className={styles.label}>사용자당 평균 지원 수</span>
            <strong className={styles.value}>{stats.avgApplicationsPerUser}건</strong>
          </div>
        </div>

        {stats.topCompanies.length > 0 && (
          <div className={styles.trendTable}>
            <h4 className={styles.trendTitle}>많이 지원한 회사 TOP 10</h4>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>순위</th>
                  <th>회사</th>
                  <th>지원 수</th>
                </tr>
              </thead>
              <tbody>
                {stats.topCompanies.map((c, i) => (
                  <tr key={c.companyName}>
                    <td>{i + 1}</td>
                    <td>{c.companyName}</td>
                    <td>{c.count}건</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

export default AdminStatsSection;