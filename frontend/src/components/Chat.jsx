import { useEffect, useRef, useState } from 'react'
import { api, TOKEN_KEY, WS_BASE } from '../api.js'

/**
 * Live chat between two matched users.
 * - history loads over REST (GET /api/matches/{matchId}/messages)
 * - live messages flow over WebSocket (/ws/chat?token=...)
 * - the server broadcasts the saved message to BOTH participants, so we only
 *   append messages that arrive from the socket (never add locally on send)
 *
 * authUserId = the login-account id from the JWT. The backend stores senderId
 * as the auth-user id, so that's what we compare bubbles against (NOT the
 * profile id).
 */
export default function Chat({ authUserId, match, onBack }) {
  const [messages, setMessages] = useState([])
  const [text, setText] = useState('')
  const [connected, setConnected] = useState(false)
  const wsRef = useRef(null)
  const bottomRef = useRef(null)

  useEffect(() => {
    api.getChatMessages(match.matchId)
      .then(setMessages)
      .catch((err) => console.error('History failed', err))

    const token = localStorage.getItem(TOKEN_KEY)
    const ws = new WebSocket(`${WS_BASE}/ws/chat?token=${encodeURIComponent(token)}`)
    wsRef.current = ws

    ws.onopen = () => setConnected(true)
    ws.onclose = () => setConnected(false)
    ws.onmessage = (event) => {
      const msg = JSON.parse(event.data)
      if (msg.matchId === match.matchId) {
        setMessages((prev) => [...prev, msg])
      }
    }

    return () => ws.close()
  }, [match.matchId])

  // auto-scroll to the newest message
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  function send(e) {
    e.preventDefault()
    const content = text.trim()
    if (!content || !wsRef.current) return
    wsRef.current.send(JSON.stringify({ matchId: match.matchId, content }))
    setText('')
  }

  return (
    <div className="page chat-page">
      <button className="btn ghost back" onClick={onBack}>← Matches</button>
      <h1>Chat with {match.name}</h1>
      <p className={'conn ' + (connected ? 'on' : 'off')}>
        {connected ? '● live' : '○ connecting...'}
      </p>

      <div className="chat-box">
        {messages.length === 0 && (
          <p className="hint">Say hi! 👋 A good icebreaker: who's buying the first round of groceries?</p>
        )}
        {messages.map((m) => (
          <div key={m.id ?? m.sentAt} className={'bubble ' + (m.senderId === authUserId ? 'mine' : 'theirs')}>
            {m.content}
            <span className="time">{new Date(m.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
          </div>
        ))}
        <div ref={bottomRef} />
      </div>

      <form onSubmit={send} className="chat-input">
        <input
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Type a message..."
          maxLength="2000"
        />
        <button className="btn primary" type="submit">Send</button>
      </form>
    </div>
  )
}
