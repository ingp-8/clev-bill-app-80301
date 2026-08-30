import { useState, type FormEvent } from 'react'
import { DataTable } from '../../components/DataTable'
import { useConfirm } from '../../components/ConfirmDialogContext'
import { Button, Checkbox, Input } from '../../components/ui'
import { useRoles } from '../../hooks/useRoles'
import { useCreateUser, useDeleteUser, useUpdateUser, useUsers } from '../../hooks/useUsers'
import { useProperty } from '../../property/PropertyContext'
import type { AppUser } from '../../api/users'

const emptyForm = { username: '', fullName: '', password: '' }

export function UsersPage() {
  const { data: users = [], isLoading } = useUsers()
  const { data: roles = [] } = useRoles()
  const { properties } = useProperty()
  const createMutation = useCreateUser()
  const updateMutation = useUpdateUser()
  const deleteMutation = useDeleteUser()
  const confirmDialog = useConfirm()

  const [editingId, setEditingId] = useState<number | null>(null)
  const [form, setForm] = useState(emptyForm)
  const [enabled, setEnabled] = useState(true)
  const [roleIds, setRoleIds] = useState<Set<number>>(new Set())
  const [propertyIds, setPropertyIds] = useState<Set<number>>(new Set())
  const [formError, setFormError] = useState<string | null>(null)

  function resetForm() {
    setEditingId(null)
    setForm(emptyForm)
    setEnabled(true)
    setRoleIds(new Set())
    setPropertyIds(new Set())
    setFormError(null)
  }

  function toggleSet(set: Set<number>, setter: (s: Set<number>) => void, id: number) {
    const next = new Set(set)
    if (next.has(id)) next.delete(id)
    else next.add(id)
    setter(next)
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setFormError(null)
    const request = {
      username: form.username,
      fullName: form.fullName,
      password: form.password || null,
      enabled,
      roleIds: Array.from(roleIds),
      propertyIds: Array.from(propertyIds),
    }
    if (editingId === null) {
      if (!form.password) {
        setFormError('Password is required for a new user')
        return
      }
      await createMutation.mutateAsync(request)
    } else {
      await updateMutation.mutateAsync({ id: editingId, request })
    }
    resetForm()
  }

  function handleEdit(row: AppUser) {
    setEditingId(row.id)
    setForm({ username: row.username, fullName: row.fullName, password: '' })
    setEnabled(row.enabled)
    setRoleIds(new Set(row.roles.map((r) => r.id)))
    setPropertyIds(new Set(row.propertyIds))
  }

  async function handleDelete(row: AppUser) {
    const ok = await confirmDialog({ message: `Delete user "${row.username}"?`, confirmLabel: 'Delete', danger: true })
    if (ok) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Users</h1>
      </div>

      <form onSubmit={handleSubmit} className="panel">
        <div className="form-row">
          <Input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} placeholder="Username" required />
          <Input value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} placeholder="Full name" required />
          <Input
            type="password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            placeholder={editingId === null ? 'Password' : 'New password (leave blank to keep)'}
          />
          <Checkbox label="Enabled" checked={enabled} onChange={(e) => setEnabled(e.target.checked)} />
        </div>

        <div style={{ display: 'flex', gap: 28, flexWrap: 'wrap' }}>
          <div className="field-group">
            <div className="field-group-title">Roles</div>
            {roles.map((role) => (
              <div key={role.id}>
                <Checkbox
                  label={role.roleName}
                  checked={roleIds.has(role.id)}
                  onChange={() => toggleSet(roleIds, setRoleIds, role.id)}
                />
              </div>
            ))}
          </div>
          <div className="field-group">
            <div className="field-group-title">Property access</div>
            {properties.map((property) => (
              <div key={property.id}>
                <Checkbox
                  label={property.propertyName}
                  checked={propertyIds.has(property.id)}
                  onChange={() => toggleSet(propertyIds, setPropertyIds, property.id)}
                />
              </div>
            ))}
            <p className="field-hint">Ignored for Super Admin — they see every property regardless.</p>
          </div>
        </div>

        {formError && <p className="form-error">{formError}</p>}

        <div className="form-row" style={{ marginTop: 14 }}>
          <Button type="submit">{editingId === null ? 'Add user' : 'Save user'}</Button>
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
          rows={users}
          rowKey={(row) => row.id}
          onEdit={handleEdit}
          onDelete={handleDelete}
          columns={[
            { header: 'Username', render: (row) => row.username },
            { header: 'Full name', render: (row) => row.fullName },
            { header: 'Roles', render: (row) => row.roles.map((r) => r.name).join(', ') || '-' },
            { header: 'Properties', render: (row) => row.propertyIds.length },
            { header: 'Enabled', render: (row) => (row.enabled ? 'Yes' : 'No') },
          ]}
        />
      )}
    </div>
  )
}
