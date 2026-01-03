import React, { useState } from 'react';

function ReviewPage() {
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    // Here you would send the review to an API endpoint.
    setSubmitted(true);
  };

  if (submitted) {
    return (
      <div className="page">
        <h2>Thank You!</h2>
        <p>Your review has been submitted. We appreciate your feedback!</p>
      </div>
    );
  }

  return (
    <div className="page">
      <h2>Review PatraKosh</h2>
      <p>Help us improve by sharing your experience.</p>
      <form onSubmit={handleSubmit} style={{ marginTop: 24, maxWidth: 500 }}>
        <div style={{ marginBottom: 16 }}>
          <label>Rating</label>
          <div style={{ marginTop: 8 }}>
            {[1, 2, 3, 4, 5].map(star => (
              <button
                key={star}
                type="button"
                style={{ background: 'none', border: 'none', fontSize: 24, cursor: 'pointer', color: star <= rating ? '#FFD700' : '#ccc' }}
                onClick={() => setRating(star)}
              >
                ★
              </button>
            ))}
          </div>
        </div>
        <div style={{ marginBottom: 16 }}>
          <label htmlFor="comment">Comment (optional)</label>
          <textarea
            id="comment"
            rows={4}
            style={{ width: '100%', marginTop: 8, padding: 8, borderRadius: 4, border: '1px solid #ccc' }}
            value={comment}
            onChange={e => setComment(e.target.value)}
          />
        </div>
        <button type="submit" className="btn" disabled={!rating}>
          Submit Review
        </button>
      </form>
    </div>
  );
}

export default ReviewPage;
