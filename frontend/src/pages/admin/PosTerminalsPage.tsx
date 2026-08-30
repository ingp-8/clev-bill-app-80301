import { useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { DataTable } from '../../components/DataTable'
import { Button, Checkbox, Input } from '../../components/ui'
import {
  useCreatePosTerminal,
  useDeletePosTerminal,
  usePosTerminalsByProperty,
  useUpdatePosTerminal,
} from '../../hooks/usePosTerminals'
import type { PosTerminal } from '../../api/posTerminals'

export function PosTerminalsPage() {
  const { propertyId } = useParams<{ propertyId: string }>()
  const id = Number(propertyId)

  const { data: terminals = [], isLoading } = usePosTerminalsByProperty(id)
  const createMutation = useCreatePosTerminal(id)
  const updateMutation = useUpdatePosTerminal(id)
  const deleteMutation = useDeletePosTerminal(id)

  const [editingId, setEditingId] = useState<number | null>(null)
  const [posName, setPosName] = useState('')
  const [active, setActive] = useState(true)

  function resetForm() {
    setEditingId(null)
    setPosName('')
    setActive(true)
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const request = { posName, active }
    if (editingId === null) {
      await createMutation.mutateAsync(request)
    } else {
      await updateMutation.mutateAsync({ id: editingId, request })
    }
    resetForm()
  }

  function handleEdit(row: PosTerminal) {
    setEditingId(row.id)
    setPosName(row.posName)
    setActive(row.active)
  }

  async function handleDelete(row: PosTerminal) {
    if (confirm(`Delete "${row.posName}"?`)) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div className="page page-narrow">
      <Link to="/admin/properties" className="page-back">
        &larr; Back to properties
      </Link>
      <div className="page-header">
        <h1>POS Terminals — Property #{id}</h1>
      </div>

      <form onSubmit={handleSubmit} className="panel">
        <div className="form-row">
          <Input value={posName} onChange={(e) => setPosName(e.target.value)} placeholder="POS name" required />
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
          rows={terminals}
          rowKey={(row) => row.id}
          onEdit={handleEdit}
          onDelete={handleDelete}
          columns={[
            { header: 'POS Name', render: (row) => row.posName },
            { header: 'Active', render: (row) => (row.active ? 'Yes' : 'No') },
          ]}
        />
      )}
    </div>
  )
}
