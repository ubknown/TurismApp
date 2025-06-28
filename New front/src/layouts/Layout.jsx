import React from 'react';
import { Outlet } from 'react-router-dom';
import BackgroundLayer from '../components/BackgroundLayer';
import NavBar from '../components/NavBar';
import OwnerApplicationBanner from '../components/OwnerApplicationBanner';

const Layout = () => {
  return (
    <div className="min-h-screen relative">
      <BackgroundLayer />
      
      <div className="relative z-10">
        <NavBar />
        <OwnerApplicationBanner />
        <Outlet />
      </div>
    </div>
  );
};

export default Layout;
