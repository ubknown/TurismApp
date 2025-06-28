// Global toast utility for use outside of React components
let globalToast = null;

export const setGlobalToast = (toastInstance) => {
  globalToast = toastInstance;
};

export const getGlobalToast = () => {
  return globalToast;
};

export const showAutoLogoutToast = () => {
  if (globalToast) {
    globalToast.warning('Session Expired', 'You have been automatically logged out due to an expired session');
  }
};
