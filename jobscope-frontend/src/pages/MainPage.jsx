import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/useAuthStore';
import { ClipboardList, Calendar, Bell } from 'lucide-react';
import styles from './MainPage.module.css';

const KAKAO_AUTH_URL = `https://kauth.kakao.com/oauth/authorize?client_id=${import.meta.env.VITE_KAKAO_CLIENT_ID}&redirect_uri=${import.meta.env.VITE_KAKAO_REDIRECT_URI}&response_type=code&scope=talk_message`;

const FEATURES = [
  {
    icon: ClipboardList,
    colorClass: 'iconBlue',
    title: '지원 현황 관리',
    desc: '전형 단계별 이력을 체계적으로 기록',
  },
  {
    icon: Calendar,
    colorClass: 'iconGreen',
    title: '일정 캘린더',
    desc: '면접 일정과 마감일을 한눈에 확인',
  },
  {
    icon: Bell,
    colorClass: 'iconOrange',
    title: '마감일 알림',
    desc: 'D-7, D-3, D-1 카카오 알림 자동 발송',
  },
];

function MainPage() {
  const navigate = useNavigate();
  const accessToken = useAuthStore((state) => state.accessToken);

  useEffect(() => {
    if (accessToken) {
      navigate('/dashboard', { replace: true });
    }
  }, [accessToken, navigate]);

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <div className={styles.logo}>
          <span className={styles.logoLetter}>J</span>
        </div>
        <span className={styles.brandName}>JobScope</span>
      </header>

      <main className={styles.hero}>
        <h1 className={styles.headline}>
          취업 지원 현황을<br />
          <span className={styles.highlight}>한 눈에</span> 관리하세요
        </h1>
        <p className={styles.subline}>
          여러 회사 지원 현황을 한 곳에서 트래킹하고<br />
          마감일 알림까지 받아보세요.
        </p>

        <div className={styles.features}>
          {FEATURES.map(({ icon: Icon, colorClass, title, desc }) => (
            <div key={title} className={styles.featureItem}>
              <div className={`${styles.featureIcon} ${styles[colorClass]}`}>
                <Icon size={20} />
              </div>
              <div className={styles.featureText}>
                <span className={styles.featureTitle}>{title}</span>
                <span className={styles.featureDesc}>{desc}</span>
              </div>
            </div>
          ))}
        </div>
      </main>

      <div className={styles.cta}>
        <button
          className={styles.kakaoBtn}
          onClick={() => { window.location.href = KAKAO_AUTH_URL; }}
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
            <path d="M12 3C7.03 3 3 6.36 3 10.5c0 2.64 1.63 4.96 4.09 6.33L6 20l4.18-2.58c.59.08 1.21.08 1.82.08 4.97 0 9-3.36 9-7.5S16.97 3 12 3z" />
          </svg>
          카카오로 시작하기
        </button>
        <p className={styles.disclaimer}>
          로그인 시 서비스 이용약관 및 개인정보처리방침에 동의하게 됩니다.
        </p>
      </div>
    </div>
  );
}

export default MainPage;