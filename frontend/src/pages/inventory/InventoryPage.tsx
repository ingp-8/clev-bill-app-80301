import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { DataTable } from '../../components/DataTable'
import { Button, Input, Select } from '../../components/ui'
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
    <div className="page">
      <div className="page-header">
        <h1>Inventory</h1>
      </div>

      <form onSubmit={handleSubmit} className="panel">
        <div className="form-row">
          <Select value={itemId} onChange={(e) => setItemId(e.target.value)} required>
            <option value="">Item...</option>
            {items.map((item) => (
              <option key={item.id} value={item.id}>
                {item.sku} — {item.name}
              </option>
            ))}
          </Select>
          <Input
            type="number"
            step="0.001"
            value={quantityDelta}
            onChange={(e) => setQuantityDelta(e.target.value)}
            placeholder="Qty (+/-)"
            required
            style={{ width: 110 }}
          />
          <Input value={note} onChange={(e) => setNote(e.target.value)} placeholder="Note" style={{ flex: 1, minWidth: 160 }} />
          <Button type="submit">Adjust</Button>
        </div>
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
