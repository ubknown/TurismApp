import React from 'react';
import judeteData from '../data/judete-localitati.json';

const CountyDropdown = ({ 
  value, 
  onChange, 
  required = false, 
  disabled = false, 
  placeholder = "All",
  className = "",
  id = "county-select",
  name = "county",
  includeAllOption = true // New prop to control whether to include "All" option
}) => {
  const counties = Object.keys(judeteData).sort();

  return (
    <select
      id={id}
      name={name}
      value={value}
      onChange={onChange}
      required={required}
      disabled={disabled}
      className={className || "w-full px-4 py-3 bg-white/5 backdrop-blur-md border border-white/20 rounded-xl text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all duration-300 h-12"}
    >
      {includeAllOption && (
        <option value="" className="bg-slate-800 text-white">
          All
        </option>
      )}
      {!includeAllOption && !value && (
        <option value="" disabled className="bg-slate-800 text-gray-400">
          Select County / Județ
        </option>
      )}
      {counties.map(county => (
        <option key={county} value={county} className="bg-slate-800 text-white">
          {county}
        </option>
      ))}
    </select>
  );
};

export default CountyDropdown;
