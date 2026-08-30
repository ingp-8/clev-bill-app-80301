import { useState, type FormEvent } from 'react'
import { DataTable } from '../../components/DataTable'
import { useConfirm } from '../../components/ConfirmDialogContext'
import { Button, Checkbox, Input, Select } from '../../components/ui'
import { brandHooks, categoryHooks, hsnCodeHooks, itemHooks, taxRateHooks } from '../../hooks/useMasters'
import { useProperty } from '../../property/PropertyContext'
import type { Item, ItemRequest, ItemUnit } from '../../api/masters'

const UNITS: ItemUnit[] = ['PCS', 'BOX', 'KG', 'GM', 'LTR', 'ML']

const emptyForm = {
  sku: '',
  barcode: '',
  name: '',
  categoryId: '',
  brandId: '',
  taxRateId: '',
  hsnCodeId: '',
  unit: 'PCS' as ItemUnit,
  sellingPrice: '',
  costPrice: '',
}

export function ItemsPage() {
  const { activeProperty } = useProperty()
  const propertyId = activeProperty!.id
  const { data: items = [], isLoading } = itemHooks.useListByProperty(propertyId)
  const { data: categories = [] } = categoryHooks.useListByProperty(propertyId)
  const { data: brands = [] } = brandHooks.useListByProperty(propertyId)
  const { data: taxRates = [] } = taxRateHooks.useListByProperty(propertyId)
  const { data: hsnCodes = [] } = hsnCodeHooks.useListByProperty(propertyId)
  const createMutation = itemHooks.useCreateInProperty(propertyId)
  const updateMutation = itemHooks.useUpdate(propertyId)
  const deleteMutation = itemHooks.useDelete(propertyId)
  const confirmDialog = useConfirm()

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
    if (!form.taxRateId || !form.hsnCodeId) return

    const request: ItemRequest = {
      sku: form.sku,
      barcode: form.barcode || null,
      name: form.name,
      categoryId: form.categoryId ? Number(form.categoryId) : null,
      brandId: form.brandId ? Number(form.brandId) : null,
      taxRateId: Number(form.taxRateId),
      hsnCodeId: Number(form.hsnCodeId),
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
      hsnCodeId: String(row.hsnCode.id),
      unit: row.unit,
      sellingPrice: String(row.sellingPrice),
      costPrice: row.costPrice !== null ? String(row.costPrice) : '',
    })
    setActive(row.active)
  }

  async function handleDelete(row: Item) {
    const ok = await confirmDialog({ message: `Delete "${row.name}"?`, confirmLabel: 'Delete', danger: true })
    if (ok) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1>Items</h1>
      </div>

      <form onSubmit={handleSubmit} className="panel">
        <div className="form-row">
          <Input value={form.sku} onChange={(e) => setForm({ ...form, sku: e.target.value })} placeholder="SKU" required style={{ width: 100 }} />
          <Input
            value={form.barcode}
            onChange={(e) => setForm({ ...form, barcode: e.target.value })}
            placeholder="Barcode"
            style={{ width: 120 }}
          />
          <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Name" required />
          <Select value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
            <option value="">No category</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </Select>
          <Select value={form.brandId} onChange={(e) => setForm({ ...form, brandId: e.target.value })}>
            <option value="">No brand</option>
            {brands.map((b) => (
              <option key={b.id} value={b.id}>
                {b.name}
              </option>
            ))}
          </Select>
          <Select value={form.taxRateId} onChange={(e) => setForm({ ...form, taxRateId: e.target.value })} required>
            <option value="">Tax rate...</option>
            {taxRates.map((t) => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </Select>
          <Select value={form.hsnCodeId} onChange={(e) => setForm({ ...form, hsnCodeId: e.target.value })} required>
            <option value="">HSN code...</option>
            {hsnCodes.map((h) => (
              <option key={h.id} value={h.id}>
                {h.code}
                {h.description ? ` — ${h.description}` : ''}
              </option>
            ))}
          </Select>
          <Select value={form.unit} onChange={(e) => setForm({ ...form, unit: e.target.value as ItemUnit })}>
            {UNITS.map((u) => (
              <option key={u} value={u}>
                {u}
              </option>
            ))}
          </Select>
          <Input
            type="number"
            step="0.01"
            value={form.sellingPrice}
            onChange={(e) => setForm({ ...form, sellingPrice: e.target.value })}
            placeholder="Selling price"
            required
            style={{ width: 110 }}
          />
          <Input
            type="number"
            step="0.01"
            value={form.costPrice}
            onChange={(e) => setForm({ ...form, costPrice: e.target.value })}
            placeholder="Cost price"
            style={{ width: 110 }}
          />
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
          rows={items}
          rowKey={(row) => row.id}
          onEdit={handleEdit}
          onDelete={handleDelete}
          columns={[
            { header: 'SKU', render: (row) => row.sku },
            { header: 'Name', render: (row) => row.name },
            { header: 'Category', render: (row) => row.category?.name ?? '-' },
            { header: 'Brand', render: (row) => row.brand?.name ?? '-' },
            { header: 'HSN', render: (row) => row.hsnCode.code },
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
