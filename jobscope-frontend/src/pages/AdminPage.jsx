import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/useAuthStore';
import AdminStatsSection from '../components/admin/AdminStatsSection';
import AdminUserTable from '../components/admin/AdminUserTable';
import AdminNoticeList from '../components/admin/AdminNoticeList';
import { LayoutDashboard } from 'lucide-react';
import styles from './AdminPage.module.css';

const TABS = [
  { id: 'stats', label: '통계' },
  { id: 'users', label: '사용자' },
  { id: 'notices', label: '공지사항' },
];

function AdminPage() {
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const [activeTab, setActiveTab] = useState('stats');

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <span className={styles.logo}>JobScope 관리자</span>
        <div className={styles.headerRight}>
          <span className={styles.adminBadge}>{user?.nickname}</span>
          <button className={styles.dashboardBtn} onClick={() => navigate('/dashboard')}>
            <LayoutDashboard size={16} />
            대시보드
          </button>
        </div>
      </header>

      <div className={styles.tabs}>
        {TABS.map((tab) => (
          <button
            key={tab.id}
            className={`${styles.tab} ${activeTab === tab.id ? styles.tabActive : ''}`}
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <main className={styles.content}>
        {activeTab === 'stats' && <AdminStatsSection />}
        {activeTab === 'users' && <AdminUserTable />}
        {activeTab === 'notices' && <AdminNoticeList />}
      </main>
    </div>
  );
}

export default AdminPage;