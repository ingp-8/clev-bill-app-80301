import { useState, type FormEvent } from 'react'
import { DataTable } from '../../components/DataTable'
import { brandHooks, categoryHooks, itemHooks, taxRateHooks } from '../../hooks/useMasters'
import type { Item, ItemRequest, ItemUnit } from '../../api/masters'

const UNITS: ItemUnit[] = ['PCS', 'BOX', 'KG', 'GM', 'LTR', 'ML']

const emptyForm = {
  sku: '',
  barcode: '',
  name: '',
  categoryId: '',
  brandId: '',
  taxRateId: '',
  hsnCode: '',
  unit: 'PCS' as ItemUnit,
  sellingPrice: '',
  costPrice: '',
}

export function ItemsPage() {
  const { data: items = [], isLoading } = itemHooks.useList()
  const { data: categories = [] } = categoryHooks.useList()
  const { data: brands = [] } = brandHooks.useList()
  const { data: taxRates = [] } = taxRateHooks.useList()
  const createMutation = itemHooks.useCreate()
  const updateMutation = itemHooks.useUpdate()
  const deleteMutation = itemHooks.useDelete()

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
    if (!form.taxRateId) return

    const request: ItemRequest = {
      sku: form.sku,
      barcode: form.barcode || null,
      name: form.name,
      categoryId: form.categoryId ? Number(form.categoryId) : null,
      brandId: form.brandId ? Number(form.brandId) : null,
      taxRateId: Number(form.taxRateId),
      hsnCode: form.hsnCode,
      unit: form.unit,
      sellingPrice: Number(form.sellingPrice),
      costPrice: form.costPrice ? Number(form.costPrice) : null,
      active,
    }

    if (editingId === null) {
      await createMutation.mutateAsync(request)
    } else {
      await updateMutation.mutateAsync({ id: editingId, request })
    }
    resetForm()
  }

  function handleEdit(row: Item) {
    setEditingId(row.id)
    setForm({
      sku: row.sku,
      barcode: row.barcode ?? '',
      name: row.name,
      categoryId: row.category ? String(row.category.id) : '',
      brandId: row.brand ? String(row.brand.id) : '',
      taxRateId: String(row.taxRate.id),
      hsnCode: row.hsnCode ?? '',
      unit: row.unit,
      sellingPrice: String(row.sellingPrice),
      costPrice: row.costPrice !== null ? String(row.costPrice) : '',
    })
    setActive(row.active)
  }

  async function handleDelete(row: Item) {
    if (confirm(`Delete "${row.name}"?`)) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div>
      <h2>Items</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 16, flexWrap: 'wrap' }}>
        <input value={form.sku} onChange={(e) => setForm({ ...form, sku: e.target.value })} placeholder="SKU" required style={{ padding: 6, width: 100 }} />
        <input
          value={form.barcode}
          onChange={(e) => setForm({ ...form, barcode: e.target.value })}
          placeholder="Barcode"
          style={{ padding: 6, width: 120 }}
        />
        <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Name" required style={{ padding: 6 }} />
        <select value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })} style={{ padding: 6 }}>
          <option value="">No category</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
        <select value={form.brandId} onChange={(e) => setForm({ ...form, brandId: e.target.value })} style={{ padding: 6 }}>
          <option value="">No brand</option>
          {brands.map((b) => (
            <option key={b.id} value={b.id}>
              {b.name}
            </option>
          ))}
        </select>
        <select
          value={form.taxRateId}
          onChange={(e) => setForm({ ...form, taxRateId: e.target.value })}
          required
          style={{ padding: 6 }}
        >
          <option value="">Tax rate...</option>
          {taxRates.map((t) => (
            <option key={t.id} value={t.id}>
              {t.name}
            </option>
          ))}
        </select>
        <input
          value={form.hsnCode}
          onChange={(e) => setForm({ ...form, hsnCode: e.target.value })}
          placeholder="HSN code"
          required
          pattern="^([0-9]{4}|[0-9]{6}|[0-9]{8})$"
          title="4, 6, or 8-digit HSN code"
          style={{ padding: 6, width: 90 }}
        />
        <select value={form.unit} onChange={(e) => setForm({ ...form, unit: e.target.value as ItemUnit })} style={{ padding: 6 }}>
          {UNITS.map((u) => (
            <option key={u} value={u}>
              {u}
            </option>
          ))}
        </select>
        <input
          type="number"
          step="0.01"
          value={form.sellingPrice}
          onChange={(e) => setForm({ ...form, sellingPrice: e.target.value })}
          placeholder="Selling price"
          required
          style={{ padding: 6, width: 110 }}
        />
        <input
          type="number"
          step="0.01"
          value={form.costPrice}
          onChange={(e) => setForm({ ...form, costPrice: e.target.value })}
          placeholder="Cost price"
          style={{ padding: 6, width: 110 }}
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
          rows={items}
          rowKey={(row) => row.id}
          onEdit={handleEdit}
          onDelete={handleDelete}
          columns={[
            { header: 'SKU', render: (row) => row.sku },
            { header: 'Name', render: (row) => row.name },
            { header: 'Category', render: (row) => row.category?.name ?? '-' },
            { header: 'Brand', render: (row) => row.brand?.name ?? '-' },
            { header: 'HSN', render: (row) => row.hsnCode },
            { header: 'Tax', render: (row) => row.taxRate.name },
            { header: 'Unit', render: (row) => row.unit },
            { header: 'Price', render: (row) => row.sellingPrice.toFixed(2) },
            { header: 'Active', render: (row) => (row.active ? 'Yes' : 'No') },
          ]}
        />
      )}
    </div>
  )
}
