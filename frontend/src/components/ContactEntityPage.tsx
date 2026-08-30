import { useState, type FormEvent } from 'react'
import { DataTable } from './DataTable'

interface ContactEntity {
  id: number
  name: string
  phone: string | null
  email: string | null
  gstin: string | null
  address: string | null
  active: boolean
}

interface ContactEntityRequest {
  name: string
  phone: string | null
  email: string | null
  gstin: string | null
  address: string | null
  active: boolean
}

interface ContactEntityPageProps<T extends ContactEntity> {
  title: string
  hooks: {
    useList: () => { data?: T[]; isLoading: boolean }
    useCreate: () => { mutateAsync: (request: ContactEntityRequest) => Promise<T> }
    useUpdate: () => { mutateAsync: (args: { id: number; request: ContactEntityRequest }) => Promise<T> }
    useDelete: () => { mutateAsync: (id: number) => Promise<void> }
  }
}

const emptyForm = { name: '', phone: '', email: '', gstin: '', address: '' }

export function ContactEntityPage<T extends ContactEntity>({ title, hooks }: ContactEntityPageProps<T>) {
  const { data: rows = [], isLoading } = hooks.useList()
  const createMutation = hooks.useCreate()
  const updateMutation = hooks.useUpdate()
  const deleteMutation = hooks.useDelete()

  const [editingId, setEditingId] = useState<number | null>(null)
  const [form, setForm] = useState(emptyForm)
  const [active, setActive] = useState(true)

  function resetForm() {
    setEditingId(null)
    setForm(emptyForm)
    setActive(true)
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const request: ContactEntityRequest = {
      name: form.name,
      phone: form.phone || null,
      email: form.email || null,
      gstin: form.gstin || null,
      address: form.address || null,
      active,
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
  }

  async function handleDelete(row: T) {
    if (confirm(`Delete "${row.name}"?`)) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div>
      <h2>{title}</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 16, flexWrap: 'wrap' }}>
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
            { header: 'Name', render: (row) => row.name },
            { header: 'Phone', render: (row) => row.phone ?? '-' },
            { header: 'Email', render: (row) => row.email ?? '-' },
            { header: 'GSTIN', render: (row) => row.gstin ?? '-' },
            { header: 'Active', render: (row) => (row.active ? 'Yes' : 'No') },
          ]}
        />
      )}
    </div>
  )
}
