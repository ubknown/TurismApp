import React from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import Layout from '../layouts/Layout';
import HomePage from '../pages/HomePage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import ForgotPasswordPage from '../pages/ForgotPasswordPage';
import ResetPasswordPage from '../pages/ResetPasswordPage';
import EmailConfirmedPage from '../pages/EmailConfirmedPage';
import DashboardPage from '../pages/EnhancedDashboard';
import UnitsListPage from '../pages/UnitsListPage';
import UnitDetailsPage from '../pages/UnitDetailsPage';
import MyUnitsPage from '../pages/MyUnitsPage';
import AddUnitPage from '../pages/AddUnitPage';
import EditUnitPage from '../pages/EditUnitPage';
import ProfitAnalyticsPage from '../pages/ProfitAnalyticsPage';
import BookingsPage from '../pages/BookingsPage';
import DatabaseDebugPage from '../pages/DatabaseDebugPage';
import OwnerApplicationResponsePage from '../pages/TestOwnerPage.jsx';
import AdminDashboard from '../pages/AdminDashboard';
import NotFoundPage from '../pages/NotFoundPage';
import PrivateRoute from '../components/PrivateRoute';
import RoleRoute from '../components/RoleRoute';

const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      {
        index: true,
        element: <HomePage />
      },
      {
        path: 'login',
        element: <LoginPage />
      },
      {
        path: 'register',
        element: <RegisterPage />
      },
      {
        path: 'forgot-password',
        element: <ForgotPasswordPage />
      },
      {
        path: 'reset-password',
        element: <ResetPasswordPage />
      },
      {
        path: 'email-confirmed',
        element: <EmailConfirmedPage />
      },
      {
        path: 'confirm',
        element: <EmailConfirmedPage />
      },
      {
        path: 'owner-application-response',
        element: <OwnerApplicationResponsePage />
      },
      {
        path: 'admin',
        element: <AdminDashboard />
      },
      {
        path: 'dashboard',
        element: (
          <RoleRoute allowedRoles={['OWNER', 'ADMIN']}>
            <DashboardPage />
          </RoleRoute>
        )
      },
      {
        path: 'units',
        element: <UnitsListPage />
      },
      {
        path: 'units/:id',
        element: <UnitDetailsPage />
      },
      {
        path: 'my-units',
        element: (
          <RoleRoute allowedRoles={['OWNER', 'ADMIN']}>
            <MyUnitsPage />
          </RoleRoute>
        )
      },
      {
        path: 'my-units/add',
        element: (
          <RoleRoute allowedRoles={['OWNER', 'ADMIN']}>
            <AddUnitPage />
          </RoleRoute>
        )
      },
      {
        path: 'my-units/edit/:id',
        element: (
          <RoleRoute allowedRoles={['OWNER', 'ADMIN']}>
            <EditUnitPage />
          </RoleRoute>
        )
      },
      {
        path: 'profit-analytics',
        element: (
          <RoleRoute allowedRoles={['OWNER', 'ADMIN']}>
            <ProfitAnalyticsPage />
          </RoleRoute>
        )
      },
      {
        path: 'bookings',
        element: (
          <PrivateRoute>
            <BookingsPage />
          </PrivateRoute>
        )
      },
      {
        path: 'debug',
        element: <DatabaseDebugPage />
      },
      // Catch-all route for 404
      {
        path: '*',
        element: <NotFoundPage />
      }
    ]
  }
]);

const AppRouter = () => {
  return <RouterProvider router={router} />;
};

export default AppRouter;
