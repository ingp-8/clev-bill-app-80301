import { useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { DataTable } from '../../components/DataTable'
import { itemHooks } from '../../hooks/useMasters'
import { useCreatePriceListItem, useDeletePriceListItem, usePriceListItems } from '../../hooks/useMasters'
import type { PriceListItem } from '../../api/masters'

export function PriceListDetailPage() {
  const { priceListId } = useParams<{ priceListId: string }>()
  const id = Number(priceListId)

  const { data: rows = [], isLoading } = usePriceListItems(id)
  const { data: items = [] } = itemHooks.useList()
  const createMutation = useCreatePriceListItem(id)
  const deleteMutation = useDeletePriceListItem(id)

  const [itemId, setItemId] = useState('')
  const [price, setPrice] = useState('')

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!itemId || !price) return
    await createMutation.mutateAsync({ itemId: Number(itemId), price: Number(price) })
    setItemId('')
    setPrice('')
  }

  async function handleDelete(row: PriceListItem) {
    if (confirm(`Remove "${row.item.name}" from this price list?`)) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div>
      <p>
        <Link to="/masters/price-lists">&larr; Back to price lists</Link>
      </p>
      <h2>Price List #{id} — Items</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 16 }}>
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
          step="0.01"
          value={price}
          onChange={(e) => setPrice(e.target.value)}
          placeholder="Price"
          required
          style={{ padding: 6, width: 110 }}
        />
        <button type="submit">Add</button>
      </form>

      {isLoading ? (
        <p>Loading...</p>
      ) : (
        <DataTable
          rows={rows}
          rowKey={(row) => row.id}
          onDelete={handleDelete}
          columns={[
            { header: 'Item', render: (row) => row.item.name },
            { header: 'Price', render: (row) => row.price.toFixed(2) },
          ]}
        />
      )}
    </div>
  )
}
