import React from 'react';

export interface BadgeProps {
  id?: string;
  variant?: 'neutral' | 'success' | 'danger' | 'warning' | 'info' | 'primary';
  size?: 'sm' | 'md';
  children: React.ReactNode;
  icon?: React.ReactNode;
  className?: string;
  onRemove?: () => void;
}

export const Badge: React.FC<BadgeProps> = ({
  id,
  variant = 'neutral',
  size = 'md',
  children,
  icon,
  className = '',
  onRemove,
}) => {
  const variantStyles = {
    neutral: 'bg-[#1B202A] text-[#94A3B8] border border-[#2D333B]',
    success: 'bg-[#10B981]/15 text-[#10B981] border border-[#10B981]/30',
    danger: 'bg-[#F43F5E]/15 text-[#FB7185] border border-[#F43F5E]/30',
    warning: 'bg-[#F59E0B]/15 text-[#F59E0B] border border-[#F59E0B]/30',
    info: 'bg-[#6366F1]/15 text-[#818CF8] border border-[#6366F1]/30',
    primary: 'bg-[#6366F1]/20 text-[#818CF8] border border-[#6366F1]/40',
  };

  const sizeStyles = {
    sm: 'text-xs px-2 py-0.5 gap-1',
    md: 'text-sm px-2.5 py-1 gap-1.5',
  };

  return (
    <span
      id={id}
      className={`inline-flex items-center font-medium rounded-md font-mono ${variantStyles[variant]} ${sizeStyles[size]} ${className}`}
    >
      {icon}
      <span>{children}</span>
      {onRemove && (
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            onRemove();
          }}
          className="ml-1 hover:opacity-75 focus:outline-none"
          aria-label={`삭제: ${children}`}
        >
          ×
        </button>
      )}
    </span>
  );
};
