import { useEffect, useState, type FormEvent } from 'react'
import { useSearchParams } from 'react-router-dom'
import { DataTable } from '../../components/DataTable'
import { useConfirm } from '../../components/ConfirmDialogContext'
import { Button, Checkbox, FieldLabel, Input, Select } from '../../components/ui'
import {
  useCreatePosTerminal,
  useDeletePosTerminal,
  usePosTerminalsByProperty,
  useUpdatePosTerminal,
} from '../../hooks/usePosTerminals'
import { useProperty } from '../../property/PropertyContext'
import type { PosTerminal } from '../../api/posTerminals'

export function PosTerminalsPage() {
  const { properties } = useProperty()
  const [searchParams, setSearchParams] = useSearchParams()
  const propertyIdParam = searchParams.get('propertyId')
  const [propertyId, setPropertyId] = useState<number | null>(propertyIdParam ? Number(propertyIdParam) : null)

  useEffect(() => {
    if (propertyId === null && properties.length > 0) {
      setPropertyId(properties[0].id)
    }
  }, [properties, propertyId])

  const id = propertyId ?? Number.NaN
  const { data: terminals = [], isLoading } = usePosTerminalsByProperty(id)
  const createMutation = useCreatePosTerminal(id)
  const updateMutation = useUpdatePosTerminal(id)
  const deleteMutation = useDeletePosTerminal(id)
  const confirmDialog = useConfirm()

  const [editingId, setEditingId] = useState<number | null>(null)
  const [posName, setPosName] = useState('')
  const [active, setActive] = useState(true)

  function resetForm() {
    setEditingId(null)
    setPosName('')
    setActive(true)
  }

  function handlePropertyChange(newId: number) {
    setPropertyId(newId)
    setSearchParams({ propertyId: String(newId) })
    resetForm()
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
    const ok = await confirmDialog({ message: `Delete "${row.posName}"?`, confirmLabel: 'Delete', danger: true })
    if (ok) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div className="page page-narrow">
      <div className="page-header">
        <h1>POS Terminals</h1>
      </div>

      <div style={{ marginBottom: 16 }}>
        <FieldLabel>Property</FieldLabel>
        <Select value={propertyId ?? ''} onChange={(e) => handlePropertyChange(Number(e.target.value))}>
          {properties.map((p) => (
            <option key={p.id} value={p.id}>
              {p.propertyName}
            </option>
          ))}
        </Select>
      </div>

      <form onSubmit={handleSubmit} className="panel">
        <div className="form-row">
          <Input value={posName} onChange={(e) => setPosName(e.target.value)} placeholder="POS name" required />
          <Checkbox label="Active" checked={active} onChange={(e) => setActive(e.target.checked)} />
          <Button type="submit" disabled={propertyId === null}>
            {editingId === null ? 'Add' : 'Save'}
          </Button>
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
          emptyMessage="No POS terminals for this property yet"
          columns={[
            { header: 'POS Name', render: (row) => row.posName },
            { header: 'Active', render: (row) => (row.active ? 'Yes' : 'No') },
          ]}
        />
      )}
    </div>
  )
}
