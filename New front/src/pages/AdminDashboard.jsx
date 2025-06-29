import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AlertBox from '../components/AlertBox';

const AdminDashboard = () => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [loginForm, setLoginForm] = useState({ email: '', password: '' });
    const [applications, setApplications] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [adminUser, setAdminUser] = useState(null);
    const [reviewNotes, setReviewNotes] = useState({});
    const navigate = useNavigate();

    // Clear any existing tokens on component mount
    useEffect(() => {
        localStorage.removeItem('adminToken');
        setIsAuthenticated(false);
    }, []);

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const response = await fetch('http://localhost:8080/api/admin/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(loginForm),
            });

            const data = await response.json();

            if (response.ok) {
                localStorage.setItem('adminToken', data.token);
                setAdminUser(data.user);
                setIsAuthenticated(true);
                setLoginForm({ email: '', password: '' });
                await loadApplications();
            } else {
                setError(data.message || 'Login failed');
            }
        } catch (err) {
            setError('Network error. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const loadApplications = async () => {
        try {
            const token = localStorage.getItem('adminToken');
            const response = await fetch('http://localhost:8080/api/admin/applications', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            const data = await response.json();

            if (response.ok) {
                setApplications(data.applications);
            } else {
                setError(data.message || 'Failed to load applications');
                if (response.status === 401) {
                    setIsAuthenticated(false);
                    localStorage.removeItem('adminToken');
                }
            }
        } catch (err) {
            setError('Failed to load applications');
        }
    };

    const handleApprove = async (applicationId) => {
        setLoading(true);
        setError('');
        setSuccess('');

        try {
            const token = localStorage.getItem('adminToken');
            const response = await fetch(`http://localhost:8080/api/admin/applications/${applicationId}/approve`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    reviewNotes: reviewNotes[applicationId] || ''
                }),
            });

            const data = await response.json();

            if (response.ok) {
                setSuccess(`Application approved successfully for ${data.userEmail}`);
                await loadApplications(); // Refresh the list
                setReviewNotes(prev => ({ ...prev, [applicationId]: '' }));
            } else {
                setError(data.message || 'Failed to approve application');
                if (response.status === 401) {
                    setIsAuthenticated(false);
                    localStorage.removeItem('adminToken');
                }
            }
        } catch (err) {
            setError('Failed to approve application');
        } finally {
            setLoading(false);
        }
    };

    const handleReject = async (applicationId) => {
        setLoading(true);
        setError('');
        setSuccess('');

        try {
            const token = localStorage.getItem('adminToken');
            const response = await fetch(`http://localhost:8080/api/admin/applications/${applicationId}/reject`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    reviewNotes: reviewNotes[applicationId] || ''
                }),
            });

            const data = await response.json();

            if (response.ok) {
                setSuccess(`Application rejected for ${data.userEmail}`);
                await loadApplications(); // Refresh the list
                setReviewNotes(prev => ({ ...prev, [applicationId]: '' }));
            } else {
                setError(data.message || 'Failed to reject application');
                if (response.status === 401) {
                    setIsAuthenticated(false);
                    localStorage.removeItem('adminToken');
                }
            }
        } catch (err) {
            setError('Failed to reject application');
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('adminToken');
        setIsAuthenticated(false);
        setAdminUser(null);
        setApplications([]);
        setError('');
        setSuccess('');
    };

    const getStatusBadgeClass = (status) => {
        switch (status) {
            case 'PENDING':
                return 'bg-yellow-100 text-yellow-800 border-yellow-200';
            case 'APPROVED':
                return 'bg-green-100 text-green-800 border-green-200';
            case 'REJECTED':
                return 'bg-red-100 text-red-800 border-red-200';
            default:
                return 'bg-gray-100 text-gray-800 border-gray-200';
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleString();
    };

    // Login form
    if (!isAuthenticated) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center px-4">
                <div className="max-w-md w-full space-y-8">
                    <div className="text-center">
                        <h2 className="mt-6 text-3xl font-extrabold text-gray-900">
                            Admin Dashboard Login
                        </h2>
                        <p className="mt-2 text-sm text-gray-600">
                            Please enter your admin credentials
                        </p>
                    </div>
                    
                    {error && <AlertBox type="error" message={error} />}
                    
                    <form className="mt-8 space-y-6" onSubmit={handleLogin}>
                        <div className="rounded-md shadow-sm space-y-4">
                            <div>
                                <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                                    Email Address
                                </label>
                                <input
                                    id="email"
                                    name="email"
                                    type="email"
                                    required
                                    className="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                    placeholder="Admin email address"
                                    value={loginForm.email}
                                    onChange={(e) => setLoginForm(prev => ({ ...prev, email: e.target.value }))}
                                />
                            </div>
                            <div>
                                <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                                    Password
                                </label>
                                <input
                                    id="password"
                                    name="password"
                                    type="password"
                                    required
                                    className="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                    placeholder="Password"
                                    value={loginForm.password}
                                    onChange={(e) => setLoginForm(prev => ({ ...prev, password: e.target.value }))}
                                />
                            </div>
                        </div>

                        <div>
                            <button
                                type="submit"
                                disabled={loading}
                                className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
                            >
                                {loading ? 'Signing in...' : 'Sign in'}
                            </button>
                        </div>
                    </form>
                    
                    <div className="text-center">
                        <button
                            onClick={() => navigate('/')}
                            className="text-indigo-600 hover:text-indigo-500 text-sm"
                        >
                            ← Back to Home
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    // Admin dashboard
    return (
        <div className="min-h-screen bg-gray-50">
            {/* Header */}
            <div className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center py-6">
                        <div>
                            <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
                            <p className="text-sm text-gray-600">
                                Welcome, {adminUser?.firstName} {adminUser?.lastName}
                            </p>
                        </div>
                        <button
                            onClick={handleLogout}
                            className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                        >
                            Logout
                        </button>
                    </div>
                </div>
            </div>

            {/* Content */}
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {error && <AlertBox type="error" message={error} onClose={() => setError('')} />}
                {success && <AlertBox type="success" message={success} onClose={() => setSuccess('')} />}

                <div className="bg-white shadow overflow-hidden rounded-lg">
                    <div className="px-4 py-5 sm:p-6">
                        <div className="flex justify-between items-center mb-6">
                            <h2 className="text-lg font-medium text-gray-900">
                                Owner Applications ({applications.length})
                            </h2>
                            <button
                                onClick={loadApplications}
                                disabled={loading}
                                className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-md text-sm font-medium disabled:opacity-50"
                            >
                                {loading ? 'Refreshing...' : 'Refresh'}
                            </button>
                        </div>

                        {applications.length === 0 ? (
                            <div className="text-center py-12">
                                <p className="text-gray-500">No applications found.</p>
                            </div>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="min-w-full divide-y divide-gray-200">
                                    <thead className="bg-gray-50">
                                        <tr>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Applicant
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Email
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Status
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Submitted
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Message
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Actions
                                            </th>
                                        </tr>
                                    </thead>
                                    <tbody className="bg-white divide-y divide-gray-200">
                                        {applications.map((application) => (
                                            <tr key={application.id} className="hover:bg-gray-50">
                                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                                                    {application.userName}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                                    {application.userEmail}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap">
                                                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full border ${getStatusBadgeClass(application.status)}`}>
                                                        {application.status}
                                                    </span>
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                                    {formatDate(application.submittedAt)}
                                                </td>
                                                <td className="px-6 py-4 text-sm text-gray-500 max-w-xs">
                                                    <div className="truncate" title={application.message}>
                                                        {application.message || 'No message'}
                                                    </div>
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                                    {application.status === 'PENDING' ? (
                                                        <div className="flex space-x-2">
                                                            <button
                                                                onClick={() => handleApprove(application.id)}
                                                                disabled={loading}
                                                                className="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded text-xs disabled:opacity-50"
                                                            >
                                                                Accept
                                                            </button>
                                                            <button
                                                                onClick={() => handleReject(application.id)}
                                                                disabled={loading}
                                                                className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded text-xs disabled:opacity-50"
                                                            >
                                                                Refuse
                                                            </button>
                                                        </div>
                                                    ) : (
                                                        <span className="text-gray-400 text-xs">
                                                            {application.status === 'APPROVED' ? 'Approved' : 'Rejected'}
                                                            {application.reviewedAt && (
                                                                <div className="text-xs text-gray-400">
                                                                    {formatDate(application.reviewedAt)}
                                                                </div>
                                                            )}
                                                        </span>
                                                    )}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;
