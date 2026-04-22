import { NavLink } from 'react-router-dom';
import { House, ClipboardList, Calendar, BarChart2, User } from 'lucide-react';
import styles from './BottomNav.module.css';

const NAV_ITEMS = [
  { to: '/dashboard', icon: House, label: '홈' },
  { to: '/applications', icon: ClipboardList, label: '지원현황' },
  { to: '/calendar', icon: Calendar, label: '캘린더' },
  { to: '/analytics', icon: BarChart2, label: '분석' },
  { to: '/mypage', icon: User, label: 'MY' },
];

function BottomNav() {
  return (
    <nav className={styles.nav} aria-label="메인 내비게이션">
      {NAV_ITEMS.map(({ to, icon: Icon, label }) => (
        <NavLink
          key={to}
          to={to}
          className={({ isActive }) => `${styles.item}${isActive ? ` ${styles.active}` : ''}`}
        >
          <Icon size={22} />
          <span className={styles.label}>{label}</span>
        </NavLink>
      ))}
    </nav>
  );
}

export default BottomNav;