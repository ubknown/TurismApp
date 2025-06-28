import React from 'react';

const TypeDropdown = ({ 
  value, 
  onChange, 
  required = false, 
  disabled = false, 
  placeholder = "All",
  className = "",
  id = "type-select",
  name = "type",
  includeAllOption = true // New prop to control whether to include "All" option
}) => {
  const accommodationTypes = [
    { value: 'HOTEL', label: 'Hotel' },
    { value: 'PENSIUNE', label: 'Pensiune' },
    { value: 'CABANA', label: 'Cabana' },
    { value: 'VILA', label: 'Vila' },
    { value: 'APARTAMENT', label: 'Apartament' },
    { value: 'CASA_DE_VACANTA', label: 'Casă de vacanță' },
    { value: 'HOSTEL', label: 'Hostel' },
    { value: 'MOTEL', label: 'Motel' },
    { value: 'CAMPING', label: 'Camping' },
    { value: 'BUNGALOW', label: 'Bungalow' }
  ];

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
          All Types
        </option>
      )}
      {!includeAllOption && !value && (
        <option value="" disabled className="bg-slate-800 text-gray-400">
          Select Type of Accommodation
        </option>
      )}
      {accommodationTypes.map(type => (
        <option key={type.value} value={type.value} className="bg-slate-800 text-white">
          {type.label}
        </option>
      ))}
    </select>
  );
};

export default TypeDropdown;
