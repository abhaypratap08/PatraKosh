import React, { useEffect, useMemo, useState } from 'react'
import api from '../api'

function formatBytes(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${(bytes / Math.pow(k, i)).toFixed(i === 0 ? 0 : 2)} ${sizes[i]}`
}

async function downloadFile(id, filename) {
  const res = await api.get(`/api/files/${id}/download`, { responseType: 'blob' })
  const blob = new Blob([res.data])
  const url = window.URL.createObjectURL(blob)

  const a = document.createElement('a')
  a.href = url
  a.download = filename || 'download'
  document.body.appendChild(a)
  a.click()
  a.remove()

  window.URL.revokeObjectURL(url)
}

export default function DashboardPage() {
  const [files, setFiles] = useState([])
  const [stats, setStats] = useState({ fileCount: 0, storageUsed: 0 })
  const [q, setQ] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [uploading, setUploading] = useState(false)

  const fetchAll = async () => {
    setError('')
    setLoading(true)
    try {
      const [listRes, statsRes] = await Promise.all([
        api.get('/api/files', { params: q?.trim() ? { q: q.trim() } : {} }),
        api.get('/api/files/stats')
      ])
      setFiles(listRes.data)
      setStats(statsRes.data)
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load files')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchAll()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const onUpload = async (e) => {
    const file = e.target.files?.[0]
    if (!file) return

    setUploading(true)
    setError('')
    try {
      const form = new FormData()
      form.append('file', file)
      await api.post('/api/files', form, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      await fetchAll()
    } catch (err) {
      setError(err?.response?.data?.message || 'Upload failed')
    } finally {
      setUploading(false)
      e.target.value = ''
    }
  }

  const onDelete = async (id) => {
    setError('')
    try {
      await api.delete(`/api/files/${id}`)
      await fetchAll()
    } catch (err) {
      setError(err?.response?.data?.message || 'Delete failed')
    }
  }

  const onRename = async (file) => {
    const next = window.prompt('Rename file to:', file.filename)
    if (!next || !next.trim()) return
    setError('')
    try {
      await api.put(`/api/files/${file.id}`, { filename: next.trim() })
      await fetchAll()
    } catch (err) {
      setError(err?.response?.data?.message || 'Rename failed')
    }
  }

  const rows = useMemo(() => files || [], [files])

  return (
    <div className="row" style={{ gap: 16 }}>
      <div className="card">
        <div className="topbar" style={{ marginBottom: 10 }}>
          <div>
            <div style={{ fontWeight: 700 }}>Your Files</div>
            <div className="muted" style={{ fontSize: 12 }}>
              {stats.fileCount} files • {formatBytes(stats.storageUsed)} used
            </div>
          </div>

          <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
            <input
              className="input"
              placeholder="Search files…"
              value={q}
              onChange={(e) => setQ(e.target.value)}
              style={{ width: 240 }}
            />
            <button className="btn" onClick={fetchAll} disabled={loading}>Search</button>
            <label className={`btn primary ${uploading ? 'disabled' : ''}`} style={{ display: 'inline-block' }}>
              {uploading ? 'Uploading…' : 'Upload'}
              <input type="file" onChange={onUpload} style={{ display: 'none' }} disabled={uploading} />
            </label>
          </div>
        </div>

        {error ? <div className="error" style={{ marginBottom: 10 }}>{error}</div> : null}

        <div style={{ overflowX: 'auto' }}>
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Size</th>
                <th className="muted">Actions</th>
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 ? (
                <tr>
                  <td colSpan={3} className="muted">{loading ? 'Loading…' : 'No files found'}</td>
                </tr>
              ) : (
                rows.map((f) => (
                  <tr key={f.id}>
                    <td>
                      <div style={{ fontWeight: 600 }}>{f.filename}</div>
                      <div className="muted" style={{ fontSize: 12 }}>{f.mimeType || 'application/octet-stream'}</div>
                    </td>
                    <td>{formatBytes(f.fileSize)}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                        <button className="btn" onClick={() => downloadFile(f.id, f.filename)}>Download</button>
                        <button className="btn" onClick={() => onRename(f)}>Rename</button>
                        <button className="btn danger" onClick={() => onDelete(f.id)}>Delete</button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card">
        <div style={{ fontWeight: 700, marginBottom: 6 }}>Next</div>
        <div className="muted" style={{ fontSize: 13 }}>
          Sharing, activity feed, and the remaining screens (Team/Timeline/Leaderboard/FAQ/Review) will be wired up next.
        </div>
      </div>
    </div>
  )
}
