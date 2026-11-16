import { useState } from 'react'
import { api } from '../api.js'

/**
 * Login / Register screen. On success we hand the AuthResponse
 * (token is already in localStorage) up to App.
 */
export default function Auth({ onAuthed }) {
  const [mode, setMode] = useState('login') // 'login' | 'register'
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }))

  async function submit(e) {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      const auth =
        mode === 'register'
          ? await api.register(form.name, form.email, form.password)
          : await api.login(form.email, form.password)
      localStorage.setItem('flatmatch_token', auth.token)
      onAuthed(auth)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="page">
      <h1>Welcome to FlatMatch 🏠</h1>
      <p className="subtitle">Find a flatmate by lifestyle, not by luck.</p>

      <div className="auth-tabs">
        <button className={mode === 'login' ? 'nav-btn active' : 'nav-btn'} onClick={() => setMode('login')}>
          Log in
        </button>
        <button className={mode === 'register' ? 'nav-btn active' : 'nav-btn'} onClick={() => setMode('register')}>
          Create account
        </button>
      </div>

      <form onSubmit={submit} className="card form">
        {mode === 'register' && (
          <div className="field">
            <label>Name</label>
            <input required value={form.name} onChange={set('name')} placeholder="Vikram Shah" />
          </div>
        )}
        <div className="field">
          <label>Email</label>
          <input required type="email" value={form.email} onChange={set('email')} placeholder="you@example.com" />
        </div>
        <div className="field">
          <label>Password {mode === 'register' && <small>(min 8 characters)</small>}</label>
          <input required type="password" minLength="8" value={form.password} onChange={set('password')}
            placeholder="••••••••" />
        </div>

        {error && <div className="error">{error}</div>}

        <button className="btn primary" disabled={busy} type="submit">
          {busy ? 'Please wait...' : mode === 'register' ? 'Create account' : 'Log in'}
        </button>
      </form>
    </div>
  )
}
