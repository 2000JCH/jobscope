import instance from './axios';

export const getMe = () => instance.get('/api/users/me');

export const updateMe = (data) => instance.patch('/api/users/me', data);

export const deleteMe = () => instance.delete('/api/users/me');

export const updateProfileImage = (file) => {
  const formData = new FormData();
  formData.append('image', file);
  return instance.post('/api/users/me/profile-image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const resetProfileImage = () =>
  instance.delete('/api/users/me/profile-image');

export const getJourney = () => instance.get('/api/users/me/journey');