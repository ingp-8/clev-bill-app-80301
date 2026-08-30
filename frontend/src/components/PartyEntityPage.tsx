import { useState, type FormEvent } from 'react'
import { DataTable } from './DataTable'
import { useProperty } from '../property/PropertyContext'

interface PartyEntity {
  id: number
  name: string
  phone: string | null
  email: string | null
  gstin: string | null
  address: string | null
  active: boolean
  propertyIds: number[]
}

interface PartyEntityRequest {
  name: string
  phone: string | null
  email: string | null
  gstin: string | null
  address: string | null
  active: boolean
  propertyIds: number[]
}

interface PartyEntityPageProps<T extends PartyEntity> {
  title: string
  hooks: {
    useList: () => { data?: T[]; isLoading: boolean }
    useCreate: () => { mutateAsync: (request: PartyEntityRequest) => Promise<T> }
    useUpdate: () => { mutateAsync: (args: { id: number; request: PartyEntityRequest }) => Promise<T> }
    useDelete: () => { mutateAsync: (id: number) => Promise<void> }
  }
}

const emptyForm = { name: '', phone: '', email: '', gstin: '', address: '' }

export function PartyEntityPage<T extends PartyEntity>({ title, hooks }: PartyEntityPageProps<T>) {
  const { properties } = useProperty()
  const { data: rows = [], isLoading } = hooks.useList()
  const createMutation = hooks.useCreate()
  const updateMutation = hooks.useUpdate()
  const deleteMutation = hooks.useDelete()

  const [editingId, setEditingId] = useState<number | null>(null)
  const [form, setForm] = useState(emptyForm)
  const [active, setActive] = useState(true)
  const [propertyIds, setPropertyIds] = useState<Set<number>>(new Set())

  function resetForm() {
    setEditingId(null)
    setForm(emptyForm)
    setActive(true)
    setPropertyIds(new Set())
  }

  function toggleProperty(id: number) {
    setPropertyIds((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const request: PartyEntityRequest = {
      name: form.name,
      phone: form.phone || null,
      email: form.email || null,
      gstin: form.gstin || null,
      address: form.address || null,
      active,
      propertyIds: Array.from(propertyIds),
    }
    if (editingId === null) {
      await createMutation.mutateAsync(request)
    } else {
      await updateMutation.mutateAsync({ id: editingId, request })
    }
    resetForm()
  }

  function handleEdit(row: T) {
    setEditingId(row.id)
    setForm({
      name: row.name,
      phone: row.phone ?? '',
      email: row.email ?? '',
      gstin: row.gstin ?? '',
      address: row.address ?? '',
    })
    setActive(row.active)
    setPropertyIds(new Set(row.propertyIds))
  }

  async function handleDelete(row: T) {
    if (confirm(`Delete "${row.name}"?`)) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div>
      <h2>{title}</h2>
      <form onSubmit={handleSubmit} style={{ marginBottom: 16, border: '1px solid var(--border)', padding: 16, borderRadius: 6 }}>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 12, flexWrap: 'wrap' }}>
          <input
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            placeholder="Name"
            required
            style={{ padding: 6 }}
          />
          <input
            value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })}
            placeholder="Phone"
            style={{ padding: 6 }}
          />
          <input
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            placeholder="Email"
            style={{ padding: 6 }}
          />
          <input
            value={form.gstin}
            onChange={(e) => setForm({ ...form, gstin: e.target.value })}
            placeholder="GSTIN"
            style={{ padding: 6 }}
          />
          <input
            value={form.address}
            onChange={(e) => setForm({ ...form, address: e.target.value })}
            placeholder="Address"
            style={{ padding: 6 }}
          />
          <label>
            <input type="checkbox" checked={active} onChange={(e) => setActive(e.target.checked)} /> Active
          </label>
        </div>

        <div style={{ marginBottom: 12 }}>
          <div style={{ fontWeight: 600, fontSize: 13, marginBottom: 4 }}>Serves properties</div>
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
            {properties.map((property) => (
              <label key={property.id} style={{ fontSize: 13 }}>
                <input
                  type="checkbox"
                  checked={propertyIds.has(property.id)}
                  onChange={() => toggleProperty(property.id)}
                />{' '}
                {property.propertyName}
              </label>
            ))}
          </div>
        </div>

        <button type="submit">{editingId === null ? 'Add' : 'Save'}</button>{' '}
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
            { header: 'Name', render: (row) => row.name },
            { header: 'Phone', render: (row) => row.phone ?? '-' },
            { header: 'Email', render: (row) => row.email ?? '-' },
            { header: 'GSTIN', render: (row) => row.gstin ?? '-' },
            { header: 'Properties', render: (row) => row.propertyIds.length },
            { header: 'Active', render: (row) => (row.active ? 'Yes' : 'No') },
          ]}
        />
      )}
    </div>
  )
}
