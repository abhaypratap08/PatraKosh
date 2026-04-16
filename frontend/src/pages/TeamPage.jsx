import React from 'react'
import { PREVIEW_TEAM } from '../preview-data'

const roadmap = [
  'Invite collaborators into managed workspaces.',
  'Add shared folders with scoped permissions.',
  'Track team-level activity without mixing personal storage.',
  'Promote reviewers and approvers through role-aware controls.'
]

export default function TeamPage() {
  return (
    <div className="page-stack">
      <section className="card section-card">
        <span className="eyebrow eyebrow-muted">Team View</span>
        <h1>Collaboration is staged, not improvised.</h1>
        <p className="muted">
          PatraKosh is already structured around private workspaces, share links, and audit history. The next step is
          deliberate team collaboration with shared folders and role-aware access.
        </p>
      </section>

      <section className="team-grid">
        {PREVIEW_TEAM.map((member) => (
          <article key={member.id} className="card profile-card">
            <div className="profile-badge">{member.name.slice(0, 1)}</div>
            <div>
              <h2>{member.name}</h2>
              <p className="muted">{member.role} · {member.zone}</p>
              <p>{member.focus}</p>
            </div>
          </article>
        ))}
      </section>

      <section className="card section-card">
        <span className="eyebrow eyebrow-muted">Next Release Direction</span>
        <h2>What the team surface still needs</h2>
        <ul className="plain-list">
          {roadmap.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      </section>
    </div>
  )
}
