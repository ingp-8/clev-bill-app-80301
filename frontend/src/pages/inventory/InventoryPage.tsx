import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { DataTable } from '../../components/DataTable'
import { itemHooks } from '../../hooks/useMasters'
import { useAdjustInventory, useInventoryList } from '../../hooks/useInventory'
import { useProperty } from '../../property/PropertyContext'

export function InventoryPage() {
  const { activeProperty } = useProperty()
  const { data: levels = [], isLoading } = useInventoryList()
  const { data: items = [] } = itemHooks.useListByProperty(activeProperty!.id)
  const adjustMutation = useAdjustInventory()

  const [itemId, setItemId] = useState('')
  const [quantityDelta, setQuantityDelta] = useState('')
  const [note, setNote] = useState('')

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!itemId || !quantityDelta) return
    await adjustMutation.mutateAsync({
      itemId: Number(itemId),
      quantityDelta: Number(quantityDelta),
      note: note || null,
    })
    setItemId('')
    setQuantityDelta('')
    setNote('')
  }

  return (
    <div style={{ maxWidth: 800, margin: '24px auto', textAlign: 'left', padding: '0 16px' }}>
      <p>
        <Link to="/">&larr; Back</Link>
      </p>
      <h1 style={{ fontSize: 24 }}>Inventory</h1>

      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 16, flexWrap: 'wrap' }}>
        <select value={itemId} onChange={(e) => setItemId(e.target.value)} required style={{ padding: 6 }}>
          <option value="">Item...</option>
          {items.map((item) => (
            <option key={item.id} value={item.id}>
              {item.sku} — {item.name}
            </option>
          ))}
        </select>
        <input
          type="number"
          step="0.001"
          value={quantityDelta}
          onChange={(e) => setQuantityDelta(e.target.value)}
          placeholder="Qty (+/-)"
          required
          style={{ padding: 6, width: 100 }}
        />
        <input value={note} onChange={(e) => setNote(e.target.value)} placeholder="Note" style={{ padding: 6, flex: 1 }} />
        <button type="submit">Adjust</button>
      </form>

      {isLoading ? (
        <p>Loading...</p>
      ) : (
        <DataTable
          rows={levels}
          rowKey={(row) => row.itemId}
          columns={[
            { header: 'SKU', render: (row) => row.sku },
            { header: 'Item', render: (row) => <Link to={`/inventory/${row.itemId}`}>{row.itemName}</Link> },
            { header: 'Quantity', render: (row) => row.quantity },
          ]}
        />
      )}
    </div>
  )
}
