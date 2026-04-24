import { useEffect, useState, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { logout } from '../api/auth';
import { getMe, updateMe, deleteMe, updateProfileImage, resetProfileImage } from '../api/user';
import { useAuthStore } from '../stores/useAuthStore';
import { useAlarmLogs } from '../hooks/useAlarmLogs';
import { useTheme } from '../hooks/useTheme';
import { useCalendarTheme, CALENDAR_THEMES } from '../hooks/useCalendarTheme';
import AlarmLogItem from '../components/alarm/AlarmLogItem';
import JourneySection from '../components/mypage/JourneySection';
import { useNotice } from '../hooks/useNotice';
import { User, Camera, Settings, Bell, ShieldCheck } from 'lucide-react';
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
  const [imageUploading, setImageUploading] = useState(false);
  const feedbackTimerRef = useRef(null);
  const fileInputRef = useRef(null);

  const { logs, loading: logsLoading, currentPage, totalPages, load: loadLogs, deleteAndReload } = useAlarmLogs();
  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedIds, setSelectedIds] = useState(new Set());
  const [showSettings, setShowSettings] = useState(false);
  const [showNotice, setShowNotice] = useState(false);
  const { notice, hasUnread, markAsRead } = useNotice();
  const { isDark, setIsDark } = useTheme();
  const { themeId, setTheme } = useCalendarTheme();

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

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    if (!['image/jpeg', 'image/png'].includes(file.type)) {
      showFeedback('jpg, png 형식만 업로드할 수 있습니다.', 'error');
      e.target.value = '';
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      showFeedback('5MB 이하 이미지만 업로드할 수 있습니다.', 'error');
      e.target.value = '';
      return;
    }
    setImageUploading(true);
    updateProfileImage(file)
      .then((res) => {
        const newUrl = res.data.data.profileImage;
        setProfile((prev) => ({ ...prev, profileImage: newUrl, hasCustomProfileImage: true }));
        setAuth({ ...currentUser, profileImage: newUrl }, currentAccessToken);
        showFeedback('프로필 이미지가 변경됐습니다.');
      })
      .catch(() => {
        showFeedback('이미지 업로드에 실패했습니다.', 'error');
      })
      .finally(() => {
        setImageUploading(false);
        e.target.value = '';
      });
  };

  const handleImageReset = () => {
    resetProfileImage()
      .then(() => getMe())
      .then((res) => {
        const data = res.data.data;
        setProfile(data);
        setAuth({ ...currentUser, profileImage: data.profileImage }, currentAccessToken);
        showFeedback('기본 이미지로 초기화됐습니다.');
      })
      .catch(() => {
        showFeedback('초기화에 실패했습니다.', 'error');
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
        <div className={styles.headerBtns}>
          <button
            className={styles.bellBtn}
            onClick={() => {
              setShowSettings(false);
              setShowNotice((v) => {
                if (!v) markAsRead();
                return !v;
              });
            }}
            aria-label="공지사항"
          >
            <Bell size={20} />
            {hasUnread && <span className={styles.bellBadge} />}
          </button>
          <button
            className={styles.settingsBtn}
            onClick={() => { setShowNotice(false); setShowSettings((v) => !v); }}
            aria-label="설정"
          >
            <Settings size={20} />
          </button>
        </div>
      </div>

      {showNotice && (
        <div className={styles.noticePanel}>
          {notice ? (
            <>
              <strong className={styles.noticeTitle}>{notice.title}</strong>
              <p className={styles.noticeContent}>{notice.content}</p>
            </>
          ) : (
            <p className={styles.noticeEmpty}>새로운 알림이 없습니다.</p>
          )}
        </div>
      )}


      {showSettings && (
        <div className={styles.settingsPanel}>
          <div className={styles.settingsRow}>
            <span className={styles.settingsLabel}>다크모드</span>
            <label className={styles.toggle}>
              <input
                type="checkbox"
                checked={isDark}
                onChange={(e) => setIsDark(e.target.checked)}
              />
              <span className={styles.toggleSlider} />
            </label>
          </div>
          <div className={styles.settingsDivider} />
          <div className={styles.settingsThemeRow}>
            <span className={styles.settingsLabel}>캘린더 테마</span>
            <div className={styles.themePicker}>
              {CALENDAR_THEMES.map((t) => (
                <button
                  key={t.id}
                  type="button"
                  className={`${styles.themeSwatch} ${themeId === t.id ? styles.themeSwatchActive : ''}`}
                  style={{ background: t.color }}
                  onClick={() => setTheme(t.id)}
                  aria-label={t.label}
                  title={t.label}
                />
              ))}
            </div>
          </div>
        </div>
      )}

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
          <div className={styles.avatarWrapper}>
            <button
              className={styles.avatarBtn}
              onClick={() => fileInputRef.current?.click()}
              disabled={imageUploading}
              aria-label="프로필 이미지 변경"
            >
              {profile.profileImage ? (
                <img src={profile.profileImage} alt="프로필" className={styles.avatar} />
              ) : (
                <div className={styles.avatarPlaceholder}>
                  <User size={22} />
                </div>
              )}
              <div className={styles.avatarOverlay}>
                <Camera size={16} />
              </div>
            </button>
            {profile.hasCustomProfileImage && (
              <button className={styles.imageResetBtn} onClick={handleImageReset}>
                초기화
              </button>
            )}
            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png"
              className={styles.fileInput}
              onChange={handleImageChange}
            />
          </div>
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
          <h2 className={styles.sectionTitle}>취준 여정</h2>
        </div>
        <JourneySection />
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
        {currentUser?.role === 'ADMIN' && (
          <button className={styles.adminBtn} onClick={() => navigate('/admin')}>
            <ShieldCheck size={15} />
            관리자 페이지
          </button>
        )}
      </section>
    </div>
  );
}

export default MyPage;