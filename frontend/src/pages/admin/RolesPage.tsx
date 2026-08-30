import { useMemo, useState, type FormEvent } from 'react'
import { DataTable } from '../../components/DataTable'
import { useConfirm } from '../../components/ConfirmDialogContext'
import { Button, Checkbox, Input } from '../../components/ui'
import { useCreateRole, useDeleteRole, usePermissions, useRoles, useUpdateRole } from '../../hooks/useRoles'
import type { Role } from '../../api/roles'

export function RolesPage() {
  const { data: roles = [], isLoading } = useRoles()
  const { data: permissions = [] } = usePermissions()
  const createMutation = useCreateRole()
  const updateMutation = useUpdateRole()
  const deleteMutation = useDeleteRole()
  const confirmDialog = useConfirm()

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
    const ok = await confirmDialog({ message: `Delete role "${row.roleName}"?`, confirmLabel: 'Delete', danger: true })
    if (ok) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Roles</h1>
      </div>

      <form onSubmit={handleSubmit} className="panel">
        <div className="form-row">
          <Input value={roleName} onChange={(e) => setRoleName(e.target.value)} placeholder="Role name" required disabled={editingSystem} />
          <Input
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Description"
            style={{ flex: 1, minWidth: 200 }}
          />
          <Checkbox label="Active" checked={active} onChange={(e) => setActive(e.target.checked)} disabled={editingSystem} />
        </div>

        <div className="check-grid" style={{ marginBottom: 14 }}>
          {Array.from(permissionsByModule.entries()).map(([moduleCode, modulePermissions]) => {
            const allChecked = modulePermissions.every((p) => permissionIds.has(p.id))
            return (
              <div key={moduleCode} className="check-card">
                <label className="check-card-title">
                  <input type="checkbox" checked={allChecked} onChange={(e) => toggleModule(moduleCode, e.target.checked)} />
                  {moduleCode}
                </label>
                <div className="check-card-body">
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

        <div className="form-row">
          <Button type="submit">{editingId === null ? 'Add role' : 'Save role'}</Button>
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
