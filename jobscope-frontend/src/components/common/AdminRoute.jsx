import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../../stores/useAuthStore';

function AdminRoute({ children }) {
  const user = useAuthStore((state) => state.user);
  const accessToken = useAuthStore((state) => state.accessToken);

  if (!user || !accessToken) return <Navigate to="/" replace />;
  if (user.role !== 'ADMIN') return <Navigate to="/dashboard" replace />;

  return children;
}

export default AdminRoute;