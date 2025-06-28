import React from 'react';

const BackgroundLayer = () => {
  return (
    <div className="fixed inset-0 -z-10 overflow-hidden">
      {/* Night sky gradient background */}
      <div className="absolute inset-0 bg-gradient-to-br from-slate-900 via-purple-900 via-violet-800 to-indigo-900">
        
        {/* Stars layer */}
        <div className="absolute inset-0">
          {/* Generate random stars */}
          {[...Array(150)].map((_, i) => (
            <div
              key={i}
              className="absolute bg-white rounded-full animate-twinkle"
              style={{
                left: `${Math.random() * 100}%`,
                top: `${Math.random() * 70}%`,
                width: `${Math.random() * 3 + 1}px`,
                height: `${Math.random() * 3 + 1}px`,
                animationDelay: `${Math.random() * 4}s`,
                animationDuration: `${Math.random() * 3 + 2}s`,
                opacity: Math.random() * 0.8 + 0.2,
              }}
            />
          ))}
        </div>

        {/* Aurora effect overlay */}
        <div className="absolute inset-0 bg-gradient-to-t from-transparent via-purple-500/10 to-indigo-500/20 animate-pulse" 
             style={{ animationDuration: '4s' }} />
        
        {/* Mountain silhouettes */}
        <div className="absolute bottom-0 left-0 right-0">
          {/* Back mountains */}
          <svg 
            className="absolute bottom-0 w-full h-64 text-slate-800/60" 
            viewBox="0 0 1200 300" 
            preserveAspectRatio="none"
          >
            <path 
              fill="currentColor" 
              d="M0,300 L0,200 Q150,100 300,120 Q450,140 600,80 Q750,120 900,100 Q1050,80 1200,120 L1200,300 Z"
            />
          </svg>
          
          {/* Front mountains */}
          <svg 
            className="absolute bottom-0 w-full h-48 text-slate-900/80" 
            viewBox="0 0 1200 200" 
            preserveAspectRatio="none"
          >
            <path 
              fill="currentColor" 
              d="M0,200 L0,150 Q200,50 400,80 Q600,110 800,70 Q1000,90 1200,60 L1200,200 Z"
            />
          </svg>
          
          {/* Forest treeline */}
          <div className="absolute bottom-0 w-full h-32 bg-gradient-to-t from-slate-900 to-transparent">
            {/* Tree silhouettes */}
            {[...Array(20)].map((_, i) => (
              <div
                key={i}
                className="absolute bottom-0 bg-slate-900"
                style={{
                  left: `${i * 5 + Math.random() * 3}%`,
                  width: `${Math.random() * 8 + 4}px`,
                  height: `${Math.random() * 40 + 20}px`,
                  clipPath: 'polygon(50% 0%, 0% 100%, 100% 100%)',
                }}
              />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default BackgroundLayer;
