import React, { useState, useEffect } from 'react';
import api from '../api';

function LeaderboardPage() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    // If there were a stats endpoint, we would call it here.
    // For now, show a placeholder.
    setLoading(false);
  }, []);

  return (
    <div className="page">
      <h2>Leaderboard</h2>
      <p>Top users by storage usage and activity.</p>
      {loading && <p>Loading...</p>}
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {!loading && !error && (
        <div style={{ marginTop: 24 }}>
          <p><strong>Feature coming soon:</strong></p>
          <ul>
            <li>Most active users</li>
            <li>Storage usage rankings</li>
            <li>Upload/download counts</li>
            <li>Team contributions</li>
          </ul>
        </div>
      )}
    </div>
  );
}

export default LeaderboardPage;
