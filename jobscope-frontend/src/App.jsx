import { BrowserRouter, Routes, Route } from 'react-router-dom';
import MainPage from './pages/MainPage';
import KakaoCallbackPage from './pages/KakaoCallbackPage';
import DashboardPage from './pages/DashboardPage';
import ApplicationPage from './pages/ApplicationPage';
import MyPage from './pages/MyPage';
import ProtectedRoute from './components/common/ProtectedRoute';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/oauth/callback/kakao" element={<KakaoCallbackPage />} />
        <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
        <Route path="/applications" element={<ProtectedRoute><ApplicationPage /></ProtectedRoute>} />
        <Route path="/mypage" element={<ProtectedRoute><MyPage /></ProtectedRoute>} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
