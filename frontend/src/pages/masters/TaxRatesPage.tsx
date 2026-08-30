import { useState, type FormEvent } from 'react'
import { DataTable } from '../../components/DataTable'
import { taxRateHooks } from '../../hooks/useMasters'
import type { TaxRate } from '../../api/masters'

export function TaxRatesPage() {
  const { data: rows = [], isLoading } = taxRateHooks.useList()
  const createMutation = taxRateHooks.useCreate()
  const updateMutation = taxRateHooks.useUpdate()
  const deleteMutation = taxRateHooks.useDelete()

  const [editingId, setEditingId] = useState<number | null>(null)
  const [name, setName] = useState('')
  const [cgstRate, setCgstRate] = useState('0')
  const [sgstRate, setSgstRate] = useState('0')
  const [igstRate, setIgstRate] = useState('0')
  const [active, setActive] = useState(true)

  function resetForm() {
    setEditingId(null)
    setName('')
    setCgstRate('0')
    setSgstRate('0')
    setIgstRate('0')
    setActive(true)
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const request = {
      name,
      cgstRate: Number(cgstRate),
      sgstRate: Number(sgstRate),
      igstRate: Number(igstRate),
      active,
    }
    if (editingId === null) {
      await createMutation.mutateAsync(request)
    } else {
      await updateMutation.mutateAsync({ id: editingId, request })
    }
    resetForm()
  }

  function handleEdit(row: TaxRate) {
    setEditingId(row.id)
    setName(row.name)
    setCgstRate(String(row.cgstRate))
    setSgstRate(String(row.sgstRate))
    setIgstRate(String(row.igstRate))
    setActive(row.active)
  }

  async function handleDelete(row: TaxRate) {
    if (confirm(`Delete "${row.name}"?`)) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div>
      <h2>Tax Rates</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 16, flexWrap: 'wrap' }}>
        <input value={name} onChange={(e) => setName(e.target.value)} placeholder="Name (e.g. GST 18%)" required style={{ padding: 6 }} />
        <input
          type="number"
          step="0.01"
          value={cgstRate}
          onChange={(e) => setCgstRate(e.target.value)}
          placeholder="CGST %"
          style={{ padding: 6, width: 90 }}
        />
        <input
          type="number"
          step="0.01"
          value={sgstRate}
          onChange={(e) => setSgstRate(e.target.value)}
          placeholder="SGST %"
          style={{ padding: 6, width: 90 }}
        />
        <input
          type="number"
          step="0.01"
          value={igstRate}
          onChange={(e) => setIgstRate(e.target.value)}
          placeholder="IGST %"
          style={{ padding: 6, width: 90 }}
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
            { header: 'CGST %', render: (row) => row.cgstRate },
            { header: 'SGST %', render: (row) => row.sgstRate },
            { header: 'IGST %', render: (row) => row.igstRate },
            { header: 'Active', render: (row) => (row.active ? 'Yes' : 'No') },
          ]}
        />
      )}
    </div>
  )
}
