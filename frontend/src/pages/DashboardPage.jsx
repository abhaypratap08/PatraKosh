import React, { useEffect, useMemo, useState } from 'react'
import api from '../api'
import { useAuth } from '../auth-context'
import { PREVIEW_FILES } from '../preview-data'

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

  const link = document.createElement('a')
  link.href = url
  link.download = filename || 'download'
  document.body.appendChild(link)
  link.click()
  link.remove()

  window.URL.revokeObjectURL(url)
}

function downloadPreviewFile(file) {
  const contents = [
    `Preview file: ${file.filename}`,
    '',
    'The GitHub Pages deployment is a UI preview.',
    'Run PatraKosh locally to upload, download, and share real files.'
  ].join('\n')
  const blob = new Blob([contents], { type: 'text/plain' })
  const url = window.URL.createObjectURL(blob)

  const link = document.createElement('a')
  link.href = url
  link.download = `${file.filename}.preview.txt`
  document.body.appendChild(link)
  link.click()
  link.remove()

  window.URL.revokeObjectURL(url)
}

function statsFromFiles(files) {
  return {
    fileCount: files.length,
    storageUsed: files.reduce((sum, file) => sum + Number(file.fileSize || 0), 0)
  }
}

export default function DashboardPage() {
  const { previewMode } = useAuth()
  const [files, setFiles] = useState([])
  const [previewFiles, setPreviewFiles] = useState(() => PREVIEW_FILES)
  const [stats, setStats] = useState({ fileCount: 0, storageUsed: 0 })
  const [q, setQ] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [uploading, setUploading] = useState(false)
  const [sharingFileId, setSharingFileId] = useState(null)
  const [shareLink, setShareLink] = useState(null)

  const fetchAll = async () => {
    if (previewMode) {
      return
    }

    setError('')
    setLoading(true)
    try {
      const [listRes, statsRes] = await Promise.all([
        api.get('/api/files', { params: q.trim() ? { q: q.trim() } : {} }),
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
    if (previewMode) {
      setLoading(false)
      return
    }

    fetchAll()
  }, [previewMode])

  const allFiles = previewMode ? previewFiles : files
  const query = q.trim().toLowerCase()
  const rows = useMemo(() => {
    if (!query) {
      return allFiles
    }

    return allFiles.filter((file) => {
      const haystack = `${file.filename} ${file.mimeType || ''}`.toLowerCase()
      return haystack.includes(query)
    })
  }, [allFiles, query])
  const derivedStats = previewMode ? statsFromFiles(previewFiles) : stats

  const onUpload = async (event) => {
    const file = event.target.files?.[0]
    if (!file) return

    if (previewMode) {
      setError('Hosted preview mode is read-only. Run the API locally to upload real files.')
      event.target.value = ''
      return
    }

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
      event.target.value = ''
    }
  }

  const onDelete = async (id) => {
    setError('')
    setSuccess('')

    if (previewMode) {
      setPreviewFiles((current) => current.filter((file) => file.id !== id))
      if (shareLink?.fileId === id) {
        setShareLink(null)
      }
      setSuccess('Removed the file from the preview workspace.')
      return
    }

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

    if (previewMode) {
      setPreviewFiles((current) =>
        current.map((item) => (item.id === file.id ? { ...item, filename: next.trim() } : item))
      )
      setSuccess(`Renamed ${file.filename} to ${next.trim()} in the preview workspace.`)
      return
    }

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
      if (previewMode) {
        downloadPreviewFile(file)
      } else {
        await downloadFile(file.id, file.filename)
      }
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

    if (previewMode) {
      setError('Share links are disabled in hosted preview mode. Run PatraKosh locally to issue expiring links.')
      return
    }

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
    if (!shareLink || previewMode) {
      return
    }

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

  const summary = [
    { label: 'Files tracked', value: `${derivedStats.fileCount}` },
    { label: 'Storage footprint', value: formatBytes(derivedStats.storageUsed) },
    { label: 'Workspace mode', value: previewMode ? 'Preview' : 'Live' }
  ]

  return (
    <div className="workspace-grid">
      <section className="card workspace-panel">
        <div className="panel-head">
          <div>
            <span className="eyebrow eyebrow-muted">Workspace</span>
            <h1>Your files</h1>
            <p className="muted">
              {previewMode
                ? 'Browse a stable hosted preview with local interactions for rename and cleanup.'
                : 'Upload, search, download, rename, and delete files from your private document store.'}
            </p>
          </div>

          <div className="panel-actions">
            <input
              className="input search-input"
              placeholder="Search by file name or type"
              value={q}
              onChange={(event) => setQ(event.target.value)}
            />
            {!previewMode ? (
              <button className="btn ghost" onClick={fetchAll} disabled={loading}>
                {loading ? 'Syncing...' : 'Refresh'}
              </button>
            ) : null}
            <label className={`btn primary ${uploading ? 'disabled' : ''}`}>
              {uploading ? 'Uploading...' : previewMode ? 'Upload locally' : 'Upload'}
              <input type="file" onChange={onUpload} style={{ display: 'none' }} disabled={uploading} />
            </label>
          </div>
        </div>

        <div className="stats-grid compact-grid">
          {summary.map((item) => (
            <div key={item.label} className="card stat-tile">
              <span>{item.label}</span>
              <strong>{item.value}</strong>
            </div>
          ))}
        </div>

        {error ? <div className="alert error">{error}</div> : null}
        {success ? <div className="alert success">{success}</div> : null}

        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Size</th>
                <th>Uploaded</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 ? (
                <tr>
                  <td colSpan={4} className="muted">
                    {loading ? 'Loading...' : 'No files matched your search.'}
                  </td>
                </tr>
              ) : (
                rows.map((file) => (
                  <tr key={file.id}>
                    <td>
                      <div className="table-title">{file.filename}</div>
                      <div className="table-subtitle">{file.mimeType || 'application/octet-stream'}</div>
                    </td>
                    <td>{formatBytes(file.fileSize)}</td>
                    <td>{new Date(file.uploadTime).toLocaleString()}</td>
                    <td>
                      <div className="action-row">
                        <button className="btn ghost" onClick={() => onDownload(file)}>Download</button>
                        <button
                          className="btn ghost"
                          onClick={() => onShare(file)}
                          disabled={previewMode || sharingFileId === file.id}
                        >
                          {sharingFileId === file.id ? 'Sharing...' : 'Share'}
                        </button>
                        <button className="btn ghost" onClick={() => onRename(file)}>Rename</button>
                        <button className="btn danger" onClick={() => onDelete(file.id)}>Delete</button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>

      <aside className="sidebar-stack">
        <section className="card side-card">
          <span className="eyebrow eyebrow-muted">Share Links</span>
          <h2>Controlled handoff</h2>
          {previewMode ? (
            <p className="muted">
              The hosted preview intentionally disables signed share links. Run the full stack locally to create a
              24-hour public download URL and revoke it when access should end.
            </p>
          ) : shareLink ? (
            <div className="side-stack">
              <p className="muted">
                Public download link for <strong>{shareLink.filename}</strong>. It expires on{' '}
                {new Date(shareLink.expiresAt).toLocaleString()}.
              </p>
              <input className="input" readOnly value={shareLink.shareUrl} />
              <div className="action-row">
                <button className="btn ghost" onClick={() => copyShareLink(shareLink.shareUrl)}>Copy link</button>
                <a className="btn ghost" href={shareLink.shareUrl} target="_blank" rel="noreferrer">Open</a>
                <button className="btn danger" onClick={onRevokeShare}>Revoke</button>
              </div>
              <p className="muted">Downloads via this link: {shareLink.accessCount}</p>
            </div>
          ) : (
            <p className="muted">
              Issue a link from any file row to create a revocable 24-hour share window for a specific file.
            </p>
          )}
        </section>

        <section className="card side-card">
          <span className="eyebrow eyebrow-muted">Process</span>
          <h2>What this workspace enforces</h2>
          <ul className="plain-list">
            <li>Per-user storage boundaries for file CRUD operations.</li>
            <li>Server-side sessions for the real app, not raw browser tokens.</li>
            <li>Activity history to trace important document actions.</li>
          </ul>
        </section>
      </aside>
    </div>
  )
}
