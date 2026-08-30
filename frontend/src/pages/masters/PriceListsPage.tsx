import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { DataTable } from '../../components/DataTable'
import { priceListHooks } from '../../hooks/useMasters'
import { useProperty } from '../../property/PropertyContext'
import type { PriceList } from '../../api/masters'

export function PriceListsPage() {
  const { activeProperty } = useProperty()
  const propertyId = activeProperty!.id
  const { data: rows = [], isLoading } = priceListHooks.useListByProperty(propertyId)
  const createMutation = priceListHooks.useCreateInProperty(propertyId)
  const updateMutation = priceListHooks.useUpdate(propertyId)
  const deleteMutation = priceListHooks.useDelete(propertyId)

  const [editingId, setEditingId] = useState<number | null>(null)
  const [name, setName] = useState('')
  const [isDefault, setIsDefault] = useState(false)
  const [active, setActive] = useState(true)

  function resetForm() {
    setEditingId(null)
    setName('')
    setIsDefault(false)
    setActive(true)
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const request = { name, isDefault, active }
    if (editingId === null) {
      await createMutation.mutateAsync(request)
    } else {
      await updateMutation.mutateAsync({ id: editingId, request })
    }
    resetForm()
  }

  function handleEdit(row: PriceList) {
    setEditingId(row.id)
    setName(row.name)
    setIsDefault(row.isDefault)
    setActive(row.active)
  }

  async function handleDelete(row: PriceList) {
    if (confirm(`Delete "${row.name}"?`)) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div>
      <h2>Price Lists</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 16 }}>
        <input value={name} onChange={(e) => setName(e.target.value)} placeholder="Name" required style={{ padding: 6 }} />
        <label>
          <input type="checkbox" checked={isDefault} onChange={(e) => setIsDefault(e.target.checked)} /> Default
        </label>
        <label>
          <input type="checkbox" checked={active} onChange={(e) => setActive(e.target.checked)} /> Active
        </label>
        <button type="submit">{editingId === null ? 'Add' : 'Save'}</button>
        {editingId !== null && (
          <button type="button" onClick={resetForm}>
            Cancel
          </button>
        )}
      </form>

      {isLoading ? (
        <p>Loading...</p>
      ) : (
        <DataTable
          rows={rows}
          rowKey={(row) => row.id}
          onEdit={handleEdit}
          onDelete={handleDelete}
          columns={[
            { header: 'Name', render: (row) => <Link to={`/masters/price-lists/${row.id}`}>{row.name}</Link> },
            { header: 'Default', render: (row) => (row.isDefault ? 'Yes' : 'No') },
            { header: 'Active', render: (row) => (row.active ? 'Yes' : 'No') },
          ]}
        />
      )}
    </div>
  )
}
