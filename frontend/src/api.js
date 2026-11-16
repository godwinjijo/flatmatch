/**
 * Single place for every API call the frontend makes.
 * Components never call fetch directly — they call these functions.
 * The JWT is stored in localStorage and attached to every request.
 */

export const TOKEN_KEY = 'flatmatch_token'
export const PROFILE_KEY = 'flatmatch_profile_id'

// Empty in dev (Vite proxies /api to :8081). In production Vercel sets
// VITE_API_URL to the backend URL, e.g. https://flatmatch-api.onrender.com
export const API_BASE = import.meta.env.VITE_API_URL || ''

// WebSocket base for live chat: wss://backend... in prod, ws://localhost:5173 in dev
export const WS_BASE = API_BASE
  ? API_BASE.replace(/^http/, 'ws')
  : `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}`

function authHeaders() {
  const token = localStorage.getItem(TOKEN_KEY)
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function isLoggedIn() {
  return Boolean(localStorage.getItem(TOKEN_KEY))
}

export function logout() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(PROFILE_KEY)
}

async function request(path, options = {}) {
  const res = await fetch(API_BASE + '/api' + path, {
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    ...options,
  })

  if (!res.ok) {
    const body = await res.json().catch(() => ({}))
    // validation errors come as {error, fields: {field: message}}
    const message = body.fields
      ? Object.values(body.fields).join(', ')
      : (body.error || `Request failed (${res.status})`)
    const err = new Error(message)
    err.status = res.status
    throw err
  }
  if (res.status === 204) return null
  return res.json()
}

export const api = {
  // ---- auth ----
  register: (name, email, password) =>
    request('/auth/register', { method: 'POST', body: JSON.stringify({ name, email, password }) }),

  login: (email, password) =>
    request('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),

  me: () => request('/auth/me'),

  // ---- profiles ----
  createProfile: (data) =>
    request('/profiles', { method: 'POST', body: JSON.stringify(data) }),

  getProfile: (id) =>
    request(`/profiles/${id}`),

  getCandidates: (id) =>
    request(`/profiles/${id}/matches`),

  // ---- swipes & matches ----
  swipe: (swiperId, swipeeId, decision) =>
    request('/swipes', {
      method: 'POST',
      body: JSON.stringify({ swiperId, swipeeId, decision }),
    }),

  getMatches: (id) =>
    request(`/matches/${id}`),

  getChatMessages: (matchId) =>
    request(`/matches/${matchId}/messages`),
}
