import React from 'react';
import { Loader2 } from 'lucide-react';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost' | 'outline';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  id,
  children,
  variant = 'primary',
  size = 'md',
  isLoading = false,
  disabled = false,
  leftIcon,
  rightIcon,
  className = '',
  ...props
}) => {
  const baseStyles =
    'inline-flex items-center justify-center font-medium rounded-lg transition-all duration-150 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-[#0A0C10] disabled:opacity-50 disabled:cursor-not-allowed select-none active:scale-[0.98]';

  const sizeStyles = {
    sm: 'px-3 py-1.5 text-xs gap-1.5',
    md: 'px-4 py-2 text-sm gap-2',
    lg: 'px-6 py-2.5 text-base gap-2.5',
  };

  const variantStyles = {
    primary:
      'bg-[#6366F1] hover:bg-[#4F46E5] text-white focus:ring-[#6366F1] shadow-sm',
    secondary:
      'bg-[#1B202A] hover:bg-[#252C3A] text-[#E2E8F0] border border-[#2D333B] focus:ring-[#6366F1]',
    danger:
      'bg-[#F43F5E]/15 hover:bg-[#F43F5E]/25 text-[#FB7185] border border-[#F43F5E]/40 focus:ring-[#F43F5E]',
    ghost:
      'bg-transparent hover:bg-[#1B202A] text-slate-300 hover:text-white focus:ring-slate-400',
    outline:
      'bg-transparent hover:bg-[#1B202A] text-slate-300 hover:text-white border border-[#2D333B] hover:border-slate-500 focus:ring-[#6366F1]',
  };

  return (
    <button
      id={id}
      disabled={disabled || isLoading}
      className={`${baseStyles} ${sizeStyles[size]} ${variantStyles[variant]} ${className}`}
      {...props}
    >
      {isLoading ? (
        <Loader2 className="w-4 h-4 animate-spin text-current" aria-hidden="true" />
      ) : (
        leftIcon
      )}
      <span>{children}</span>
      {!isLoading && rightIcon}
    </button>
  );
};
