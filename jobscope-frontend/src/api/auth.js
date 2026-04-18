import instance from './axios';

export const loginWithKakao = (code) =>
  instance.post('/api/auth/kakao', { code });

export const logout = () =>
  instance.delete('/api/auth/logout');

export const getMe = () =>
  instance.get('/api/users/me');

export const updateMe = (data) =>
  instance.patch('/api/users/me', data);

export const deleteMe = () =>
  instance.delete('/api/users/me');
