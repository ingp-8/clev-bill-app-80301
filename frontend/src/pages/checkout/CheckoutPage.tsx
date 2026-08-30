import { useEffect, useMemo, useRef, useState, type KeyboardEvent } from 'react'
import { itemHooks } from '../../hooks/useMasters'
import { usePosTerminalsByProperty } from '../../hooks/usePosTerminals'
import { useCheckout, useEInvoice } from '../../hooks/useSales'
import { useProperty } from '../../property/PropertyContext'
import type { Item } from '../../api/masters'
import type { Sale } from '../../api/sales'

interface CartLine {
  item: Item
  quantity: number
}

function round2(value: number): number {
  return Math.round(value * 100) / 100
}

function EInvoiceStatusBadge({ saleId }: { saleId: number }) {
  const { data: eInvoice, isError } = useEInvoice(saleId)
  if (isError || !eInvoice) return null

  return (
    <p style={{ marginTop: 8 }}>
      E-invoice: <strong>{eInvoice.status}</strong>
      {eInvoice.status === 'SUCCESS' && eInvoice.irn && <> — IRN {eInvoice.irn.slice(0, 16)}...</>}
      {eInvoice.status === 'FAILED' && eInvoice.lastError && <> — {eInvoice.lastError}</>}
    </p>
  )
}

export function CheckoutPage() {
  const { activeProperty } = useProperty()
  const propertyId = activeProperty!.id
  const { data: items = [] } = itemHooks.useListByProperty(propertyId)
  const { data: posTerminals = [] } = usePosTerminalsByProperty(propertyId)
  const checkoutMutation = useCheckout()

  const [scanValue, setScanValue] = useState('')
  const [scanError, setScanError] = useState<string | null>(null)
  const [cart, setCart] = useState<CartLine[]>([])
  const [paymentMethod, setPaymentMethod] = useState<'CASH' | 'CARD' | 'UPI' | 'GIFT_CARD' | 'OTHER'>('CASH')
  const [lastSale, setLastSale] = useState<Sale | null>(null)
  const [posId, setPosId] = useState<number | null>(null)
  const scanInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (posTerminals.length === 0) return
    const stillValid = posTerminals.some((p) => p.id === posId)
    if (!stillValid) setPosId(posTerminals[0].id)
  }, [posTerminals, posId])

  const itemsByCode = useMemo(() => {
    const map = new Map<string, Item>()
    for (const item of items) {
      map.set(item.sku.toLowerCase(), item)
      if (item.barcode) map.set(item.barcode.toLowerCase(), item)
    }
    return map
  }, [items])

  const totals = useMemo(() => {
    let subtotal = 0
    let tax = 0
    for (const line of cart) {
      const lineSubtotal = line.item.sellingPrice * line.quantity
      const lineTax = lineSubtotal * ((line.item.taxRate.cgstRate + line.item.taxRate.sgstRate) / 100)
      subtotal += lineSubtotal
      tax += lineTax
    }
    return { subtotal: round2(subtotal), tax: round2(tax), total: round2(subtotal + tax) }
  }, [cart])

  const [paymentAmount, setPaymentAmount] = useState('')
  const effectivePaymentAmount = paymentAmount === '' ? totals.total : Number(paymentAmount)

  function addToCart(item: Item) {
    setCart((prev) => {
      const existing = prev.find((line) => line.item.id === item.id)
      if (existing) {
        return prev.map((line) => (line.item.id === item.id ? { ...line, quantity: line.quantity + 1 } : line))
      }
      return [...prev, { item, quantity: 1 }]
    })
  }

  function handleScanKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key !== 'Enter') return
    const code = scanValue.trim().toLowerCase()
    if (!code) return
    const item = itemsByCode.get(code)
    if (item) {
      addToCart(item)
      setScanError(null)
    } else {
      setScanError(`No item found for "${scanValue.trim()}"`)
    }
    setScanValue('')
  }

  function updateQuantity(itemId: number, quantity: number) {
    if (quantity <= 0) {
      setCart((prev) => prev.filter((line) => line.item.id !== itemId))
      return
    }
    setCart((prev) => prev.map((line) => (line.item.id === itemId ? { ...line, quantity } : line)))
  }

  function removeLine(itemId: number) {
    setCart((prev) => prev.filter((line) => line.item.id !== itemId))
  }

  async function handleCompleteSale() {
    if (cart.length === 0 || posId === null) return
    const sale = await checkoutMutation.mutateAsync({
      propertyId,
      posId,
      customerId: null,
      items: cart.map((line) => ({ itemId: line.item.id, quantity: line.quantity })),
      payments: [{ method: paymentMethod, amount: effectivePaymentAmount }],
    })
    setLastSale(sale)
    setCart([])
    setPaymentAmount('')
    scanInputRef.current?.focus()
  }

  return (
    <div style={{ maxWidth: 800, margin: '24px auto', textAlign: 'left', padding: '0 16px' }}>
      <h1 style={{ fontSize: 24 }}>Checkout</h1>

      {posTerminals.length > 1 && (
        <label style={{ display: 'block', marginBottom: 8 }}>
          POS terminal{' '}
          <select value={posId ?? ''} onChange={(e) => setPosId(Number(e.target.value))} style={{ padding: 6 }}>
            {posTerminals.map((p) => (
              <option key={p.id} value={p.id}>
                {p.posName}
              </option>
            ))}
          </select>
        </label>
      )}

      <input
        ref={scanInputRef}
        value={scanValue}
        onChange={(e) => setScanValue(e.target.value)}
        onKeyDown={handleScanKeyDown}
        placeholder="Scan barcode or type SKU, then Enter"
        autoFocus
        style={{ width: '100%', padding: 10, fontSize: 16, marginBottom: 8 }}
      />
      {scanError && <p style={{ color: 'crimson', margin: '0 0 12px' }}>{scanError}</p>}

      <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: 16 }}>
        <thead>
          <tr>
            <th style={{ textAlign: 'left', borderBottom: '1px solid var(--border)', padding: '6px 4px' }}>Item</th>
            <th style={{ textAlign: 'right', borderBottom: '1px solid var(--border)', padding: '6px 4px' }}>Qty</th>
            <th style={{ textAlign: 'right', borderBottom: '1px solid var(--border)', padding: '6px 4px' }}>Price</th>
            <th style={{ textAlign: 'right', borderBottom: '1px solid var(--border)', padding: '6px 4px' }}>Line Total</th>
            <th style={{ borderBottom: '1px solid var(--border)', padding: '6px 4px' }} />
          </tr>
        </thead>
        <tbody>
          {cart.length === 0 && (
            <tr>
              <td colSpan={5} style={{ padding: '12px 4px', color: 'var(--text)' }}>
                Cart is empty — scan an item to begin
              </td>
            </tr>
          )}
          {cart.map((line) => (
            <tr key={line.item.id}>
              <td style={{ padding: '6px 4px' }}>{line.item.name}</td>
              <td style={{ padding: '6px 4px', textAlign: 'right' }}>
                <input
                  type="number"
                  step="0.001"
                  value={line.quantity}
                  onChange={(e) => updateQuantity(line.item.id, Number(e.target.value))}
                  style={{ width: 70, textAlign: 'right', padding: 4 }}
                />
              </td>
              <td style={{ padding: '6px 4px', textAlign: 'right' }}>{line.item.sellingPrice.toFixed(2)}</td>
              <td style={{ padding: '6px 4px', textAlign: 'right' }}>
                {(line.item.sellingPrice * line.quantity).toFixed(2)}
              </td>
              <td style={{ padding: '6px 4px' }}>
                <button type="button" onClick={() => removeLine(line.item.id)}>
                  Remove
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 16 }}>
        <table>
          <tbody>
            <tr>
              <td style={{ padding: '2px 12px 2px 0' }}>Subtotal</td>
              <td style={{ textAlign: 'right' }}>{totals.subtotal.toFixed(2)}</td>
            </tr>
            <tr>
              <td style={{ padding: '2px 12px 2px 0' }}>Tax</td>
              <td style={{ textAlign: 'right' }}>{totals.tax.toFixed(2)}</td>
            </tr>
            <tr>
              <td style={{ padding: '2px 12px 2px 0', fontWeight: 700 }}>Total</td>
              <td style={{ textAlign: 'right', fontWeight: 700 }}>{totals.total.toFixed(2)}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 16 }}>
        <select value={paymentMethod} onChange={(e) => setPaymentMethod(e.target.value as typeof paymentMethod)} style={{ padding: 8 }}>
          <option value="CASH">Cash</option>
          <option value="CARD">Card</option>
          <option value="UPI">UPI</option>
          <option value="GIFT_CARD">Gift Card</option>
          <option value="OTHER">Other</option>
        </select>
        <input
          type="number"
          step="0.01"
          value={paymentAmount}
          onChange={(e) => setPaymentAmount(e.target.value)}
          placeholder={totals.total.toFixed(2)}
          style={{ padding: 8, width: 120 }}
        />
        <button
          type="button"
          onClick={handleCompleteSale}
          disabled={cart.length === 0 || posId === null || checkoutMutation.isPending}
          style={{ padding: '8px 16px', fontWeight: 700 }}
        >
          {checkoutMutation.isPending ? 'Processing...' : 'Complete Sale'}
        </button>
      </div>

      {checkoutMutation.isError && (
        <p style={{ color: 'crimson' }}>
          {(checkoutMutation.error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
            'Checkout failed'}
        </p>
      )}

      {lastSale && (
        <div style={{ border: '1px solid var(--border)', padding: 12, marginTop: 16 }}>
          <strong>Sale complete — {lastSale.billNumber}</strong>
          <p>Total charged: {lastSale.totalAmount.toFixed(2)}</p>
          <EInvoiceStatusBadge saleId={lastSale.id} />
        </div>
      )}
    </div>
  )
}
