import instance from './axios';

export const loginWithKakao = (code) =>
  instance.post('/api/auth/kakao', { code });

export const refreshToken = () =>
  instance.post('/api/auth/refresh');

export const logout = () =>
  instance.post('/api/auth/logout');

export const getMe = () =>
  instance.get('/api/users/me');
