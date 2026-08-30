import { useState } from 'react'
import { Link } from 'react-router-dom'
import { DataTable } from '../../components/DataTable'
import { categoryHooks } from '../../hooks/useMasters'
import { useSalesByDay, useSalesByItem, useSalesSummary } from '../../hooks/useReports'
import { useProperty } from '../../property/PropertyContext'

function toIsoOrUndefined(dateStr: string): string | undefined {
  return dateStr ? new Date(dateStr).toISOString() : undefined
}

export function ReportsPage() {
  const { activeProperty } = useProperty()
  const propertyId = activeProperty!.id
  const { data: categories = [] } = categoryHooks.useListByProperty(propertyId)
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')
  const [categoryId, setCategoryId] = useState('')

  const filters = {
    from: toIsoOrUndefined(fromDate),
    to: toIsoOrUndefined(toDate),
    categoryId: categoryId ? Number(categoryId) : undefined,
  }

  const { data: summary } = useSalesSummary(propertyId, filters)
  const { data: byItem = [] } = useSalesByItem(propertyId, filters)
  const { data: byDay = [] } = useSalesByDay(propertyId, filters)

  return (
    <div style={{ maxWidth: 900, margin: '24px auto', textAlign: 'left', padding: '0 16px' }}>
      <p>
        <Link to="/">&larr; Back</Link>
      </p>
      <h1 style={{ fontSize: 24 }}>Reports</h1>

      <div style={{ display: 'flex', gap: 8, marginBottom: 20, flexWrap: 'wrap' }}>
        <label>
          From{' '}
          <input type="date" value={fromDate} onChange={(e) => setFromDate(e.target.value)} style={{ padding: 6 }} />
        </label>
        <label>
          To <input type="date" value={toDate} onChange={(e) => setToDate(e.target.value)} style={{ padding: 6 }} />
        </label>
        <label>
          Category{' '}
          <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} style={{ padding: 6 }}>
            <option value="">All</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
      </div>

      {summary && (
        <div style={{ display: 'flex', gap: 16, marginBottom: 24, flexWrap: 'wrap' }}>
          <StatTile label="Sales" value={String(summary.saleCount)} />
          <StatTile label="Revenue" value={summary.totalRevenue.toFixed(2)} />
          <StatTile label="Tax collected" value={summary.totalTax.toFixed(2)} />
          <StatTile label="Avg sale value" value={summary.averageSaleValue.toFixed(2)} />
        </div>
      )}

      <h2 style={{ fontSize: 18 }}>Sales by Item</h2>
      <DataTable
        rows={byItem}
        rowKey={(row) => row.itemId}
        emptyMessage="No sales in this range"
        columns={[
          { header: 'SKU', render: (row) => row.sku },
          { header: 'Item', render: (row) => row.name },
          { header: 'Qty sold', render: (row) => row.quantitySold },
          { header: 'Revenue', render: (row) => row.revenue.toFixed(2) },
        ]}
      />

      <h2 style={{ fontSize: 18, marginTop: 24 }}>Sales by Day</h2>
      <DataTable
        rows={byDay}
        rowKey={(row) => row.date}
        emptyMessage="No sales in this range"
        columns={[
          { header: 'Date', render: (row) => row.date },
          { header: 'Sales', render: (row) => row.saleCount },
          { header: 'Revenue', render: (row) => row.revenue.toFixed(2) },
        ]}
      />
    </div>
  )
}

function StatTile({ label, value }: { label: string; value: string }) {
  return (
    <div style={{ border: '1px solid var(--border)', borderRadius: 6, padding: '12px 16px', minWidth: 120 }}>
      <div style={{ fontSize: 12, color: 'var(--text)' }}>{label}</div>
      <div style={{ fontSize: 22, fontWeight: 700 }}>{value}</div>
    </div>
  )
}
