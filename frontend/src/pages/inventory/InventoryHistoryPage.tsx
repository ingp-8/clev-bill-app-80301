import { Link, useParams } from 'react-router-dom'
import { DataTable } from '../../components/DataTable'
import { useInventoryTransactions } from '../../hooks/useInventory'

export function InventoryHistoryPage() {
  const { itemId } = useParams<{ itemId: string }>()
  const id = Number(itemId)
  const { data: transactions = [], isLoading } = useInventoryTransactions(id)

  return (
    <div style={{ maxWidth: 800, margin: '24px auto', textAlign: 'left', padding: '0 16px' }}>
      <p>
        <Link to="/inventory">&larr; Back to inventory</Link>
      </p>
      <h1 style={{ fontSize: 24 }}>Stock History — Item #{id}</h1>

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
