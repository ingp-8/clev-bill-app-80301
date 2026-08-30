import { useState } from 'react'
import { DataTable } from '../../components/DataTable'
import { FieldLabel, Select } from '../../components/ui'
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
    <div className="page">
      <div className="page-header">
        <h1>Reports</h1>
      </div>

      <div className="panel">
        <div className="form-row">
          <div>
            <FieldLabel>From</FieldLabel>
            <input type="date" value={fromDate} onChange={(e) => setFromDate(e.target.value)} className="field-input" />
          </div>
          <div>
            <FieldLabel>To</FieldLabel>
            <input type="date" value={toDate} onChange={(e) => setToDate(e.target.value)} className="field-input" />
          </div>
          <div>
            <FieldLabel>Category</FieldLabel>
            <Select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
              <option value="">All</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </Select>
          </div>
        </div>
      </div>

      {summary && (
        <div className="stat-row">
          <div className="stat">
            <div className="k">Sales</div>
            <div className="v">{summary.saleCount}</div>
          </div>
          <div className="stat alt">
            <div className="k">Revenue</div>
            <div className="v">₹{summary.totalRevenue.toFixed(2)}</div>
          </div>
          <div className="stat">
            <div className="k">Tax collected</div>
            <div className="v">₹{summary.totalTax.toFixed(2)}</div>
          </div>
          <div className="stat alt">
            <div className="k">Avg sale value</div>
            <div className="v">₹{summary.averageSaleValue.toFixed(2)}</div>
          </div>
        </div>
      )}

      <div className="panel-head" style={{ marginTop: 4 }}>
        <h2>Sales by Item</h2>
      </div>
      <DataTable
        rows={byItem}
        rowKey={(row) => row.itemId}
        emptyMessage="No sales in this range"
        columns={[
          { header: 'SKU', render: (row) => row.sku },
          { header: 'Item', render: (row) => row.name },
          { header: 'Qty sold', render: (row) => row.quantitySold },
          { header: 'Revenue', render: (row) => `₹${row.revenue.toFixed(2)}` },
        ]}
      />

      <div className="panel-head" style={{ marginTop: 24 }}>
        <h2>Sales by Day</h2>
      </div>
      <DataTable
        rows={byDay}
        rowKey={(row) => row.date}
        emptyMessage="No sales in this range"
        columns={[
          { header: 'Date', render: (row) => row.date },
          { header: 'Sales', render: (row) => row.saleCount },
          { header: 'Revenue', render: (row) => `₹${row.revenue.toFixed(2)}` },
        ]}
      />
    </div>
  )
}
