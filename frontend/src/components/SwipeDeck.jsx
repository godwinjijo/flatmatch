import { useEffect, useRef, useState } from 'react'
import { api } from '../api.js'

/**
 * Card swipe deck.
 * - Drag the top card left (pass), right (like), or up (super like)
 * - Buttons do the same with a CSS animation
 * - After every swipe we POST to the backend; a mutual like shows the
 *   "It's a Match!" celebration overlay.
 */
const DRAG_THRESHOLD = 120 // px before a drag counts as a swipe

export default function SwipeDeck({ me, onOpenMatches }) {
  const [candidates, setCandidates] = useState([])
  const [loading, setLoading] = useState(true)
  const [matchCelebration, setMatchCelebration] = useState(null)
  const [flyOut, setFlyOut] = useState(null) // 'left' | 'right' | 'up'
  const drag = useRef(null) // live drag state: {dx, dy}

  useEffect(() => {
    api.getCandidates(me.id)
      .then(setCandidates)
      .catch((err) => console.error('Failed to load candidates', err))
      .finally(() => setLoading(false))
  }, [me.id])

  async function decide(decision) {
    const top = candidates[0]
    if (!top || flyOut) return

    setFlyOut(decision === 'PASS' ? 'left' : decision === 'SUPER_LIKE' ? 'up' : 'right')
    setCandidates((c) => c.slice(1)) // pop immediately so the next card is ready

    try {
      const res = await api.swipe(me.id, top.profileId, decision)
      if (res.matched) {
        setMatchCelebration(top)
      }
    } catch (err) {
      console.error('Swipe failed', err)
    } finally {
      setTimeout(() => setFlyOut(null), 300)
    }
  }

  // Keyboard shortcuts: ← pass, → like, ↑ super like
  useEffect(() => {
    const onKey = (e) => {
      if (e.key === 'ArrowLeft') decide('PASS')
      if (e.key === 'ArrowRight') decide('LIKE')
      if (e.key === 'ArrowUp') decide('SUPER_LIKE')
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  })

  // ---- pointer drag handling for the top card ----
  function onPointerDown(e) {
    drag.current = { startX: e.clientX, startY: e.clientY, dx: 0, dy: 0 }
    e.currentTarget.setPointerCapture(e.pointerId)
  }
  function onPointerMove(e) {
    if (!drag.current) return
    drag.current.dx = e.clientX - drag.current.startX
    drag.current.dy = e.clientY - drag.current.startY
    setCardOffset({ dx: drag.current.dx, dy: drag.current.dy })
  }
  function onPointerUp() {
    if (!drag.current) return
    const { dx, dy } = drag.current
    drag.current = null
    setCardOffset(null)
    if (dx > DRAG_THRESHOLD) decide('LIKE')
    else if (dx < -DRAG_THRESHOLD) decide('PASS')
    else if (dy < -DRAG_THRESHOLD) decide('SUPER_LIKE')
  }

  const [cardOffset, setCardOffset] = useState(null)

  if (loading) return <div className="page center">Loading candidates...</div>

  const top = candidates[0]
  const next = candidates[1]

  return (
    <div className="page deck-page">
      <div className="deck">
        {next && (
          <div className="swipe-card behind">
            <CardFace candidate={next} />
          </div>
        )}
        {top && (
          <div
            className={'swipe-card top' + (flyOut ? ` fly-${flyOut}` : '')}
            style={cardOffset && !flyOut ? {
              transform: `translate(${cardOffset.dx}px, ${cardOffset.dy}px) rotate(${cardOffset.dx / 20}deg)`,
            } : undefined}
            onPointerDown={onPointerDown}
            onPointerMove={onPointerMove}
            onPointerUp={onPointerUp}
          >
            <CardFace candidate={top} />
            {cardOffset && (
              <>
                {cardOffset.dx > 40 && <div className="stamp like">LIKE ❤️</div>}
                {cardOffset.dx < -40 && <div className="stamp pass">PASS 💔</div>}
                {cardOffset.dy < -40 && <div className="stamp super">SUPER ⭐</div>}
              </>
            )}
          </div>
        )}
        {!top && (
          <div className="swipe-card empty">
            <h3>No more candidates 😴</h3>
            <p>You've seen everyone compatible so far. Check back later!</p>
          </div>
        )}
      </div>

      <div className="deck-actions">
        <button className="action pass" onClick={() => decide('PASS')} title="Pass (←)">✖</button>
        <button className="action super" onClick={() => decide('SUPER_LIKE')} title="Super like (↑)">⭐</button>
        <button className="action like" onClick={() => decide('LIKE')} title="Like (→)">❤️</button>
      </div>
      <p className="hint">Drag the card or use ← → ↑ · {candidates.length} candidate(s) left</p>

      {matchCelebration && (
        <div className="overlay" onClick={() => setMatchCelebration(null)}>
          <div className="match-card">
            <h1>It's a Match! 🎉</h1>
            <p>You and <b>{matchCelebration.name}</b> both liked each other.</p>
            <button className="btn primary" onClick={() => { setMatchCelebration(null); onOpenMatches() }}>
              See my matches
            </button>
            <button className="btn ghost" onClick={() => setMatchCelebration(null)}>
              Keep swiping
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

function CardFace({ candidate }) {
  return (
    <>
      <div className="card-photo">
        <span>{candidate.name?.[0] ?? '?'}</span>
        <div className="score-badge">{candidate.score}% match</div>
      </div>
      <div className="card-body">
        <h3>{candidate.name}, {candidate.age ?? '?'} <small>📍 {candidate.city || 'unknown city'}</small></h3>
        <p className="bio">{candidate.bio || 'No bio yet.'}</p>
        {candidate.sharedInterests?.length > 0 && (
          <div className="shared">✨ Shared: {candidate.sharedInterests.join(', ')}</div>
        )}
        <div className="breakdown">
          {Object.entries(candidate.breakdown || {})
            .filter(([, pts]) => pts > 0)
            .map(([dim, pts]) => (
              <span key={dim} className="pill">{dim} −{pts}</span>
            ))}
          {Object.values(candidate.breakdown || {}).every((p) => p === 0) && (
            <span className="pill good">lifestyle twin!</span>
          )}
        </div>
      </div>
    </>
  )
}
