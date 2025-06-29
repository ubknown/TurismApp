import React, { useState, useRef, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { 
  Home, 
  Search, 
  BarChart3, 
  Calendar, 
  LogOut, 
  Menu, 
  X,
  Mountain,
  Building,
  LogIn,
  UserPlus,
  TrendingUp,
  Settings,
  Trash2,
  Crown,
  Star
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { ownerApplicationService } from '../services/ownerApplicationService';
import { deleteAccount } from '../services/authService';

const NavBar = () => {
  const { user, isAuthenticated, logout, isGuest, isOwner, isAdmin, canApplyAsOwner, hasOwnerApplication, getOwnerStatus, updateUser, refreshUser } = useAuth();
  const { info, success, error: showError } = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [settingsDropdownOpen, setSettingsDropdownOpen] = useState(false);
  const [deleteAccountModalOpen, setDeleteAccountModalOpen] = useState(false);
  const [ownerApplicationModalOpen, setOwnerApplicationModalOpen] = useState(false);
  const [deleteConfirmText, setDeleteConfirmText] = useState('');
  const settingsRef = useRef(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (settingsRef.current && !settingsRef.current.contains(event.target)) {
        setSettingsDropdownOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    info('Logged Out', 'You have been successfully logged out');
    navigate('/');
    setMobileMenuOpen(false);
    setSettingsDropdownOpen(false);
  };

  const handleDeleteAccount = async () => {
    if (deleteConfirmText !== 'DELETE') {
      showError('Confirmation Error', 'Please type "DELETE" to confirm');
      return;
    }

    try {
      console.log('🗑️ Starting account deletion process...');
      await deleteAccount();
      success('Account Deleted', 'Your account has been permanently deleted');
      logout();
      navigate('/');
      setDeleteAccountModalOpen(false);
    } catch (error) {
      console.error('❌ Account deletion failed:', error);
      
      // Provide specific error messages based on the error type
      let errorMessage = 'Failed to delete account. Please try again.';
      let errorTitle = 'Deletion Failed';
      
      if (error.response?.status === 401) {
        errorTitle = 'Authentication Error';
        errorMessage = 'You need to be logged in to delete your account. Please refresh and try again.';
      } else if (error.response?.status === 403) {
        errorTitle = 'Access Denied';
        errorMessage = 'You do not have permission to delete this account.';
      } else if (error.response?.status === 404) {
        errorTitle = 'Account Not Found';
        errorMessage = 'Your account could not be found. Please refresh and try again.';
      } else if (error.response?.status === 500) {
        errorTitle = 'Server Error';
        errorMessage = 'A server error occurred while deleting your account. Please contact support if this persists.';
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      showError(errorTitle, errorMessage);
    }
  };

  const handleOwnerApplication = () => {
    setOwnerApplicationModalOpen(true);
    setMobileMenuOpen(false);
  };

  const isActivePath = (path) => {
    return location.pathname === path;
  };

  const navLinkClass = (path) => {
    const baseClass = "px-4 py-2 rounded-xl transition-all duration-300 flex items-center gap-2";
    return isActivePath(path) 
      ? `${baseClass} bg-white/20 text-white` 
      : `${baseClass} text-violet-200 hover:bg-white/10 hover:text-white`;
  };

  return (
    <nav className="bg-white/5 backdrop-blur-xl border-b border-white/10 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto">
        <div className="flex items-center justify-between h-16 px-2">
          {/* Logo and App Name - Far Left */}
          <div className="flex items-center">
            <Link 
              to="/" 
              className="flex items-center space-x-2 text-white font-bold text-xl hover:text-violet-300 transition-colors"
            >
              <Mountain className="w-8 h-8 text-violet-400" />
              <span>TurismApp</span>
            </Link>
          </div>

          {/* Desktop Navigation - Far Right */}
          <div className="flex items-center gap-6">
            {/* Public Links */}
            <Link to="/" className={navLinkClass('/')}>
              <Home className="w-4 h-4" />
              Home
            </Link>
            
            <Link to="/units" className={navLinkClass('/units')}>
              <Search className="w-4 h-4" />
              Browse Units
            </Link>

            {/* Authenticated Links */}
            {isAuthenticated ? (
              <>
                {/* Owner/Admin specific links */}
                {(isOwner() || isAdmin()) && (
                  <>
                    <Link to="/dashboard" className={navLinkClass('/dashboard')}>
                      <BarChart3 className="w-4 h-4" />
                      Dashboard
                    </Link>
                    
                    {/* Admin specific link */}
                    {isAdmin() && (
                      <Link to="/admin" className={navLinkClass('/admin')}>
                        <Settings className="w-4 h-4" />
                        Admin Panel
                      </Link>
                    )}
                    
                    <Link to="/my-units" className={navLinkClass('/my-units')}>
                      <Building className="w-4 h-4" />
                      My Units
                    </Link>
                    
                    <Link to="/profit-analytics" className={navLinkClass('/profit-analytics')}>
                      <TrendingUp className="w-4 h-4" />
                      Analytics
                    </Link>
                  </>
                )}
                
                {/* Common authenticated links */}
                <Link to="/bookings" className={navLinkClass('/bookings')}>
                  <Calendar className="w-4 h-4" />
                  {isGuest() ? 'My Bookings' : 'Bookings'}
                </Link>

                {/* User Menu with Settings */}
                <div className="flex items-center space-x-3 pl-4 border-l border-white/20">
                  <div className="text-right">
                    <div className="text-violet-200 text-sm">
                      Welcome, {user?.firstName || 'User'}
                    </div>
                    <div className="text-violet-300 text-xs">
                      {user?.role === 'GUEST' ? 'Guest' : user?.role === 'OWNER' ? 'Property Owner' : 'Admin'}
                    </div>
                  </div>
                  
                  {/* Settings Dropdown */}
                  <div className="relative" ref={settingsRef}>
                    <button
                      onClick={() => setSettingsDropdownOpen(!settingsDropdownOpen)}
                      className="px-3 py-2 bg-white/10 hover:bg-white/20 text-violet-200 hover:text-white rounded-xl transition-all duration-300 flex items-center gap-2"
                    >
                      <Settings className="w-4 h-4" />
                    </button>

                    {settingsDropdownOpen && (
                      <div className="absolute right-0 mt-2 min-w-[380px] bg-gradient-to-br from-purple-800/90 to-violet-900/90 backdrop-blur-md border border-purple-300/20 rounded-2xl shadow-2xl shadow-purple-500/20 z-50">
                        <div className="p-6">
                          {/* Apply as Owner Section for Guests who can apply */}
                          {canApplyAsOwner() && (
                            <div className="text-center mb-6 pb-6 border-b border-purple-300/20">
                              <div className="text-white font-medium text-lg mb-4 leading-relaxed">
                                Turn your dreams into business!🏡
                              </div>
                              <button
                                onClick={() => {
                                  handleOwnerApplication();
                                  setSettingsDropdownOpen(false);
                                }}
                                className="w-full px-6 py-4 bg-gradient-to-r from-yellow-500 to-orange-500 hover:from-yellow-600 hover:to-orange-600 text-white font-semibold rounded-xl transition-all duration-300 transform hover:scale-105 shadow-lg flex items-center justify-center gap-2"
                              >
                                <Crown className="w-5 h-5" />
                                Apply as Owner
                              </button>
                            </div>
                          )}

                          {/* Show application status for users with existing applications */}
                          {hasOwnerApplication() && (
                            <div className="text-center mb-6 pb-6 border-b border-purple-300/20">
                              <div className="text-white font-medium text-lg mb-2">
                                Owner Application Status
                              </div>
                              <div className={`px-4 py-2 rounded-lg text-sm font-medium ${
                                getOwnerStatus() === 'PENDING' ? 'bg-yellow-500/20 text-yellow-300' :
                                getOwnerStatus() === 'APPROVED' ? 'bg-green-500/20 text-green-300' :
                                getOwnerStatus() === 'REJECTED' ? 'bg-red-500/20 text-red-300' :
                                'bg-gray-500/20 text-gray-300'
                              }`}>
                                {getOwnerStatus() === 'PENDING' && '⏳ Pending Review'}
                                {getOwnerStatus() === 'APPROVED' && '✅ Approved'}
                                {getOwnerStatus() === 'REJECTED' && '❌ Rejected'}
                              </div>
                            </div>
                          )}
                          
                          {/* Menu Options */}
                          <div className="space-y-2">
                            <button
                              onClick={handleLogout}
                              className="w-full text-left px-4 py-3 text-purple-100 hover:text-white hover:bg-white/10 rounded-xl transition-all duration-200 flex items-center gap-3"
                            >
                              <LogOut className="w-5 h-5" />
                              <span className="font-medium">Log Out</span>
                            </button>
                            <button
                              onClick={() => {
                                setDeleteAccountModalOpen(true);
                                setSettingsDropdownOpen(false);
                              }}
                              className="w-full text-left px-4 py-3 text-red-300 hover:text-red-200 hover:bg-red-500/20 rounded-xl transition-all duration-200 flex items-center gap-3"
                            >
                              <Trash2 className="w-5 h-5" />
                              <span className="font-medium">Delete Account</span>
                            </button>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </>
            ) : (
              /* Non-authenticated Links */
              <>
                <Link
                  to="/login"
                  className="px-4 py-2 text-violet-200 hover:text-white transition-colors flex items-center gap-2"
                >
                  <LogIn className="w-4 h-4" />
                  Login
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 bg-violet-600 hover:bg-violet-700 text-white rounded-xl transition-all duration-300 flex items-center gap-2"
                >
                  <UserPlus className="w-4 h-4" />
                  Sign Up
                </Link>
              </>
            )}

          {/* Mobile menu button */}
          <div className="md:hidden">
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 rounded-xl text-violet-200 hover:text-white hover:bg-white/10 transition-colors"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>

        {/* Mobile Navigation Menu */}
        {mobileMenuOpen && (
          <div className="md:hidden py-4 border-t border-white/10">
            <div className="space-y-2">
              {/* Public Mobile Links */}
              <Link 
                to="/" 
                className={`block ${navLinkClass('/')}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                <Home className="w-4 h-4" />
                Home
              </Link>
              
              <Link 
                to="/units" 
                className={`block ${navLinkClass('/units')}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                <Search className="w-4 h-4" />
                Browse Units
              </Link>

              {/* Authenticated Mobile Links */}
              {isAuthenticated ? (
                <>
                  {/* Owner/Admin specific mobile links */}
                  {(isOwner() || isAdmin()) && (
                    <>
                      <Link 
                        to="/dashboard" 
                        className={`block ${navLinkClass('/dashboard')}`}
                        onClick={() => setMobileMenuOpen(false)}
                      >
                        <BarChart3 className="w-4 h-4" />
                        Dashboard
                      </Link>
                      
                      {/* Admin specific mobile link */}
                      {isAdmin() && (
                        <Link 
                          to="/admin" 
                          className={`block ${navLinkClass('/admin')}`}
                          onClick={() => setMobileMenuOpen(false)}
                        >
                          <Settings className="w-4 h-4" />
                          Admin Panel
                        </Link>
                      )}
                      
                      <Link 
                        to="/my-units" 
                        className={`block ${navLinkClass('/my-units')}`}
                        onClick={() => setMobileMenuOpen(false)}
                      >
                        <Building className="w-4 h-4" />
                        My Units
                      </Link>
                      
                      <Link 
                        to="/profit-analytics" 
                        className={`block ${navLinkClass('/profit-analytics')}`}
                        onClick={() => setMobileMenuOpen(false)}
                      >
                        <TrendingUp className="w-4 h-4" />
                        Analytics
                      </Link>
                    </>
                  )}
                  
                  {/* Common authenticated mobile links */}
                  <Link 
                    to="/bookings" 
                    className={`block ${navLinkClass('/bookings')}`}
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    <Calendar className="w-4 h-4" />
                    {isGuest() ? 'My Bookings' : 'Bookings'}
                  </Link>

                  <div className="pt-4 border-t border-white/10 space-y-2">
                    <div className="text-violet-200 text-sm mb-1 px-4">
                      Signed in as {user?.firstName || 'User'}
                    </div>
                    <div className="text-violet-300 text-xs mb-3 px-4">
                      {user?.role === 'GUEST' ? 'Guest' : user?.role === 'OWNER' ? 'Property Owner' : 'Admin'}
                    </div>
                    
                    {/* Apply as Owner Button for Guests who can apply - Mobile */}
                    {canApplyAsOwner() && (
                      <button
                        onClick={() => {
                          handleOwnerApplication();
                          setMobileMenuOpen(false);
                        }}
                        className="w-full px-4 py-3 mb-3 bg-gradient-to-r from-yellow-500/20 to-orange-500/20 hover:from-yellow-500/30 hover:to-orange-500/30 text-yellow-300 hover:text-yellow-200 rounded-xl transition-all duration-300 flex items-center gap-2 border border-yellow-500/30"
                      >
                        <Crown className="w-4 h-4" />
                        <div className="flex-1 text-left">
                          <div className="font-medium text-sm">Turn your dreams into business!🏡</div>
                          <div className="text-xs text-yellow-400">Apply as Owner</div>
                        </div>
                      </button>
                    )}

                    {/* Show application status for users with existing applications - Mobile */}
                    {hasOwnerApplication() && (
                      <div className="w-full px-4 py-3 mb-3 rounded-xl border border-purple-300/30">
                        <div className="text-white font-medium text-sm mb-1">Owner Application</div>
                        <div className={`text-xs font-medium ${
                          getOwnerStatus() === 'PENDING' ? 'text-yellow-300' :
                          getOwnerStatus() === 'APPROVED' ? 'text-green-300' :
                          getOwnerStatus() === 'REJECTED' ? 'text-red-300' :
                          'text-gray-300'
                        }`}>
                          {getOwnerStatus() === 'PENDING' && '⏳ Pending Review'}
                          {getOwnerStatus() === 'APPROVED' && '✅ Approved'}
                          {getOwnerStatus() === 'REJECTED' && '❌ Rejected'}
                        </div>
                      </div>
                    )}

                    <button
                      onClick={handleLogout}
                      className="w-full text-left px-4 py-2 text-violet-200 hover:text-white hover:bg-white/10 rounded-xl transition-colors flex items-center gap-2"
                    >
                      <LogOut className="w-4 h-4" />
                      Log Out
                    </button>
                    <button
                      onClick={() => {
                        setDeleteAccountModalOpen(true);
                        setMobileMenuOpen(false);
                      }}
                      className="w-full text-left px-4 py-2 text-red-300 hover:text-red-200 hover:bg-red-500/10 rounded-xl transition-colors flex items-center gap-2"
                    >
                      <Trash2 className="w-4 h-4" />
                      Delete Account
                    </button>
                  </div>
                </>
              ) : (
                /* Non-authenticated Mobile Links */
                <div className="pt-4 border-t border-white/10 space-y-2">
                  <Link
                    to="/login"
                    className="block px-4 py-2 text-violet-200 hover:text-white hover:bg-white/10 rounded-xl transition-colors flex items-center gap-2"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    <LogIn className="w-4 h-4" />
                    Login
                  </Link>
                  <Link
                    to="/register"
                    className="block px-4 py-2 bg-violet-600 hover:bg-violet-700 text-white rounded-xl transition-all duration-300 flex items-center gap-2"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    <UserPlus className="w-4 h-4" />
                    Sign Up
                  </Link>
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Delete Account Modal */}
      {deleteAccountModalOpen && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-slate-800/95 backdrop-blur-md border border-white/20 rounded-2xl p-6 w-full max-w-md">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 bg-red-500/20 rounded-full flex items-center justify-center">
                <Trash2 className="w-5 h-5 text-red-400" />
              </div>
              <div>
                <h3 className="text-lg font-semibold text-white">Delete Account</h3>
                <p className="text-white/70 text-sm">This action cannot be undone</p>
              </div>
            </div>
            
            <div className="mb-6">
              <p className="text-white/80 mb-4">
                Are you sure you want to permanently delete your account? All your data will be lost.
              </p>
              <p className="text-white/70 text-sm mb-3">
                Type <span className="font-mono font-bold text-red-400">DELETE</span> to confirm:
              </p>
              <input
                type="text"
                value={deleteConfirmText}
                onChange={(e) => setDeleteConfirmText(e.target.value)}
                className="w-full px-4 py-3 bg-white/5 border border-white/20 rounded-xl text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-red-500/50 focus:border-red-500/50"
                placeholder="Type DELETE here"
              />
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => {
                  setDeleteAccountModalOpen(false);
                  setDeleteConfirmText('');
                }}
                className="flex-1 px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-xl transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteAccount}
                disabled={deleteConfirmText !== 'DELETE'}
                className="flex-1 px-4 py-2 bg-red-500 hover:bg-red-600 disabled:bg-red-500/50 disabled:cursor-not-allowed text-white rounded-xl transition-colors"
              >
                Delete Account
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Owner Application Modal */}
      {ownerApplicationModalOpen && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-slate-800/95 backdrop-blur-md border border-white/20 rounded-2xl p-6 w-full max-w-lg">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-12 h-12 bg-gradient-to-r from-yellow-500/20 to-orange-500/20 rounded-full flex items-center justify-center">
                <Crown className="w-6 h-6 text-yellow-400" />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-white">Become a Property Owner!</h3>
                <p className="text-white/70">Start your hosting journey today</p>
              </div>
            </div>
            
            <div className="mb-6">
              <p className="text-white/80 mb-4">
                Ready to share your amazing property with travelers? Join our community of hosts and start earning!
              </p>
              
              <div className="space-y-3 mb-6">
                <div className="flex items-center gap-3 text-white/70">
                  <Star className="w-4 h-4 text-yellow-400" />
                  <span>List your property for free</span>
                </div>
                <div className="flex items-center gap-3 text-white/70">
                  <Star className="w-4 h-4 text-yellow-400" />
                  <span>Reach thousands of travelers</span>
                </div>
                <div className="flex items-center gap-3 text-white/70">
                  <Star className="w-4 h-4 text-yellow-400" />
                  <span>Manage bookings easily</span>
                </div>
                <div className="flex items-center gap-3 text-white/70">
                  <Star className="w-4 h-4 text-yellow-400" />
                  <span>Get detailed analytics</span>
                </div>
              </div>

              <div className="bg-gradient-to-r from-yellow-500/10 to-orange-500/10 border border-yellow-500/30 rounded-xl p-4">
                <p className="text-yellow-200 text-sm">
                  <strong>Note:</strong> Your account will be upgraded to Owner status. You'll get access to property management tools and can start listing your accommodations immediately.
                </p>
              </div>
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => setOwnerApplicationModalOpen(false)}
                className="flex-1 px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-xl transition-colors"
              >
                Maybe Later
              </button>
              <button
                onClick={async () => {
                  try {
                    console.log('🚀 Starting owner application submission...');
                    
                    const result = await ownerApplicationService.submitApplication(
                      'I would like to become a property owner on TurismApp to list my accommodations and start earning.'
                    );
                    
                    console.log('✅ Owner application submitted successfully:', result);
                    
                    // Check if backend returned updated user data
                    if (result.updatedUser) {
                      console.log('🔄 Updating user state with fresh data from server:', result.updatedUser);
                      
                      // Update the user state immediately for instant UI refresh
                      updateUser(result.updatedUser);
                      
                      success('Application Submitted!', result.message || 'Your owner application has been submitted successfully!');
                      setOwnerApplicationModalOpen(false);
                      
                      console.log('✨ UI state updated successfully - no page reload needed!');
                    } else {
                      // Fallback: If backend doesn't return updated user data, use refresh method
                      console.log('⚠️ Backend response missing updatedUser, falling back to refresh...');
                      
                      success('Application Submitted!', result.message || 'Your owner application has been submitted successfully!');
                      setOwnerApplicationModalOpen(false);
                      
                      // Give a brief moment for success message, then refresh user data
                      setTimeout(async () => {
                        try {
                          await refreshUser();
                          console.log('✅ User data refreshed from server');
                        } catch (refreshError) {
                          console.error('❌ Failed to refresh user data:', refreshError);
                          // Last resort fallback
                          window.location.reload();
                        }
                      }, 500);
                    }
                    
                  } catch (error) {
                    console.error('❌ Owner application submission failed:', error);
                    showError('Application Failed', error.response?.data?.message || 'Failed to submit owner application. Please try again.');
                  }
                }}
                className="flex-1 px-4 py-2 bg-gradient-to-r from-yellow-500 to-orange-500 hover:from-yellow-600 hover:to-orange-600 text-white rounded-xl transition-all duration-300 font-medium"
              >
                Apply Now!
              </button>
            </div>
          </div>
        </div>
      )}
      </div>
    </nav>
  );
};

export default NavBar;
