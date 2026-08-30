import { Link, useParams } from 'react-router-dom'
import { DataTable } from '../../components/DataTable'
import { useInventoryTransactions } from '../../hooks/useInventory'

export function InventoryHistoryPage() {
  const { itemId } = useParams<{ itemId: string }>()
  const id = Number(itemId)
  const { data: transactions = [], isLoading } = useInventoryTransactions(id)

  return (
    <div className="page">
      <Link to="/inventory" className="page-back">
        &larr; Back to inventory
      </Link>
      <div className="page-header">
        <h1>Stock History — Item #{id}</h1>
      </div>

      {isLoading ? (
        <p>Loading...</p>
      ) : (
        <DataTable
          rows={transactions}
          rowKey={(row) => row.id}
          columns={[
            { header: 'Date', render: (row) => new Date(row.createdAt).toLocaleString() },
            { header: 'Change', render: (row) => (row.changeQuantity > 0 ? `+${row.changeQuantity}` : row.changeQuantity) },
            { header: 'Reason', render: (row) => row.reason },
            { header: 'Reference', render: (row) => row.referenceId ?? '-' },
            { header: 'Note', render: (row) => row.note ?? '-' },
            { header: 'By', render: (row) => row.createdByName ?? '-' },
          ]}
        />
      )}
    </div>
  )
}
