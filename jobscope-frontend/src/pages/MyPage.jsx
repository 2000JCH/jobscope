import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMe, updateMe, deleteMe, logout } from '../api/auth';
import { useAuthStore } from '../stores/useAuthStore';
import { useAlarmLogs } from '../hooks/useAlarmLogs';
import AlarmLogItem from '../components/alarm/AlarmLogItem';

function MyPage() {
  const navigate = useNavigate();
  const logoutStore = useAuthStore((state) => state.logout);
  const setAuth = useAuthStore((state) => state.setAuth);
  const currentUser = useAuthStore((state) => state.user);
  const currentAccessToken = useAuthStore((state) => state.accessToken);

  const [profile, setProfile] = useState(null);
  const [nickname, setNickname] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');

  const { logs, loading: logsLoading, currentPage, totalPages, load: loadLogs, deleteAndReload } = useAlarmLogs();
  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedIds, setSelectedIds] = useState(new Set());

  useEffect(() => {
    getMe()
      .then((res) => {
        const data = res.data.data;
        setProfile(data);
        setNickname(data.nickname);
        setPhoneNumber(data.phoneNumber ?? '');
      })
      .catch(() => {
        navigate('/', { replace: true });
      });
  }, [navigate]);

  const handleUpdate = () => {
    updateMe({ nickname, phoneNumber })
      .then(() => {
        setAuth(
          { ...currentUser, nickname, phoneNumber },
          currentAccessToken
        );
        alert('프로필이 수정됐습니다.');
      })
      .catch(() => {
        alert('수정에 실패했습니다.');
      });
  };

  const handleLogout = () => {
    logout()
      .finally(() => {
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
        alert('탈퇴 처리에 실패했습니다.');
      });
  };

  if (!profile) return <div>불러오는 중...</div>;

  return (
    <div>
      <h1>마이페이지</h1>

      <section>
        <h2>프로필</h2>
        {profile.profileImage && (
          <img src={profile.profileImage} alt="프로필 이미지" width={64} height={64} />
        )}
        <p>이메일: {profile.email ?? '없음'}</p>

        <div>
          <label>닉네임</label>
          <input
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
          />
        </div>
        <div>
          <label>전화번호</label>
          <input
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
            placeholder="알림톡 수신 번호"
          />
        </div>
        <button onClick={handleUpdate}>저장</button>
      </section>

      <section>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2>알림 발송 이력</h2>
          {logs.length > 0 && !isEditMode && (
            <button onClick={() => { setIsEditMode(true); setSelectedIds(new Set()); }}>
              편집
            </button>
          )}
          {isEditMode && (
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button
                onClick={() => {
                  if (selectedIds.size === logs.length) {
                    setSelectedIds(new Set());
                  } else {
                    setSelectedIds(new Set(logs.map((l) => l.id)));
                  }
                }}
              >
                {selectedIds.size === logs.length ? '전체 해제' : '전체 선택'}
              </button>
              <button
                onClick={() => { setIsEditMode(false); setSelectedIds(new Set()); }}
              >
                취소
              </button>
              <button
                disabled={selectedIds.size === 0}
                onClick={() => {
                  deleteAndReload([...selectedIds], currentPage).then(() => {
                    setIsEditMode(false);
                    setSelectedIds(new Set());
                  });
                }}
              >
                삭제 ({selectedIds.size})
              </button>
            </div>
          )}
        </div>
        {logsLoading && <div>불러오는 중...</div>}
        {!logsLoading && logs.length === 0 && (
          <p style={{ color: '#999', fontSize: '0.875rem' }}>발송된 알림이 없습니다.</p>
        )}
        {logs.map((log) => (
          <AlarmLogItem
            key={log.id}
            log={log}
            isEditMode={isEditMode}
            isSelected={selectedIds.has(log.id)}
            onToggle={() => {
              setSelectedIds((prev) => {
                const next = new Set(prev);
                if (next.has(log.id)) next.delete(log.id);
                else next.add(log.id);
                return next;
              });
            }}
          />
        ))}
        {totalPages > 1 && (
          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem', justifyContent: 'center' }}>
            <button
              onClick={() => loadLogs(currentPage - 1)}
              disabled={currentPage === 0}
            >
              이전
            </button>
            <span>{currentPage + 1} / {totalPages}</span>
            <button
              onClick={() => loadLogs(currentPage + 1)}
              disabled={currentPage >= totalPages - 1}
            >
              다음
            </button>
          </div>
        )}
      </section>

      <section>
        <button onClick={handleLogout}>로그아웃</button>
        <button onClick={handleDelete}>회원탈퇴</button>
      </section>
    </div>
  );
}

export default MyPage;