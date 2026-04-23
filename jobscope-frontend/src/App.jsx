import { BrowserRouter, Routes, Route } from 'react-router-dom';
import MainPage from './pages/MainPage';
import KakaoCallbackPage from './pages/KakaoCallbackPage';
import DashboardPage from './pages/DashboardPage';
import ApplicationPage from './pages/ApplicationPage';
import CalendarPage from './pages/CalendarPage';
import AnalyticsPage from './pages/AnalyticsPage';
import MyPage from './pages/MyPage';
import ProtectedRoute from './components/common/ProtectedRoute';
import BottomNav from './components/common/BottomNav';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/oauth/callback/kakao" element={<KakaoCallbackPage />} />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <DashboardPage />
              <BottomNav />
            </ProtectedRoute>
          }
        />
        <Route
          path="/applications"
          element={
            <ProtectedRoute>
              <ApplicationPage />
              <BottomNav />
            </ProtectedRoute>
          }
        />
        <Route
          path="/calendar"
          element={
            <ProtectedRoute>
              <CalendarPage />
              <BottomNav />
            </ProtectedRoute>
          }
        />
        <Route
          path="/analytics"
          element={
            <ProtectedRoute>
              <AnalyticsPage />
              <BottomNav />
            </ProtectedRoute>
          }
        />
        <Route
          path="/mypage"
          element={
            <ProtectedRoute>
              <MyPage />
              <BottomNav />
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;