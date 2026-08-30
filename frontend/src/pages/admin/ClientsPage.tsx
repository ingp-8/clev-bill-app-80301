import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { DataTable } from '../../components/DataTable'
import { useClients, useCreateClient, useDeleteClient, useUpdateClient } from '../../hooks/useClients'
import type { Client } from '../../api/clients'

const emptyForm = { clientName: '', address: '', email: '', mobileNo: '' }

export function ClientsPage() {
  const { data: clients = [], isLoading } = useClients()
  const createMutation = useCreateClient()
  const updateMutation = useUpdateClient()
  const deleteMutation = useDeleteClient()

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
    const request = {
      clientName: form.clientName,
      address: form.address || null,
      email: form.email || null,
      mobileNo: form.mobileNo || null,
      logoPath: null,
      active,
    }
    if (editingId === null) {
      await createMutation.mutateAsync(request)
    } else {
      await updateMutation.mutateAsync({ id: editingId, request })
    }
    resetForm()
  }

  function handleEdit(row: Client) {
    setEditingId(row.id)
    setForm({
      clientName: row.clientName,
      address: row.address ?? '',
      email: row.email ?? '',
      mobileNo: row.mobileNo ?? '',
    })
    setActive(row.active)
  }

  async function handleDelete(row: Client) {
    if (confirm(`Delete "${row.clientName}"?`)) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div style={{ maxWidth: 900, margin: '24px auto', textAlign: 'left', padding: '0 16px' }}>
      <h1 style={{ fontSize: 24 }}>Clients</h1>
      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 16, flexWrap: 'wrap' }}>
        <input
          value={form.clientName}
          onChange={(e) => setForm({ ...form, clientName: e.target.value })}
          placeholder="Client name"
          required
          style={{ padding: 6 }}
        />
        <input
          value={form.address}
          onChange={(e) => setForm({ ...form, address: e.target.value })}
          placeholder="Address"
          style={{ padding: 6 }}
        />
        <input
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          placeholder="Email"
          style={{ padding: 6 }}
        />
        <input
          value={form.mobileNo}
          onChange={(e) => setForm({ ...form, mobileNo: e.target.value })}
          placeholder="Mobile no."
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
          rows={clients}
          rowKey={(row) => row.id}
          onEdit={handleEdit}
          onDelete={handleDelete}
          columns={[
            {
              header: 'Client',
              render: (row) => <Link to={`/admin/properties?clientId=${row.id}`}>{row.clientName}</Link>,
            },
            { header: 'Email', render: (row) => row.email ?? '-' },
            { header: 'Mobile', render: (row) => row.mobileNo ?? '-' },
            { header: 'Active', render: (row) => (row.active ? 'Yes' : 'No') },
          ]}
        />
      )}
    </div>
  )
}
