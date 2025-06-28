import React from 'react';

const TestGlass = () => {
  return (
    <div className="fixed top-4 right-4 z-50">
      <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-xl p-4">
        <div className="text-white font-bold">Glass Test</div>
        <div className="text-gray-300 text-sm">If you see this with glassmorphism, it's working!</div>
      </div>
    </div>
  );
};

export default TestGlass;
