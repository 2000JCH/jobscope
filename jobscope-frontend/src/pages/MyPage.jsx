import { useEffect, useState, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMe, updateMe, deleteMe, logout } from '../api/auth';
import { useAuthStore } from '../stores/useAuthStore';
import { useAlarmLogs } from '../hooks/useAlarmLogs';
import AlarmLogItem from '../components/alarm/AlarmLogItem';
import { User } from 'lucide-react';
import styles from './MyPage.module.css';

function MyPage() {
  const navigate = useNavigate();
  const logoutStore = useAuthStore((state) => state.logout);
  const setAuth = useAuthStore((state) => state.setAuth);
  const currentUser = useAuthStore((state) => state.user);
  const currentAccessToken = useAuthStore((state) => state.accessToken);

  const [profile, setProfile] = useState(null);
  const [nickname, setNickname] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [feedback, setFeedback] = useState(null);
  const feedbackTimerRef = useRef(null);

  const { logs, loading: logsLoading, currentPage, totalPages, load: loadLogs, deleteAndReload } = useAlarmLogs();
  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedIds, setSelectedIds] = useState(new Set());

  const showFeedback = useCallback((message, type = 'success') => {
    if (feedbackTimerRef.current) clearTimeout(feedbackTimerRef.current);
    setFeedback({ message, type });
    feedbackTimerRef.current = setTimeout(() => setFeedback(null), 3000);
  }, []);

  useEffect(() => {
    return () => {
      if (feedbackTimerRef.current) clearTimeout(feedbackTimerRef.current);
    };
  }, []);

  useEffect(() => {
    getMe()
      .then((res) => {
        const data = res.data.data;
        setProfile(data);
        setNickname(data.nickname);
        setPhoneNumber(data.phoneNumber ?? '');
      })
      .catch((err) => {
        if (err.response?.status === 404) {
          // DB에 유저가 없음 (볼륨 초기화 등) — stale 토큰 정리 후 로그인으로
          logoutStore();
        }
        navigate('/', { replace: true });
      });
  }, [navigate, logoutStore]);

  const handleUpdate = () => {
    updateMe({ nickname, phoneNumber })
      .then(() => {
        setAuth({ ...currentUser, nickname, phoneNumber }, currentAccessToken);
        showFeedback('프로필이 수정됐습니다.');
      })
      .catch(() => {
        showFeedback('수정에 실패했습니다.', 'error');
      });
  };

  const handleLogout = () => {
    logout().finally(() => {
      logoutStore();
      navigate('/', { replace: true });
    });
  };

  const handleDelete = () => {
    if (!window.confirm('정말 탈퇴하시겠습니까? 모든 데이터가 삭제됩니다.')) return;
    deleteMe()
      .then(() => {
        logoutStore();
        navigate('/', { replace: true });
      })
      .catch(() => {
        showFeedback('탈퇴 처리에 실패했습니다.', 'error');
      });
  };

  const toggleSelectAll = () => {
    setSelectedIds(selectedIds.size === logs.length ? new Set() : new Set(logs.map((l) => l.id)));
  };

  const toggleItem = useCallback((id) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }, []);

  if (!profile) return <div className={styles.alarmLoading}>불러오는 중...</div>;

  const allSelected = logs.length > 0 && selectedIds.size === logs.length;

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1 className={styles.title}>마이페이지</h1>
      </div>

      {feedback && (
        <div className={`${styles.feedback} ${feedback.type === 'error' ? styles.feedbackError : styles.feedbackSuccess}`}>
          {feedback.message}
        </div>
      )}

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2 className={styles.sectionTitle}>프로필</h2>
        </div>
        <div className={styles.profileCard}>
          {profile.profileImage ? (
            <img src={profile.profileImage} alt="프로필" className={styles.avatar} />
          ) : (
            <div className={styles.avatarPlaceholder}>
              <User size={22} />
            </div>
          )}
          <span className={styles.profileEmail}>{profile.email ?? ''}</span>
        </div>
        <div className={styles.formFields}>
          <div className={styles.field}>
            <span className={styles.label}>닉네임</span>
            <input
              className={styles.input}
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              placeholder="닉네임을 입력하세요"
              aria-label="닉네임"
            />
          </div>
          <div className={styles.field}>
            <span className={styles.label}>전화번호</span>
            <input
              className={styles.input}
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              placeholder="알림톡 수신 번호"
              aria-label="전화번호"
            />
          </div>
        </div>
        <button className={styles.saveBtn} onClick={handleUpdate}>저장</button>
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2 className={styles.sectionTitle}>알림 발송 이력</h2>
          {!isEditMode && logs.length > 0 && (
            <button className={styles.editBtn} onClick={() => { setIsEditMode(true); setSelectedIds(new Set()); }}>
              편집
            </button>
          )}
          {isEditMode && (
            <div className={styles.alarmEditBar}>
              <button className={styles.editActionBtn} onClick={toggleSelectAll}>
                {allSelected ? '전체 해제' : '전체 선택'}
              </button>
              <button className={styles.editActionBtn} onClick={() => { setIsEditMode(false); setSelectedIds(new Set()); }}>
                취소
              </button>
              <button
                className={styles.deleteBtn}
                disabled={selectedIds.size === 0}
                onClick={() => deleteAndReload([...selectedIds], currentPage)
                  .then(() => { setIsEditMode(false); setSelectedIds(new Set()); })
                  .catch(() => showFeedback('삭제에 실패했습니다.', 'error'))}
              >
                삭제 ({selectedIds.size})
              </button>
            </div>
          )}
        </div>
        {logsLoading && <div className={styles.alarmEmpty}>불러오는 중...</div>}
        {!logsLoading && logs.length === 0 && (
          <p className={styles.alarmEmpty}>발송된 알림이 없습니다.</p>
        )}
        <div className={styles.alarmList}>
          {logs.map((log) => (
            <AlarmLogItem
              key={log.id}
              log={log}
              isEditMode={isEditMode}
              isSelected={selectedIds.has(log.id)}
              onToggle={() => toggleItem(log.id)}
            />
          ))}
        </div>
        {totalPages > 1 && (
          <div className={styles.pagination}>
            <button className={styles.pageBtn} onClick={() => loadLogs(currentPage - 1)} disabled={currentPage === 0}>이전</button>
            <span className={styles.pageInfo}>{currentPage + 1} / {totalPages}</span>
            <button className={styles.pageBtn} onClick={() => loadLogs(currentPage + 1)} disabled={currentPage >= totalPages - 1}>다음</button>
          </div>
        )}
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2 className={styles.sectionTitle}>계정 관리</h2>
        </div>
        <div className={styles.accountBtns}>
          <button className={styles.logoutBtn} onClick={handleLogout}>로그아웃</button>
          <button className={styles.withdrawBtn} onClick={handleDelete}>회원탈퇴</button>
        </div>
      </section>
    </div>
  );
}

export default MyPage;