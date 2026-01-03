import React from 'react';

function FAQPage() {
  const faqs = [
    { q: 'How do I upload a file?', a: 'Click the Upload button on the Dashboard and select a file from your device.' },
    { q: 'Can I share files with others?', a: 'Yes! Use the Share button to generate a public link. You can also set an expiration date.' },
    { q: 'Is my data secure?', a: 'Passwords are hashed and files are stored per-user. The web version uses JWT for secure API access.' },
    { q: 'What is the storage limit?', a: 'Each user has a 1GB quota by default. Admins can adjust quotas.' },
    { q: 'Can I use S3 or MinIO for storage?', a: 'Yes! Set PATRAKOSH_STORAGE_PROVIDER=s3 and configure the S3 environment variables.' },
    { q: 'How do I run the desktop version?', a: 'Run `mvn javafx:run` after configuring your database in application.properties or via environment variables.' },
    { q: 'How do I run the web version?', a: 'Start the API with `mvn -Dspring-boot.run.mainClass=com.patrakosh.api.ApiApplication spring-boot:run` and the frontend with `cd frontend && npm run dev`.' },
  ];

  return (
    <div className="page">
      <h2>Frequently Asked Questions</h2>
      <div style={{ marginTop: 24 }}>
        {faqs.map((faq, i) => (
          <details key={i} style={{ background: '#fff', padding: 16, marginBottom: 12, borderRadius: 8, border: '1px solid #e0e0e0' }}>
            <summary style={{ fontWeight: 'bold', cursor: 'pointer' }}>{faq.q}</summary>
            <p style={{ marginTop: 8 }}>{faq.a}</p>
          </details>
        ))}
      </div>
    </div>
  );
}

export default FAQPage;
