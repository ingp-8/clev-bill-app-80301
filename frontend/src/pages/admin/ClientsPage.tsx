import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { DataTable } from '../../components/DataTable'
import { Button, Checkbox, Input } from '../../components/ui'
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
    <div className="page">
      <div className="page-header">
        <h1>Clients</h1>
      </div>

      <form onSubmit={handleSubmit} className="panel">
        <div className="form-row">
          <Input value={form.clientName} onChange={(e) => setForm({ ...form, clientName: e.target.value })} placeholder="Client name" required />
          <Input value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} placeholder="Address" />
          <Input value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="Email" />
          <Input value={form.mobileNo} onChange={(e) => setForm({ ...form, mobileNo: e.target.value })} placeholder="Mobile no." />
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
