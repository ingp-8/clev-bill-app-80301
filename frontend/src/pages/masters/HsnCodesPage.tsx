import { useState, type FormEvent } from 'react'
import { DataTable } from '../../components/DataTable'
import { useConfirm } from '../../components/ConfirmDialogContext'
import { Button, Checkbox, Input } from '../../components/ui'
import { hsnCodeHooks } from '../../hooks/useMasters'
import { useProperty } from '../../property/PropertyContext'
import type { HsnCode } from '../../api/masters'

export function HsnCodesPage() {
  const { activeProperty } = useProperty()
  const propertyId = activeProperty!.id
  const { data: rows = [], isLoading } = hsnCodeHooks.useListByProperty(propertyId)
  const createMutation = hsnCodeHooks.useCreateInProperty(propertyId)
  const updateMutation = hsnCodeHooks.useUpdate(propertyId)
  const deleteMutation = hsnCodeHooks.useDelete(propertyId)
  const confirmDialog = useConfirm()

  const [editingId, setEditingId] = useState<number | null>(null)
  const [code, setCode] = useState('')
  const [description, setDescription] = useState('')
  const [active, setActive] = useState(true)

  function resetForm() {
    setEditingId(null)
    setCode('')
    setDescription('')
    setActive(true)
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const request = { code, description: description || null, active }
    if (editingId === null) {
      await createMutation.mutateAsync(request)
    } else {
      await updateMutation.mutateAsync({ id: editingId, request })
    }
    resetForm()
  }

  function handleEdit(row: HsnCode) {
    setEditingId(row.id)
    setCode(row.code)
    setDescription(row.description ?? '')
    setActive(row.active)
  }

  async function handleDelete(row: HsnCode) {
    const ok = await confirmDialog({ message: `Delete HSN code "${row.code}"?`, confirmLabel: 'Delete', danger: true })
    if (ok) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1>HSN Codes</h1>
      </div>

      <form onSubmit={handleSubmit} className="panel">
        <div className="form-row">
          <Input
            value={code}
            onChange={(e) => setCode(e.target.value)}
            placeholder="HSN code (e.g. 2202)"
            required
            pattern="^([0-9]{4}|[0-9]{6}|[0-9]{8})$"
            title="4, 6, or 8-digit HSN code"
            style={{ width: 140 }}
          />
          <Input
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Description"
            style={{ flex: 1, minWidth: 200 }}
          />
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
            { header: 'Code', render: (row) => row.code },
            { header: 'Description', render: (row) => row.description ?? '-' },
            { header: 'Active', render: (row) => (row.active ? 'Yes' : 'No') },
          ]}
        />
      )}
    </div>
  )
}
