import { useMemo, useState, type FormEvent } from 'react'
import { DataTable } from '../../components/DataTable'
import { useCreateRole, useDeleteRole, usePermissions, useRoles, useUpdateRole } from '../../hooks/useRoles'
import type { Role } from '../../api/roles'

export function RolesPage() {
  const { data: roles = [], isLoading } = useRoles()
  const { data: permissions = [] } = usePermissions()
  const createMutation = useCreateRole()
  const updateMutation = useUpdateRole()
  const deleteMutation = useDeleteRole()

  const [editingId, setEditingId] = useState<number | null>(null)
  const [editingSystem, setEditingSystem] = useState(false)
  const [roleName, setRoleName] = useState('')
  const [description, setDescription] = useState('')
  const [active, setActive] = useState(true)
  const [permissionIds, setPermissionIds] = useState<Set<number>>(new Set())

  const permissionsByModule = useMemo(() => {
    const map = new Map<string, typeof permissions>()
    for (const p of permissions) {
      const list = map.get(p.moduleCode) ?? []
      list.push(p)
      map.set(p.moduleCode, list)
    }
    return map
  }, [permissions])

  function resetForm() {
    setEditingId(null)
    setEditingSystem(false)
    setRoleName('')
    setDescription('')
    setActive(true)
    setPermissionIds(new Set())
  }

  function togglePermission(id: number) {
    setPermissionIds((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  function toggleModule(moduleCode: string, checked: boolean) {
    const modulePermissionIds = (permissionsByModule.get(moduleCode) ?? []).map((p) => p.id)
    setPermissionIds((prev) => {
      const next = new Set(prev)
      for (const id of modulePermissionIds) {
        if (checked) next.add(id)
        else next.delete(id)
      }
      return next
    })
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const request = { roleName, description: description || null, active, permissionIds: Array.from(permissionIds) }
    if (editingId === null) {
      await createMutation.mutateAsync(request)
    } else {
      await updateMutation.mutateAsync({ id: editingId, request })
    }
    resetForm()
  }

  function handleEdit(row: Role) {
    setEditingId(row.id)
    setEditingSystem(row.isSystem)
    setRoleName(row.roleName)
    setDescription(row.description ?? '')
    setActive(row.active)
    setPermissionIds(new Set(row.permissions.map((p) => p.id)))
  }

  async function handleDelete(row: Role) {
    if (row.isSystem) return
    if (confirm(`Delete role "${row.roleName}"?`)) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div style={{ maxWidth: 900, margin: '24px auto', textAlign: 'left', padding: '0 16px' }}>
      <h1 style={{ fontSize: 24 }}>Roles</h1>

      <form onSubmit={handleSubmit} style={{ marginBottom: 24, border: '1px solid var(--border)', padding: 16, borderRadius: 6 }}>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 12, flexWrap: 'wrap' }}>
          <input
            value={roleName}
            onChange={(e) => setRoleName(e.target.value)}
            placeholder="Role name"
            required
            disabled={editingSystem}
            style={{ padding: 6 }}
          />
          <input
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Description"
            style={{ padding: 6, flex: 1, minWidth: 200 }}
          />
          <label>
            <input type="checkbox" checked={active} onChange={(e) => setActive(e.target.checked)} disabled={editingSystem} /> Active
          </label>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 10 }}>
          {Array.from(permissionsByModule.entries()).map(([moduleCode, modulePermissions]) => {
            const allChecked = modulePermissions.every((p) => permissionIds.has(p.id))
            return (
              <div key={moduleCode} style={{ border: '1px solid var(--border)', borderRadius: 4, padding: 8 }}>
                <label style={{ fontWeight: 600, fontSize: 13 }}>
                  <input type="checkbox" checked={allChecked} onChange={(e) => toggleModule(moduleCode, e.target.checked)} />{' '}
                  {moduleCode}
                </label>
                <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 4, fontSize: 12 }}>
                  {modulePermissions.map((p) => (
                    <label key={p.id}>
                      <input type="checkbox" checked={permissionIds.has(p.id)} onChange={() => togglePermission(p.id)} /> {p.action}
                    </label>
                  ))}
                </div>
              </div>
            )
          })}
        </div>

        <div style={{ marginTop: 12 }}>
          <button type="submit">{editingId === null ? 'Add role' : 'Save role'}</button>{' '}
          {editingId !== null && (
            <button type="button" onClick={resetForm}>
              Cancel
            </button>
          )}
        </div>
      </form>

      {isLoading ? (
        <p>Loading...</p>
      ) : (
        <DataTable
          rows={roles}
          rowKey={(row) => row.id}
          onEdit={handleEdit}
          onDelete={(row) => (row.isSystem ? undefined : handleDelete(row))}
          columns={[
            { header: 'Role', render: (row) => `${row.roleName}${row.isSystem ? ' (system)' : ''}` },
            { header: 'Description', render: (row) => row.description ?? '-' },
            { header: 'Permissions', render: (row) => row.permissions.length },
            { header: 'Active', render: (row) => (row.active ? 'Yes' : 'No') },
          ]}
        />
      )}
    </div>
  )
}
