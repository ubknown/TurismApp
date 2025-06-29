const API_BASE_URL = 'http://localhost:8080/api';

export const adminService = {
    login: async (email, password) => {
        const response = await fetch(`${API_BASE_URL}/admin/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, password }),
        });

        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'Admin login failed');
        }
        
        return data;
    },

    getAllApplications: async (token) => {
        const response = await fetch(`${API_BASE_URL}/admin/applications`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
        });

        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'Failed to load applications');
        }
        
        return data;
    },

    approveApplication: async (token, applicationId, reviewNotes = '') => {
        const response = await fetch(`${API_BASE_URL}/admin/applications/${applicationId}/approve`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ reviewNotes }),
        });

        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'Failed to approve application');
        }
        
        return data;
    },

    rejectApplication: async (token, applicationId, reviewNotes = '') => {
        const response = await fetch(`${API_BASE_URL}/admin/applications/${applicationId}/reject`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ reviewNotes }),
        });

        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'Failed to reject application');
        }
        
        return data;
    },

    verifySession: async (token) => {
        const response = await fetch(`${API_BASE_URL}/admin/verify`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
        });

        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'Session invalid');
        }
        
        return data;
    }
};

export default adminService;
