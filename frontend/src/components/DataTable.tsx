export interface DataTableColumn<T> {
  header: string
  render: (row: T) => React.ReactNode
}

interface DataTableProps<T> {
  columns: DataTableColumn<T>[]
  rows: T[]
  rowKey: (row: T) => string | number
  onEdit?: (row: T) => void
  onDelete?: (row: T) => void
  emptyMessage?: string
}

export function DataTable<T>({ columns, rows, rowKey, onEdit, onDelete, emptyMessage }: DataTableProps<T>) {
  const showActions = Boolean(onEdit || onDelete)

  return (
    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
      <thead>
        <tr>
          {columns.map((col) => (
            <th key={col.header} style={{ borderBottom: '1px solid var(--border)', padding: '8px 6px' }}>
              {col.header}
            </th>
          ))}
          {showActions && <th style={{ borderBottom: '1px solid var(--border)', padding: '8px 6px' }}>Actions</th>}
        </tr>
      </thead>
      <tbody>
        {rows.length === 0 && (
          <tr>
            <td colSpan={columns.length + (showActions ? 1 : 0)} style={{ padding: '12px 6px', color: 'var(--text)' }}>
              {emptyMessage ?? 'No records yet'}
            </td>
          </tr>
        )}
        {rows.map((row) => (
          <tr key={rowKey(row)}>
            {columns.map((col) => (
              <td key={col.header} style={{ borderBottom: '1px solid var(--border)', padding: '8px 6px' }}>
                {col.render(row)}
              </td>
            ))}
            {showActions && (
              <td style={{ borderBottom: '1px solid var(--border)', padding: '8px 6px', whiteSpace: 'nowrap' }}>
                {onEdit && (
                  <button type="button" onClick={() => onEdit(row)} style={{ marginRight: 8 }}>
                    Edit
                  </button>
                )}
                {onDelete && (
                  <button type="button" onClick={() => onDelete(row)}>
                    Delete
                  </button>
                )}
              </td>
            )}
          </tr>
        ))}
      </tbody>
    </table>
  )
}
