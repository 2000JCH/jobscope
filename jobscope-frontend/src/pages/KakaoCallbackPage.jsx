import { useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { loginWithKakao } from '../api/auth';
import { useAuthStore } from '../stores/useAuthStore';

function KakaoCallbackPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);
  const called = useRef(false);

  useEffect(() => {
    if (called.current) return;
    called.current = true;

    const code = searchParams.get('code');
    if (!code) {
      navigate('/', { replace: true });
      return;
    }

    loginWithKakao(code)
      .then((res) => {
        const { accessToken, user } = res.data.data;
        setAuth(user, accessToken);
        navigate('/dashboard', { replace: true });
      })
      .catch((err) => {
        console.error('카카오 로그인 실패', err);
        navigate('/', { replace: true });
      });
  }, [searchParams, navigate, setAuth]);

  return <div>로그인 처리 중...</div>;
}

export default KakaoCallbackPage;
