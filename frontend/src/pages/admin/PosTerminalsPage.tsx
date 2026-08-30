import { useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { DataTable } from '../../components/DataTable'
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
    <div style={{ maxWidth: 700, margin: '24px auto', textAlign: 'left', padding: '0 16px' }}>
      <p>
        <Link to="/admin/properties">&larr; Back to properties</Link>
      </p>
      <h1 style={{ fontSize: 24 }}>POS Terminals — Property #{id}</h1>

      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 16 }}>
        <input value={posName} onChange={(e) => setPosName(e.target.value)} placeholder="POS name" required style={{ padding: 6 }} />
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
