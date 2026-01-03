import React, { useState, useEffect } from 'react';
import api from '../api';

function TimelinePage() {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/activity')
      .then(res => setActivities(res.data))
      .catch(err => setError('Failed to load activity'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="page">
      <h2>Timeline</h2>
      <p>Activity history and audit trail.</p>
      {loading && <p>Loading...</p>}
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {!loading && !error && (
        <ul style={{ listStyle: 'none', padding: 0, marginTop: 20 }}>
          {activities.map(a => (
            <li key={a.id} style={{ background: '#fff', padding: 12, marginBottom: 8, borderRadius: 8, border: '1px solid #e0e0e0' }}>
              <strong>{a.action}</strong> — {a.filename || ''} <br />
              <small>{new Date(a.createdAt).toLocaleString()}</small>
            </li>
          ))}
          {activities.length === 0 && <p>No activity yet.</p>}
        </ul>
      )}
    </div>
  );
}

export default TimelinePage;
