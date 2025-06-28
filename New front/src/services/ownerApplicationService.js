import api from './axios';

export const ownerApplicationService = {
  // Submit an owner application
  async submitApplication(message = '') {
    try {
      const response = await api.post('/api/owner-applications', {
        message: message
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Get user's application status
  async getMyApplication() {
    try {
      const response = await api.get('/api/owner-applications/my-application');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Check if user can apply
  async canApply() {
    try {
      const response = await api.get('/api/owner-applications/can-apply');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Get pending applications (admin only)
  async getPendingApplications() {
    try {
      const response = await api.get('/api/owner-applications/pending');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Approve an application (admin only)
  async approveApplication(applicationId, reviewNotes = '') {
    try {
      const response = await api.post(`/api/owner-applications/${applicationId}/approve`, {
        reviewNotes: reviewNotes
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Reject an application (admin only)
  async rejectApplication(applicationId, reviewNotes = '') {
    try {
      const response = await api.post(`/api/owner-applications/${applicationId}/reject`, {
        reviewNotes: reviewNotes
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};
