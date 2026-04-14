import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/useAuthStore';

const KAKAO_AUTH_URL = `https://kauth.kakao.com/oauth/authorize?client_id=${import.meta.env.VITE_KAKAO_CLIENT_ID}&redirect_uri=${import.meta.env.VITE_KAKAO_REDIRECT_URI}&response_type=code&scope=talk_message`;

function MainPage() {
  const navigate = useNavigate();
  const accessToken = useAuthStore((state) => state.accessToken);

  useEffect(() => {
    if (accessToken) {
      navigate('/dashboard', { replace: true });
    }
  }, [accessToken, navigate]);

  const handleKakaoLogin = () => {
    window.location.href = KAKAO_AUTH_URL;
  };

  return (
    <div>
      <h1>JobScope</h1>
      <p>취업 지원 현황을 한 곳에서 관리하세요.</p>
      <button onClick={handleKakaoLogin}>
        카카오로 시작하기
      </button>
    </div>
  );
}

export default MainPage;
