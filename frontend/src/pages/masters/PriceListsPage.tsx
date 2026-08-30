import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { DataTable } from '../../components/DataTable'
import { useConfirm } from '../../components/ConfirmDialogContext'
import { Button, Checkbox, Input } from '../../components/ui'
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
  const confirmDialog = useConfirm()

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
    const ok = await confirmDialog({ message: `Delete "${row.name}"?`, confirmLabel: 'Delete', danger: true })
    if (ok) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1>Price Lists</h1>
      </div>

      <form onSubmit={handleSubmit} className="panel">
        <div className="form-row">
          <Input value={name} onChange={(e) => setName(e.target.value)} placeholder="Name" required />
          <Checkbox label="Default" checked={isDefault} onChange={(e) => setIsDefault(e.target.checked)} />
          <Checkbox label="Active" checked={active} onChange={(e) => setActive(e.target.checked)} />
          <Button type="submit">{editingId === null ? 'Add' : 'Save'}</Button>
          {editingId !== null && (
            <Button type="button" variant="ghost" onClick={resetForm}>
              Cancel
            </Button>
          )}
        </div>
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
