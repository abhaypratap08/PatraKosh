import React from 'react'

export default function PlaceholderPage({ title }) {
  return (
    <div className="card">
      <h2 style={{ marginTop: 0 }}>{title}</h2>
      <p className="muted">This screen is scaffolded to preserve navigation flow. UI and features will be implemented next.</p>
    </div>
  )
}
