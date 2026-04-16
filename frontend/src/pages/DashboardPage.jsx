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
  const [success, setSuccess] = useState('')
  const [uploading, setUploading] = useState(false)
  const [sharingFileId, setSharingFileId] = useState(null)
  const [shareLink, setShareLink] = useState(null)

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
    setSuccess('')
    try {
      const form = new FormData()
      form.append('file', file)
      await api.post('/api/files', form, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      await fetchAll()
      setSuccess(`Uploaded ${file.name}.`)
    } catch (err) {
      setError(err?.response?.data?.message || 'Upload failed')
    } finally {
      setUploading(false)
      e.target.value = ''
    }
  }

  const onDelete = async (id) => {
    setError('')
    setSuccess('')
    try {
      await api.delete(`/api/files/${id}`)
      if (shareLink?.fileId === id) {
        setShareLink(null)
      }
      await fetchAll()
      setSuccess('File deleted.')
    } catch (err) {
      setError(err?.response?.data?.message || 'Delete failed')
    }
  }

  const onRename = async (file) => {
    const next = window.prompt('Rename file to:', file.filename)
    if (!next || !next.trim()) return
    setError('')
    setSuccess('')
    try {
      await api.put(`/api/files/${file.id}`, { filename: next.trim() })
      await fetchAll()
      setSuccess(`Renamed ${file.filename} to ${next.trim()}.`)
    } catch (err) {
      setError(err?.response?.data?.message || 'Rename failed')
    }
  }

  const onDownload = async (file) => {
    setError('')
    setSuccess('')
    try {
      await downloadFile(file.id, file.filename)
      setSuccess(`Downloaded ${file.filename}.`)
    } catch (err) {
      setError(err?.response?.data?.message || 'Download failed')
    }
  }

  const copyShareLink = async (url) => {
    if (!navigator.clipboard?.writeText) {
      return false
    }

    try {
      await navigator.clipboard.writeText(url)
      return true
    } catch {
      return false
    }
  }

  const onShare = async (file) => {
    setError('')
    setSuccess('')
    setSharingFileId(file.id)
    try {
      const res = await api.post(`/api/files/${file.id}/shares`, { expiresInHours: 24 })
      const share = { ...res.data, fileId: file.id }
      setShareLink(share)
      const copied = await copyShareLink(share.shareUrl)
      setSuccess(
        copied
          ? `Share link for ${file.filename} copied to your clipboard.`
          : `Share link for ${file.filename} is ready below.`
      )
    } catch (err) {
      setError(err?.response?.data?.message || 'Share link creation failed')
    } finally {
      setSharingFileId(null)
    }
  }

  const onRevokeShare = async () => {
    if (!shareLink) return

    setError('')
    setSuccess('')
    try {
      await api.delete(`/api/files/${shareLink.fileId}/shares/${shareLink.id}`)
      setShareLink(null)
      setSuccess('Share link revoked.')
    } catch (err) {
      setError(err?.response?.data?.message || 'Share revocation failed')
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
        {success ? <div className="success" style={{ marginBottom: 10 }}>{success}</div> : null}

        <div style={{ overflowX: 'auto' }}>
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Size</th>
                <th>Uploaded</th>
                <th className="muted">Actions</th>
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 ? (
                <tr>
                  <td colSpan={4} className="muted">{loading ? 'Loading…' : 'No files found'}</td>
                </tr>
              ) : (
                rows.map((f) => (
                  <tr key={f.id}>
                    <td>
                      <div style={{ fontWeight: 600 }}>{f.filename}</div>
                      <div className="muted" style={{ fontSize: 12 }}>{f.mimeType || 'application/octet-stream'}</div>
                    </td>
                    <td>{formatBytes(f.fileSize)}</td>
                    <td>{new Date(f.uploadTime).toLocaleString()}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                        <button className="btn" onClick={() => onDownload(f)}>Download</button>
                        <button className="btn" onClick={() => onShare(f)} disabled={sharingFileId === f.id}>
                          {sharingFileId === f.id ? 'Sharing…' : 'Share'}
                        </button>
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
        <div style={{ fontWeight: 700, marginBottom: 6 }}>Share Links</div>
        {shareLink ? (
          <div className="row">
            <div className="muted" style={{ fontSize: 13 }}>
              Public download link for <strong>{shareLink.filename}</strong>. It expires on{' '}
              {new Date(shareLink.expiresAt).toLocaleString()}.
            </div>
            <input className="input" readOnly value={shareLink.shareUrl} />
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              <button className="btn" onClick={() => copyShareLink(shareLink.shareUrl)}>Copy link</button>
              <a className="btn" href={shareLink.shareUrl} target="_blank" rel="noreferrer">Open link</a>
              <button className="btn danger" onClick={onRevokeShare}>Revoke</button>
            </div>
            <div className="muted" style={{ fontSize: 12 }}>
              Downloads via this link: {shareLink.accessCount}
            </div>
          </div>
        ) : (
          <div className="muted" style={{ fontSize: 13 }}>
            Create a 24-hour public download link from any file row. You can revoke the current link from here.
          </div>
        )}
      </div>
    </div>
  )
}
