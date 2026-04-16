import React from 'react'

export default function FAQPage() {
  const faqs = [
    { q: 'How do I upload a file?', a: 'Click the Upload button on the Dashboard and select a file from your device.' },
    { q: 'Can I share files with others?', a: 'Yes. Each file row can create a 24-hour public download link, and the current link can be revoked from the dashboard.' },
    { q: 'Is my data secure?', a: 'Passwords are hashed with PBKDF2, the web app uses HttpOnly session cookies instead of exposing auth tokens to JavaScript, auth and share downloads are rate-limited, and users can only access their own files unless they intentionally create a share link.' },
    { q: 'What is the storage limit?', a: 'There is no enforced quota in the current local-storage build.' },
    { q: 'Where are uploaded files stored?', a: 'The API stores file contents in the local `storage/` directory and account/session metadata in `data/state.json` by default. Set `PATRAKOSH_STORAGE_BASE_PATH` or `PATRAKOSH_DATA_BASE_PATH` to change them.' },
    { q: 'How do I run the desktop version?', a: 'Run `mvn javafx:run`. The desktop app now signs in against the same persisted account store and keeps each user’s local desktop files in a separate folder.' },
    { q: 'How do I run the web version?', a: 'Start the API with `mvn -Dspring-boot.run.mainClass=com.patrakosh.api.ApiApplication spring-boot:run` and the frontend with `cd frontend && npm run dev`.' }
  ]

  return (
    <div className="page-stack">
      <section className="card section-card">
        <span className="eyebrow eyebrow-muted">Reference</span>
        <h1>Frequently asked questions</h1>
        <p className="muted">
          The current build is a local-first secure storage workspace. The hosted GitHub Pages version is a stable UI
          preview and not the full backend deployment.
        </p>
      </section>

      <div className="faq-stack">
        {faqs.map((faq, i) => (
          <details key={faq.q} className="card faq-item" open={i === 0}>
            <summary>{faq.q}</summary>
            <p>{faq.a}</p>
          </details>
        ))}
      </div>
    </div>
  )
}
