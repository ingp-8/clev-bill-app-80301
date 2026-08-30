import { useEffect, useMemo, useRef, useState, type KeyboardEvent } from 'react'
import { Button, Input, Select } from '../../components/ui'
import { itemHooks } from '../../hooks/useMasters'
import { usePosTerminalsByProperty } from '../../hooks/usePosTerminals'
import { useCheckout, useEInvoice } from '../../hooks/useSales'
import { useProperty } from '../../property/PropertyContext'
import type { Item } from '../../api/masters'
import type { Sale } from '../../api/sales'
import './CheckoutPage.css'

interface CartLine {
  item: Item
  quantity: number
}

const PAYMENT_METHODS: { value: 'CASH' | 'CARD' | 'UPI' | 'GIFT_CARD' | 'OTHER'; label: string }[] = [
  { value: 'CASH', label: 'Cash' },
  { value: 'CARD', label: 'Card' },
  { value: 'UPI', label: 'UPI' },
  { value: 'GIFT_CARD', label: 'Gift Card' },
  { value: 'OTHER', label: 'Other' },
]

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
  const [paymentMethod, setPaymentMethod] = useState<(typeof PAYMENT_METHODS)[number]['value']>('CASH')
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
    <div className="page">
      <div className="page-header">
        <h1>Checkout</h1>
        {posTerminals.length > 1 && (
          <Select value={posId ?? ''} onChange={(e) => setPosId(Number(e.target.value))} style={{ marginTop: 8 }}>
            {posTerminals.map((p) => (
              <option key={p.id} value={p.id}>
                {p.posName}
              </option>
            ))}
          </Select>
        )}
      </div>

      <Input
        ref={scanInputRef}
        block
        value={scanValue}
        onChange={(e) => setScanValue(e.target.value)}
        onKeyDown={handleScanKeyDown}
        placeholder="Scan barcode or type SKU, then Enter"
        autoFocus
        className="checkout-scan"
      />
      {scanError && <p className="checkout-error">{scanError}</p>}

      <div className="checkout-grid">
        <div>
          {cart.length === 0 && <p className="cart-empty">Cart is empty — scan an item to begin</p>}
          {cart.map((line) => (
            <div className="cart-card" key={line.item.id}>
              <div>
                <div className="name">{line.item.name}</div>
                <div className="meta">₹{line.item.sellingPrice.toFixed(2)} each</div>
              </div>
              <Input
                type="number"
                step="0.001"
                value={line.quantity}
                onChange={(e) => updateQuantity(line.item.id, Number(e.target.value))}
                className="qty-input"
              />
              <div className="line-total">₹{(line.item.sellingPrice * line.quantity).toFixed(2)}</div>
              <Button type="button" variant="ghost" onClick={() => removeLine(line.item.id)}>
                Remove
              </Button>
            </div>
          ))}
        </div>

        <div className="totals-panel">
          <div className="totals-row">
            <span>Subtotal</span>
            <span>₹{totals.subtotal.toFixed(2)}</span>
          </div>
          <div className="totals-row">
            <span>Tax</span>
            <span>₹{totals.tax.toFixed(2)}</span>
          </div>
          <div className="totals-row grand">
            <span>Total</span>
            <span>₹{totals.total.toFixed(2)}</span>
          </div>

          <div className="pay-pills">
            {PAYMENT_METHODS.map((m) => (
              <button
                key={m.value}
                type="button"
                className={m.value === paymentMethod ? 'active' : undefined}
                onClick={() => setPaymentMethod(m.value)}
              >
                {m.label}
              </button>
            ))}
          </div>

          <Select
            block
            className="pay-select"
            value={paymentMethod}
            onChange={(e) => setPaymentMethod(e.target.value as typeof paymentMethod)}
          >
            {PAYMENT_METHODS.map((m) => (
              <option key={m.value} value={m.value}>
                {m.label}
              </option>
            ))}
          </Select>
          <Input
            type="number"
            step="0.01"
            block
            value={paymentAmount}
            onChange={(e) => setPaymentAmount(e.target.value)}
            placeholder={totals.total.toFixed(2)}
            style={{ marginBottom: 10 }}
          />

          <Button
            type="button"
            block
            size="lg"
            onClick={handleCompleteSale}
            disabled={cart.length === 0 || posId === null || checkoutMutation.isPending}
          >
            {checkoutMutation.isPending ? 'Processing...' : 'Complete Sale'}
          </Button>

          {checkoutMutation.isError && (
            <p className="checkout-error" style={{ marginTop: 10 }}>
              {(checkoutMutation.error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
                'Checkout failed'}
            </p>
          )}
        </div>
      </div>

      {lastSale && (
        <div className="sale-complete">
          <strong>Sale complete — {lastSale.billNumber}</strong>
          <p>Total charged: ₹{lastSale.totalAmount.toFixed(2)}</p>
          <EInvoiceStatusBadge saleId={lastSale.id} />
        </div>
      )}
    </div>
  )
}
