import React from 'react';

const GlassCard = ({ 
  children, 
  className = '', 
  padding = 'p-6',
  blur = 'backdrop-blur-xl',
  opacity = 'bg-white bg-opacity-10',
  hover = true,
  ...props 
}) => {
  const hoverEffect = hover ? 'hover:bg-opacity-15 hover:shadow-2xl' : '';
  
  return (
    <div
      className={`
        ${opacity} ${blur} border border-white border-opacity-20 rounded-2xl shadow-xl
        ${padding} transition-all duration-300 ${hoverEffect}
        ${className}
      `}
      {...props}
    >
      {children}
    </div>
  );
};

export default GlassCard;
