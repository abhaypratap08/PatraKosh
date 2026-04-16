import React from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth-context'
import { PREVIEW_METRICS, PREVIEW_TEAM } from '../preview-data'

const highlights = [
  {
    title: 'Locked-down sessions',
    body: 'PatraKosh uses HttpOnly cookies, rate-limited auth flows, and revocable sessions instead of exposing raw auth tokens to browser scripts.'
  },
  {
    title: 'File-sharing with expiry',
    body: 'Authenticated users can generate time-boxed share links, revoke them on demand, and keep ownership boundaries intact.'
  },
  {
    title: 'Audit-ready activity',
    body: 'Uploads, renames, deletes, and session events feed an activity timeline designed for operational review.'
  }
]

const workflow = [
  'Authenticate with a server-side session.',
  'Upload and organize files inside your personal workspace.',
  'Create a short-lived public link only when you need to share.',
  'Review the activity trail and revoke access when the window closes.'
]

export default function HomePage() {
  const { user, viewer, previewMode } = useAuth()

  return (
    <div className="container">
      <div className="landing-shell">
        <section className="hero-grid">
          <div className="hero-copy">
            <span className="eyebrow">Secure File Workspace</span>
            <h1>PatraKosh keeps documents controlled, traceable, and deployable without the usual UI mess.</h1>
            <p className="hero-copy-text">
              This repository now ships a hardened Spring Boot API, a React web workspace, and a JavaFX desktop
              companion. The live GitHub Pages site runs as a hosted preview, while the full auth and storage flow runs
              locally with the backend.
            </p>

            <div className="hero-actions">
              {user ? <Link className="btn primary" to="/dashboard">Open workspace</Link> : null}
              {!user && previewMode ? <Link className="btn primary" to="/dashboard">Open hosted preview</Link> : null}
              {!user && !previewMode ? <Link className="btn primary" to="/login">Sign in</Link> : null}
              {!user && !previewMode ? <Link className="btn ghost" to="/signup">Create account</Link> : null}
              <a className="btn ghost" href="https://github.com/abhaypratap08/PatraKosh" target="_blank" rel="noreferrer">
                View source
              </a>
            </div>

            <div className="metric-strip">
              {PREVIEW_METRICS.map((metric) => (
                <div key={metric.label} className="metric-card">
                  <strong>{metric.value}</strong>
                  <span>{metric.label}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="hero-panel card surface-strong">
            <div className="surface-kicker">Workspace Snapshot</div>
            <div className="hero-panel-stack">
              <div className="snapshot-card">
                <div>
                  <span className="snapshot-label">Session posture</span>
                  <strong>{previewMode ? 'Hosted preview mode' : viewer ? 'Authenticated workspace' : 'Ready for sign-in'}</strong>
                </div>
                <p>
                  {previewMode
                    ? 'GitHub Pages is serving the UI preview only. Uploads and secure share flows require the local API.'
                    : viewer
                      ? 'Your workspace is ready. File operations, audit history, and expiring shares are available from the dashboard.'
                      : 'Run the API and frontend locally for the full secure workflow, or use the hosted preview to inspect the interface.'}
                </p>
              </div>

              <div className="snapshot-card">
                <div>
                  <span className="snapshot-label">Core loop</span>
                  <strong>Store, share, review, revoke</strong>
                </div>
                <ul className="plain-list">
                  {workflow.map((step) => (
                    <li key={step}>{step}</li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        </section>

        <section className="feature-grid">
          {highlights.map((highlight) => (
            <article key={highlight.title} className="card feature-card">
              <span className="eyebrow eyebrow-muted">Why it matters</span>
              <h2>{highlight.title}</h2>
              <p>{highlight.body}</p>
            </article>
          ))}
        </section>

        <section className="split-section">
          <div className="card section-card">
            <span className="eyebrow eyebrow-muted">Hosted Preview</span>
            <h2>What the public site can show</h2>
            <p>
              The GitHub Pages deployment is intentionally frontend-only. It now loads correctly under the repository
              subpath, uses hash-based routing for static hosting, and opens a preview workspace instead of crashing on a
              missing backend.
            </p>
            <p className="muted">
              For uploads, real downloads, secure sessions, and expiring share links, run the API and frontend together
              from the repository.
            </p>
          </div>

          <div className="card section-card">
            <span className="eyebrow eyebrow-muted">Collaboration Direction</span>
            <h2>Who this interface is built for</h2>
            <div className="team-preview">
              {PREVIEW_TEAM.map((member) => (
                <div key={member.id} className="mini-profile">
                  <strong>{member.name}</strong>
                  <span>{member.role}</span>
                  <p>{member.focus}</p>
                </div>
              ))}
            </div>
          </div>
        </section>
      </div>
    </div>
  )
}
