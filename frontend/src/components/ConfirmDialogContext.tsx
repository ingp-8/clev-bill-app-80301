import { createContext, useCallback, useContext, useEffect, useRef, useState, type ReactNode } from 'react'
import { Button } from './ui'
import './ConfirmDialog.css'

interface ConfirmOptions {
  title?: string
  message: string
  confirmLabel?: string
  cancelLabel?: string
  danger?: boolean
}

type ConfirmFn = (options: ConfirmOptions | string) => Promise<boolean>

const ConfirmContext = createContext<ConfirmFn | undefined>(undefined)

export function ConfirmDialogProvider({ children }: { children: ReactNode }) {
  const [options, setOptions] = useState<ConfirmOptions | null>(null)
  const resolver = useRef<((value: boolean) => void) | null>(null)

  const confirm = useCallback<ConfirmFn>((opts) => {
    const normalized = typeof opts === 'string' ? { message: opts } : opts
    setOptions(normalized)
    return new Promise<boolean>((resolve) => {
      resolver.current = resolve
    })
  }, [])

  const close = useCallback((result: boolean) => {
    setOptions(null)
    resolver.current?.(result)
    resolver.current = null
  }, [])

  useEffect(() => {
    if (!options) return
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') close(false)
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [options, close])

  return (
    <ConfirmContext.Provider value={confirm}>
      {children}
      {options && (
        <div className="confirm-backdrop" onClick={() => close(false)}>
          <div
            className="confirm-dialog"
            role="alertdialog"
            aria-modal="true"
            aria-label={options.title ?? 'Confirm'}
            onClick={(e) => e.stopPropagation()}
          >
            {options.title && <h2>{options.title}</h2>}
            <p>{options.message}</p>
            <div className="confirm-actions">
              <Button type="button" variant="ghost" onClick={() => close(false)}>
                {options.cancelLabel ?? 'Cancel'}
              </Button>
              <Button type="button" variant={options.danger ? 'danger' : 'primary'} onClick={() => close(true)} autoFocus>
                {options.confirmLabel ?? 'Confirm'}
              </Button>
            </div>
          </div>
        </div>
      )}
    </ConfirmContext.Provider>
  )
}

export function useConfirm(): ConfirmFn {
  const ctx = useContext(ConfirmContext)
  if (!ctx) {
    throw new Error('useConfirm must be used within a ConfirmDialogProvider')
  }
  return ctx
}
