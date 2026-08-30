import { useEffect, useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { DataTable } from '../../components/DataTable'
import { Button, Checkbox, FieldLabel, Input, Select } from '../../components/ui'
import { useClients } from '../../hooks/useClients'
import { useCreateProperty, useDeleteProperty, usePropertiesByClient, useUpdateProperty } from '../../hooks/useProperties'
import type { Property } from '../../api/properties'

const emptyForm = {
  propertyName: '',
  address: '',
  gstin: '',
  invoiceSeriesPrefix: '',
  defaultCgstRate: '0',
  defaultSgstRate: '0',
  defaultIgstRate: '0',
}

export function PropertiesPage() {
  const { data: clients = [] } = useClients()
  const [searchParams, setSearchParams] = useSearchParams()
  const clientIdParam = searchParams.get('clientId')
  const [clientId, setClientId] = useState<number | null>(clientIdParam ? Number(clientIdParam) : null)

  useEffect(() => {
    if (clientId === null && clients.length > 0) {
      setClientId(clients[0].id)
    }
  }, [clients, clientId])

  const { data: properties = [], isLoading } = usePropertiesByClient(clientId)
  const createMutation = useCreateProperty()
  const updateMutation = useUpdateProperty()
  const deleteMutation = useDeleteProperty()

  const [editingId, setEditingId] = useState<number | null>(null)
  const [form, setForm] = useState(emptyForm)
  const [eInvoiceEnabled, setEInvoiceEnabled] = useState(false)
  const [active, setActive] = useState(true)

  function resetForm() {
    setEditingId(null)
    setForm(emptyForm)
    setEInvoiceEnabled(false)
    setActive(true)
  }

  function handleClientChange(id: number) {
    setClientId(id)
    setSearchParams({ clientId: String(id) })
    resetForm()
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (clientId === null) return
    const request = {
      propertyName: form.propertyName,
      address: form.address || null,
      gstin: form.gstin || null,
      invoiceSeriesPrefix: form.invoiceSeriesPrefix,
      defaultCgstRate: Number(form.defaultCgstRate),
      defaultSgstRate: Number(form.defaultSgstRate),
      defaultIgstRate: Number(form.defaultIgstRate),
      eInvoiceEnabled,
      active,
    }
    if (editingId === null) {
      await createMutation.mutateAsync({ clientId, request })
    } else {
      await updateMutation.mutateAsync({ id: editingId, request })
    }
    resetForm()
  }

  function handleEdit(row: Property) {
    setEditingId(row.id)
    setForm({
      propertyName: row.propertyName,
      address: row.address ?? '',
      gstin: row.gstin ?? '',
      invoiceSeriesPrefix: row.invoiceSeriesPrefix,
      defaultCgstRate: String(row.defaultCgstRate),
      defaultSgstRate: String(row.defaultSgstRate),
      defaultIgstRate: String(row.defaultIgstRate),
    })
    setEInvoiceEnabled(row.eInvoiceEnabled)
    setActive(row.active)
  }

  async function handleDelete(row: Property) {
    if (confirm(`Delete "${row.propertyName}"?`)) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Properties</h1>
      </div>

      <div style={{ marginBottom: 16 }}>
        <FieldLabel>Client</FieldLabel>
        <Select value={clientId ?? ''} onChange={(e) => handleClientChange(Number(e.target.value))}>
          {clients.map((c) => (
            <option key={c.id} value={c.id}>
              {c.clientName}
            </option>
          ))}
        </Select>
      </div>

      <form onSubmit={handleSubmit} className="panel">
        <div className="form-row">
          <Input value={form.propertyName} onChange={(e) => setForm({ ...form, propertyName: e.target.value })} placeholder="Property name" required />
          <Input value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} placeholder="Address" />
          <Input value={form.gstin} onChange={(e) => setForm({ ...form, gstin: e.target.value })} placeholder="GSTIN" style={{ width: 140 }} />
          <Input
            value={form.invoiceSeriesPrefix}
            onChange={(e) => setForm({ ...form, invoiceSeriesPrefix: e.target.value })}
            placeholder="Invoice prefix"
            required
            style={{ width: 110 }}
          />
          <Input
            type="number"
            step="0.01"
            value={form.defaultCgstRate}
            onChange={(e) => setForm({ ...form, defaultCgstRate: e.target.value })}
            placeholder="CGST %"
            style={{ width: 84 }}
          />
          <Input
            type="number"
            step="0.01"
            value={form.defaultSgstRate}
            onChange={(e) => setForm({ ...form, defaultSgstRate: e.target.value })}
            placeholder="SGST %"
            style={{ width: 84 }}
          />
          <Input
            type="number"
            step="0.01"
            value={form.defaultIgstRate}
            onChange={(e) => setForm({ ...form, defaultIgstRate: e.target.value })}
            placeholder="IGST %"
            style={{ width: 84 }}
          />
          <Checkbox label="E-invoice" checked={eInvoiceEnabled} onChange={(e) => setEInvoiceEnabled(e.target.checked)} />
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
          rows={properties}
          rowKey={(row) => row.id}
          onEdit={handleEdit}
          onDelete={handleDelete}
          columns={[
            {
              header: 'Property',
              render: (row) => <Link to={`/admin/properties/${row.id}/pos`}>{row.propertyName}</Link>,
            },
            { header: 'GSTIN', render: (row) => row.gstin ?? '-' },
            { header: 'Invoice prefix', render: (row) => row.invoiceSeriesPrefix },
            { header: 'E-invoice', render: (row) => (row.eInvoiceEnabled ? 'Yes' : 'No') },
            { header: 'Active', render: (row) => (row.active ? 'Yes' : 'No') },
          ]}
        />
      )}
    </div>
  )
}
