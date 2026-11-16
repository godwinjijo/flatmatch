import { useEffect, useState } from 'react'
import { api } from '../api.js'

/** A user's mutual matches, newest first, each with a live compatibility score. */
export default function Matches({ me, onBackToDeck, onOpenChat }) {
  const [matches, setMatches] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api.getMatches(me.id)
      .then(setMatches)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [me.id])

  if (loading) return <div className="page center">Loading matches...</div>

  return (
    <div className="page">
      <h1>Your matches 💛</h1>
      {error && <div className="error">{error}</div>}
      {matches.length === 0 && !error && (
        <p className="subtitle">No matches yet — go swipe! Some people take time to like you back.</p>
      )}
      <div className="match-list">
        {matches.map((m) => (
          <div key={m.matchId} className="card match-card-row clickable" onClick={() => onOpenChat(m)}>
            <div className="avatar">{m.name?.[0] ?? '?'}</div>
            <div className="match-info">
              <h3>{m.name}</h3>
              <p>Matched {new Date(m.matchedAt).toLocaleString()} · click to chat 💬</p>
            </div>
            <div className={'score-badge ' + (m.score >= 80 ? 'great' : m.score >= 60 ? 'ok' : 'low')}>
              {m.score}%
            </div>
          </div>
        ))}
      </div>
      <button className="btn ghost" onClick={onBackToDeck}>← Back to swiping</button>
    </div>
  )
}
