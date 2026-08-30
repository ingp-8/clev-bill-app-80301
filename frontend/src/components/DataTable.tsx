import './DataTable.css'

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
    <div className="dt-wrap">
      <table className="dataTable">
        <thead>
          <tr>
            {columns.map((col) => (
              <th key={col.header}>{col.header}</th>
            ))}
            {showActions && <th>Actions</th>}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 && (
            <tr>
              <td colSpan={columns.length + (showActions ? 1 : 0)} className="dt-empty">
                {emptyMessage ?? 'No records yet'}
              </td>
            </tr>
          )}
          {rows.map((row) => (
            <tr key={rowKey(row)}>
              {columns.map((col) => (
                <td key={col.header} data-label={col.header}>
                  {col.render(row)}
                </td>
              ))}
              {showActions && (
                <td data-label="Actions">
                  <div className="dt-actions">
                    {onEdit && (
                      <button type="button" className="dt-btn" onClick={() => onEdit(row)}>
                        Edit
                      </button>
                    )}
                    {onDelete && (
                      <button type="button" className="dt-btn dt-btn-delete" onClick={() => onDelete(row)}>
                        Delete
                      </button>
                    )}
                  </div>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
