import { NavLink } from 'react-router-dom';
import styles from './BottomNav.module.css';

function BottomNav() {
  return (
    <nav className={styles.nav}>
      <NavLink to="/dashboard" className={({ isActive }) => isActive ? styles.active : styles.item}>
        <span className={styles.icon}>📊</span>
        <span className={styles.label}>대시보드</span>
      </NavLink>
      <NavLink to="/applications" className={({ isActive }) => isActive ? styles.active : styles.item}>
        <span className={styles.icon}>📝</span>
        <span className={styles.label}>지원현황</span>
      </NavLink>
      <NavLink to="/calendar" className={({ isActive }) => isActive ? styles.active : styles.item}>
        <span className={styles.icon}>📅</span>
        <span className={styles.label}>캘린더</span>
      </NavLink>
      <NavLink to="/mypage" className={({ isActive }) => isActive ? styles.active : styles.item}>
        <span className={styles.icon}>👤</span>
        <span className={styles.label}>마이페이지</span>
      </NavLink>
    </nav>
  );
}

export default BottomNav;
