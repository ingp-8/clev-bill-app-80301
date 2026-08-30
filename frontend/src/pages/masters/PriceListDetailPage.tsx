import { useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { DataTable } from '../../components/DataTable'
import { useConfirm } from '../../components/ConfirmDialogContext'
import { Button, Input, Select } from '../../components/ui'
import { itemHooks } from '../../hooks/useMasters'
import { useCreatePriceListItem, useDeletePriceListItem, usePriceListItems } from '../../hooks/useMasters'
import { useProperty } from '../../property/PropertyContext'
import type { PriceListItem } from '../../api/masters'

export function PriceListDetailPage() {
  const { priceListId } = useParams<{ priceListId: string }>()
  const id = Number(priceListId)
  const { activeProperty } = useProperty()

  const { data: rows = [], isLoading } = usePriceListItems(id)
  const { data: items = [] } = itemHooks.useListByProperty(activeProperty!.id)
  const createMutation = useCreatePriceListItem(id)
  const deleteMutation = useDeletePriceListItem(id)
  const confirmDialog = useConfirm()

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
    const ok = await confirmDialog({ message: `Remove "${row.item.name}" from this price list?`, confirmLabel: 'Remove', danger: true })
    if (ok) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div>
      <Link to="/masters/price-lists" className="page-back">
        &larr; Back to price lists
      </Link>
      <div className="page-header">
        <h1>Price List #{id} — Items</h1>
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
            step="0.01"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            placeholder="Price"
            required
            style={{ width: 110 }}
          />
          <Button type="submit">Add</Button>
        </div>
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
