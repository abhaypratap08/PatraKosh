import React, { useEffect, useState } from 'react'
import api from '../api'
import { useAuth } from '../auth-context'
import { PREVIEW_ACTIVITY } from '../preview-data'

const actionLabels = {
  UPLOAD: 'Uploaded a file',
  DELETE: 'Deleted a file',
  RENAME: 'Renamed a file',
  LOGIN: 'Opened a session',
  SHARE_LINK_CREATED: 'Created a share link'
}

export default function TimelinePage() {
  const { previewMode } = useAuth()
  const [activities, setActivities] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (previewMode) {
      setActivities(PREVIEW_ACTIVITY)
      setLoading(false)
      return
    }

    api.get('/api/activity')
      .then((res) => setActivities(res.data))
      .catch(() => setError('Failed to load activity history'))
      .finally(() => setLoading(false))
  }, [previewMode])

  return (
    <div className="page-stack">
      <section className="card section-card">
        <span className="eyebrow eyebrow-muted">Audit Trail</span>
        <h1>Timeline</h1>
        <p className="muted">
          Every meaningful action should be explainable later. This view keeps storage events visible for review.
        </p>
      </section>

      {loading ? <div className="card status-card">Loading activity...</div> : null}
      {error ? <div className="alert error">{error}</div> : null}

      {!loading && !error ? (
        <div className="timeline-list">
          {activities.length === 0 ? (
            <div className="card status-card">No activity recorded yet.</div>
          ) : (
            activities.map((activity) => (
              <article key={activity.id} className="card timeline-item">
                <div className="timeline-dot" />
                <div>
                  <h2>{actionLabels[activity.action] || activity.action}</h2>
                  <p>{activity.filename || activity.detail || 'Session event recorded.'}</p>
                  {activity.detail && activity.filename ? <p className="muted">{activity.detail}</p> : null}
                  <span className="timeline-time">{new Date(activity.createdAt).toLocaleString()}</span>
                </div>
              </article>
            ))
          )}
        </div>
      ) : null}
    </div>
  )
}
