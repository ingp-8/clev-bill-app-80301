import { useState, type FormEvent } from 'react'
import { DataTable } from '../../components/DataTable'
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

  const [editingId, setEditingId] = useState<number | null>(null)
  const [form, setForm] = useState(emptyForm)
  const [enabled, setEnabled] = useState(true)
  const [roleIds, setRoleIds] = useState<Set<number>>(new Set())
  const [propertyIds, setPropertyIds] = useState<Set<number>>(new Set())

  function resetForm() {
    setEditingId(null)
    setForm(emptyForm)
    setEnabled(true)
    setRoleIds(new Set())
    setPropertyIds(new Set())
  }

  function toggleSet(set: Set<number>, setter: (s: Set<number>) => void, id: number) {
    const next = new Set(set)
    if (next.has(id)) next.delete(id)
    else next.add(id)
    setter(next)
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
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
        alert('Password is required for a new user')
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
    if (confirm(`Delete user "${row.username}"?`)) {
      await deleteMutation.mutateAsync(row.id)
    }
  }

  return (
    <div style={{ maxWidth: 900, margin: '24px auto', textAlign: 'left', padding: '0 16px' }}>
      <h1 style={{ fontSize: 24 }}>Users</h1>

      <form onSubmit={handleSubmit} style={{ marginBottom: 24, border: '1px solid var(--border)', padding: 16, borderRadius: 6 }}>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 12, flexWrap: 'wrap' }}>
          <input
            value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value })}
            placeholder="Username"
            required
            style={{ padding: 6 }}
          />
          <input
            value={form.fullName}
            onChange={(e) => setForm({ ...form, fullName: e.target.value })}
            placeholder="Full name"
            required
            style={{ padding: 6 }}
          />
          <input
            type="password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            placeholder={editingId === null ? 'Password' : 'New password (leave blank to keep)'}
            style={{ padding: 6 }}
          />
          <label>
            <input type="checkbox" checked={enabled} onChange={(e) => setEnabled(e.target.checked)} /> Enabled
          </label>
        </div>

        <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
          <div>
            <div style={{ fontWeight: 600, fontSize: 13, marginBottom: 4 }}>Roles</div>
            {roles.map((role) => (
              <label key={role.id} style={{ display: 'block', fontSize: 13 }}>
                <input type="checkbox" checked={roleIds.has(role.id)} onChange={() => toggleSet(roleIds, setRoleIds, role.id)} />{' '}
                {role.roleName}
              </label>
            ))}
          </div>
          <div>
            <div style={{ fontWeight: 600, fontSize: 13, marginBottom: 4 }}>Property access</div>
            {properties.map((property) => (
              <label key={property.id} style={{ display: 'block', fontSize: 13 }}>
                <input
                  type="checkbox"
                  checked={propertyIds.has(property.id)}
                  onChange={() => toggleSet(propertyIds, setPropertyIds, property.id)}
                />{' '}
                {property.propertyName}
              </label>
            ))}
            <p style={{ fontSize: 11, color: 'var(--text)', maxWidth: 220 }}>Ignored for Super Admin — they see every property regardless.</p>
          </div>
        </div>

        <div style={{ marginTop: 12 }}>
          <button type="submit">{editingId === null ? 'Add user' : 'Save user'}</button>{' '}
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
