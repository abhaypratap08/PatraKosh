import React from 'react'
import { PREVIEW_LEADERBOARD } from '../preview-data'

export default function LeaderboardPage() {
  return (
    <div className="page-stack">
      <section className="card section-card">
        <span className="eyebrow eyebrow-muted">Operational Snapshot</span>
        <h1>Leaderboard</h1>
        <p className="muted">
          A polished ranking view makes it easier to surface heavy usage, hotspot teams, and review bottlenecks. The
          live ranking API is still to come, so this page presents the intended layout with preview figures.
        </p>
      </section>

      <section className="card leaderboard-card">
        {PREVIEW_LEADERBOARD.map((entry, index) => (
          <div key={entry.id} className="leader-row">
            <div className="leader-rank">0{index + 1}</div>
            <div className="leader-copy">
              <strong>{entry.name}</strong>
              <span>{entry.secondary}</span>
            </div>
            <div className="leader-metric">{entry.metric}</div>
          </div>
        ))}
      </section>
    </div>
  )
}
