import { forwardRef } from 'react'
import type { InputHTMLAttributes, LabelHTMLAttributes, SelectHTMLAttributes } from 'react'
import './Field.css'

type InputProps = InputHTMLAttributes<HTMLInputElement> & { block?: boolean }

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input({ className, block, ...props }, ref) {
  const cls = ['field-input', block && 'field-input-block', className].filter(Boolean).join(' ')
  return <input ref={ref} className={cls} {...props} />
})

type SelectProps = SelectHTMLAttributes<HTMLSelectElement> & { block?: boolean }

export const Select = forwardRef<HTMLSelectElement, SelectProps>(function Select({ className, block, ...props }, ref) {
  const cls = ['field-input', block && 'field-input-block', className].filter(Boolean).join(' ')
  return <select ref={ref} className={cls} {...props} />
})

type CheckboxProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> & { label: string }

export function Checkbox({ label, className, ...props }: CheckboxProps) {
  const cls = ['field-checkbox', className].filter(Boolean).join(' ')
  return (
    <label className={cls}>
      <input type="checkbox" {...props} />
      {label}
    </label>
  )
}

export function FieldLabel(props: LabelHTMLAttributes<HTMLLabelElement>) {
  return <label className="field-label" {...props} />
}
