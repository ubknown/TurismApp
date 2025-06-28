import api from './axios';

// Delete user account permanently
export const deleteAccount = async () => {
  try {
    const response = await api.delete('/api/auth/delete-account');
    return response.data;
  } catch (error) {
    if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    }
    throw new Error('Failed to delete account. Please try again.');
  }
};

// Get current user details
export const getCurrentUser = async () => {
  try {
    const response = await api.get('/api/auth/me');
    return response.data;
  } catch (error) {
    if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    }
    throw new Error('Failed to get user details.');
  }
};

export default {
  deleteAccount,
  getCurrentUser,
};
