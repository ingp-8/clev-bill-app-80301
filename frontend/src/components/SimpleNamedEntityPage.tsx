import { useState, type FormEvent } from 'react'
import { DataTable } from './DataTable'

interface NamedEntity {
  id: number
  name: string
  active: boolean
}

interface NamedEntityRequest {
  name: string
  active: boolean
}

interface SimpleNamedEntityPageProps<T extends NamedEntity> {
  title: string
  hooks: {
    useList: () => { data?: T[]; isLoading: boolean }
    useCreate: () => { mutateAsync: (request: NamedEntityRequest) => Promise<T> }
    useUpdate: () => { mutateAsync: (args: { id: number; request: NamedEntityRequest }) => Promise<T> }
    useDelete: () => { mutateAsync: (id: number) => Promise<void> }
  }
}

export function SimpleNamedEntityPage<T extends NamedEntity>({ title, hooks }: SimpleNamedEntityPageProps<T>) {
  const { data: rows = [], isLoading } = hooks.useList()
  const createMutation = hooks.useCreate()
  const updateMutation = hooks.useUpdate()
  const deleteMutation = hooks.useDelete()

  const [editingId, setEditingId] = useState<number | null>(null)
  const [name, setName] = useState('')
  const [active, setActive] = useState(true)

  function resetForm() {
    setEditingId(null)
    setName('')
    setActive(true)
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const request = { name, active }
    if (editingId === null) {
      await createMutation.mutateAsync(request)
    } else {
      await updateMutation.mutateAsync({ id: editingId, request })
    }
    resetForm()
  }

  function handleEdit(row: T) {
    setEditingId(row.id)
    setName(row.name)
    setActive(row.active)
  }

  async function handleDelete(row: T) {
    if (confirm(`Delete "${row.name}"?`)) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div>
      <h2>{title}</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 16 }}>
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Name"
          required
          style={{ padding: 6 }}
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
            { header: 'Active', render: (row) => (row.active ? 'Yes' : 'No') },
          ]}
        />
      )}
    </div>
  )
}
