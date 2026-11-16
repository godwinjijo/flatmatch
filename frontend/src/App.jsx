import { useEffect, useState } from 'react'
import { api, isLoggedIn, logout, PROFILE_KEY } from './api.js'
import './App.css'
import Auth from './components/Auth.jsx'
import Onboarding from './components/Onboarding.jsx'
import SwipeDeck from './components/SwipeDeck.jsx'
import Matches from './components/Matches.jsx'
import Chat from './components/Chat.jsx'

/**
 * App state machine:
 *   no token            -> Auth (login/register)
 *   token, no profile   -> Onboarding (creates & links the profile)
 *   token + profile     -> Swipe deck / Matches
 */
export default function App() {
  const [user, setUser] = useState(null)      // AuthResponse from /api/auth/me
  const [profile, setProfile] = useState(null)
  const [checked, setChecked] = useState(false)
  const [view, setView] = useState('deck')
  const [activeChat, setActiveChat] = useState(null) // MatchItem being chatted with

  // On load: if we have a token, ask the backend who we are
  useEffect(() => {
    if (!isLoggedIn()) {
      setChecked(true)
      return
    }
    api.me()
      .then((me) => {
        setUser(me)
        if (me.profileId) {
          return api.getProfile(me.profileId).then(setProfile)
        }
      })
      .catch(() => logout()) // invalid/expired token -> start over
      .finally(() => setChecked(true))
  }, [])

  function handleAuthed(auth) {
    setUser(auth)
    if (auth.profileId) {
      api.getProfile(auth.profileId).then(setProfile)
    }
  }

  async function handleProfileCreated(newProfile) {
    setProfile(newProfile)
    localStorage.setItem(PROFILE_KEY, String(newProfile.id))
    const me = await api.me()
    setUser(me)
  }

  function handleLogout() {
    logout()
    setUser(null)
    setProfile(null)
  }

  if (!checked) return <div className="page center">Loading...</div>
  if (!user) return <Auth onAuthed={handleAuthed} />
  if (!profile) return <Onboarding user={user} onCreated={handleProfileCreated} />

  return (
    <div className="app">
      <header className="topbar">
        <span className="logo">🏠 FlatMatch</span>
        <nav>
          <button className={view === 'deck' ? 'nav-btn active' : 'nav-btn'} onClick={() => setView('deck')}>
            Discover
          </button>
          <button className={view === 'matches' ? 'nav-btn active' : 'nav-btn'} onClick={() => setView('matches')}>
            Matches
          </button>
        </nav>
        <button className="user-chip" onClick={handleLogout} title="Log out">
          {user.name.split(' ')[0]} ⎋
        </button>
      </header>

      {view === 'deck' && <SwipeDeck me={profile} onOpenMatches={() => setView('matches')} />}
      {view === 'matches' && (
        <Matches me={profile} onBackToDeck={() => setView('deck')}
          onOpenChat={(m) => { setActiveChat(m); setView('chat') }} />
      )}
      {view === 'chat' && activeChat && (
        <Chat authUserId={user.userId} match={activeChat} onBack={() => setView('matches')} />
      )}
    </div>
  )
}
