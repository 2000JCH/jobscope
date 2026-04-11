import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMe, updateMe, deleteMe, logout } from '../api/auth';
import { useAuthStore } from '../stores/useAuthStore';

function MyPage() {
  const navigate = useNavigate();
  const logoutStore = useAuthStore((state) => state.logout);
  const setAuth = useAuthStore((state) => state.setAuth);
  const currentUser = useAuthStore((state) => state.user);
  const currentAccessToken = useAuthStore((state) => state.accessToken);

  const [profile, setProfile] = useState(null);
  const [nickname, setNickname] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');

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
        <button onClick={handleLogout}>로그아웃</button>
        <button onClick={handleDelete}>회원탈퇴</button>
      </section>
    </div>
  );
}

export default MyPage;
