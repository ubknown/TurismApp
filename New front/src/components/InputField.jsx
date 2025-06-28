import React, { forwardRef } from 'react';

const InputField = forwardRef(({ 
  label,
  type = 'text',
  placeholder,
  icon: Icon,
  error,
  className = '',
  ...props 
}, ref) => {
  return (
    <div className="space-y-2">
      {label && (
        <label className="block text-sm font-medium text-white/80">
          {label}
        </label>
      )}
      
      <div className="relative">
        {Icon && (
          <div className="absolute left-3 top-1/2 transform -translate-y-1/2 text-white/60">
            <Icon className="w-5 h-5" />
          </div>
        )}
        
        <input
          ref={ref}
          type={type}
          placeholder={placeholder}
          className={`
            w-full bg-white/5 backdrop-blur-md border border-white/20 rounded-xl 
            ${Icon ? 'pl-10 pr-4' : 'px-4'} py-3 
            text-white placeholder-white/60 
            focus:outline-none focus:ring-2 focus:ring-aurora-violet/50 focus:border-aurora-violet/50 
            transition-all duration-300
            ${error ? 'border-red-400 focus:ring-red-400/50' : ''}
            ${className}
          `}
          {...props}
        />
      </div>
      
      {error && (
        <p className="text-sm text-red-400">{error}</p>
      )}
    </div>
  );
});

InputField.displayName = 'InputField';

export default InputField;
