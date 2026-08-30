import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { useSystemConfig, useUpdateSystemConfig } from '../../hooks/useSystemConfig'

export function SettingsPage() {
  const { data: config, isLoading } = useSystemConfig()
  const updateMutation = useUpdateSystemConfig()

  const [form, setForm] = useState({
    storeName: '',
    gstin: '',
    address: '',
    invoiceSeriesPrefix: '',
    defaultCgstRate: '0',
    defaultSgstRate: '0',
    defaultIgstRate: '0',
    eInvoiceEnabled: false,
  })
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    if (!config) return
    setForm({
      storeName: config.storeName,
      gstin: config.gstin ?? '',
      address: config.address ?? '',
      invoiceSeriesPrefix: config.invoiceSeriesPrefix,
      defaultCgstRate: String(config.defaultCgstRate),
      defaultSgstRate: String(config.defaultSgstRate),
      defaultIgstRate: String(config.defaultIgstRate),
      eInvoiceEnabled: config.eInvoiceEnabled,
    })
  }, [config])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    await updateMutation.mutateAsync({
      storeName: form.storeName,
      gstin: form.gstin || null,
      address: form.address || null,
      invoiceSeriesPrefix: form.invoiceSeriesPrefix,
      defaultCgstRate: Number(form.defaultCgstRate),
      defaultSgstRate: Number(form.defaultSgstRate),
      defaultIgstRate: Number(form.defaultIgstRate),
      eInvoiceEnabled: form.eInvoiceEnabled,
    })
    setSaved(true)
    setTimeout(() => setSaved(false), 2000)
  }

  if (isLoading) return <p>Loading...</p>

  return (
    <div style={{ maxWidth: 480, margin: '24px auto', textAlign: 'left', padding: '0 16px' }}>
      <p>
        <Link to="/">&larr; Back</Link>
      </p>
      <h1 style={{ fontSize: 24 }}>Business Settings</h1>
      <form onSubmit={handleSubmit}>
        <label style={{ display: 'block', marginBottom: 12 }}>
          Store name
          <input
            value={form.storeName}
            onChange={(e) => setForm({ ...form, storeName: e.target.value })}
            required
            style={{ display: 'block', width: '100%', padding: 8, marginTop: 4 }}
          />
        </label>
        <label style={{ display: 'block', marginBottom: 12 }}>
          GSTIN
          <input
            value={form.gstin}
            onChange={(e) => setForm({ ...form, gstin: e.target.value })}
            placeholder="e.g. 27AAAAA0000A1Z5"
            style={{ display: 'block', width: '100%', padding: 8, marginTop: 4 }}
          />
        </label>
        <label style={{ display: 'block', marginBottom: 12 }}>
          Address
          <input
            value={form.address}
            onChange={(e) => setForm({ ...form, address: e.target.value })}
            style={{ display: 'block', width: '100%', padding: 8, marginTop: 4 }}
          />
        </label>
        <label style={{ display: 'block', marginBottom: 12 }}>
          Invoice series prefix
          <input
            value={form.invoiceSeriesPrefix}
            onChange={(e) => setForm({ ...form, invoiceSeriesPrefix: e.target.value })}
            required
            style={{ display: 'block', width: '100%', padding: 8, marginTop: 4 }}
          />
        </label>
        <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
          <label style={{ flex: 1 }}>
            Default CGST %
            <input
              type="number"
              step="0.01"
              value={form.defaultCgstRate}
              onChange={(e) => setForm({ ...form, defaultCgstRate: e.target.value })}
              style={{ display: 'block', width: '100%', padding: 8, marginTop: 4 }}
            />
          </label>
          <label style={{ flex: 1 }}>
            Default SGST %
            <input
              type="number"
              step="0.01"
              value={form.defaultSgstRate}
              onChange={(e) => setForm({ ...form, defaultSgstRate: e.target.value })}
              style={{ display: 'block', width: '100%', padding: 8, marginTop: 4 }}
            />
          </label>
          <label style={{ flex: 1 }}>
            Default IGST %
            <input
              type="number"
              step="0.01"
              value={form.defaultIgstRate}
              onChange={(e) => setForm({ ...form, defaultIgstRate: e.target.value })}
              style={{ display: 'block', width: '100%', padding: 8, marginTop: 4 }}
            />
          </label>
        </div>
        <label style={{ display: 'block', marginBottom: 16 }}>
          <input
            type="checkbox"
            checked={form.eInvoiceEnabled}
            onChange={(e) => setForm({ ...form, eInvoiceEnabled: e.target.checked })}
          />{' '}
          E-invoicing enabled (only turn on once above the government's e-invoice turnover threshold)
        </label>
        <button type="submit" disabled={updateMutation.isPending}>
          {updateMutation.isPending ? 'Saving...' : 'Save'}
        </button>
        {saved && <span style={{ marginLeft: 12, color: 'green' }}>Saved</span>}
      </form>
    </div>
  )
}
