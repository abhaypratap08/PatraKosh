import React, { useState } from 'react'

export default function ReviewPage() {
  const [rating, setRating] = useState(0)
  const [comment, setComment] = useState('')
  const [submitted, setSubmitted] = useState(false)

  const handleSubmit = (event) => {
    event.preventDefault()
    setSubmitted(true)
  }

  if (submitted) {
    return (
      <div className="page-stack">
        <section className="card review-card review-success">
          <span className="eyebrow eyebrow-muted">Feedback captured</span>
          <h1>Thanks for the review.</h1>
          <p className="muted">
            This demo stores the response locally in the UI layer only. Wire it to a backend endpoint when you want to
            persist product feedback.
          </p>
        </section>
      </div>
    )
  }

  return (
    <div className="page-stack">
      <section className="card review-card">
        <span className="eyebrow eyebrow-muted">Experience Review</span>
        <h1>How does the workspace feel?</h1>
        <p className="muted">Use this screen as the starting point for a future in-product feedback loop.</p>

        <form className="review-form" onSubmit={handleSubmit}>
          <div>
            <label className="muted">Rating</label>
            <div className="rating-row" aria-label="Rating selector">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  type="button"
                  className={`star-button ${star <= rating ? 'active' : ''}`}
                  onClick={() => setRating(star)}
                >
                  ★
                </button>
              ))}
            </div>
          </div>

          <div>
            <label className="muted" htmlFor="comment">Comment</label>
            <textarea
              id="comment"
              className="input textarea"
              rows={5}
              value={comment}
              onChange={(event) => setComment(event.target.value)}
              placeholder="What feels solid, unclear, or rough in the current experience?"
            />
          </div>

          <button type="submit" className="btn primary" disabled={!rating}>
            Submit review
          </button>
        </form>
      </section>
    </div>
  )
}
