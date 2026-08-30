import type { ButtonHTMLAttributes } from 'react'
import './Button.css'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'ghost' | 'danger'
  block?: boolean
  size?: 'md' | 'lg'
}

export function Button({ variant = 'primary', block, size = 'md', className, ...props }: ButtonProps) {
  const cls = ['btn', `btn-${variant}`, block && 'btn-block', size === 'lg' && 'btn-lg', className]
    .filter(Boolean)
    .join(' ')
  return <button className={cls} {...props} />
}
