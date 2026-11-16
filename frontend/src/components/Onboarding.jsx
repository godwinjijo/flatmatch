import { useState } from 'react'
import { api } from '../api.js'

// Options must match the backend's MatchingService dealbreaker rules
const DEALBREAKER_OPTIONS = [
  { tag: 'smoking', label: '🚭 No smoking' },
  { tag: 'loud-parties', label: '🎉 No loud parties' },
  { tag: 'no-pets-allowed', label: '🐾 Must allow pets' },
  { tag: 'no-guests-allowed', label: '👥 Must allow guests' },
  { tag: 'night-owl', label: '🦉 Must be a night owl' },
  { tag: 'early-riser', label: '🌅 Must be an early riser' },
]

const INTEREST_OPTIONS = [
  'cooking', 'baking', 'trekking', 'gaming', 'board-games',
  'fitness', 'movies', 'music', 'reading', 'pets',
]

function Slider({ label, low, high, value, onChange }) {
  return (
    <div className="field">
      <label>
        {label} <span className="slider-value">{value}</span>
      </label>
      <input
        type="range" min="1" max="5" value={value}
        onChange={(e) => onChange(Number(e.target.value))}
      />
      <div className="slider-labels"><span>{low}</span><span>{high}</span></div>
    </div>
  )
}

function Toggle({ label, value, onChange }) {
  return (
    <label className="toggle">
      <input type="checkbox" checked={value} onChange={(e) => onChange(e.target.checked)} />
      <span>{label}</span>
    </label>
  )
}

function Chips({ options, selected, onToggle }) {
  return (
    <div className="chips">
      {options.map((opt) => {
        const tag = typeof opt === 'string' ? opt : opt.tag
        const label = typeof opt === 'string' ? opt : opt.label
        return (
          <button
            key={tag}
            type="button"
            className={selected.includes(tag) ? 'chip selected' : 'chip'}
            onClick={() => onToggle(tag)}
          >
            {label}
          </button>
        )
      })}
    </div>
  )
}

/** The profile creation form. onCreated(profile) fires after the API accepts it. */
export default function Onboarding({ user, onCreated }) {
  const [form, setForm] = useState({
    name: user?.name || '',
    email: user?.email || '',
    age: 24,
    city: '',
    bio: '',
    cleanliness: 3,
    socialLevel: 3,
    partyLevel: 3,
    nightOwl: false,
    cooksRegularly: false,
    smoker: false,
    petsOk: false,
    guestsOk: true,
    minBudget: 5000,
    maxBudget: 15000,
    preferredArea: '',
  })
  const [dealbreakers, setDealbreakers] = useState([])
  const [interests, setInterests] = useState([])
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const set = (key) => (value) => setForm((f) => ({ ...f, [key]: value }))
  const toggle = (list, setList) => (tag) =>
    setList(list.includes(tag) ? list.filter((t) => t !== tag) : [...list, tag])

  async function submit(e) {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      const profile = await api.createProfile({
        ...form,
        dealbreakers,
        interests,
      })
      onCreated(profile)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="page">
      <h1>Create your FlatMatch profile 🏠</h1>
      <p className="subtitle">Lifestyle over hobbies — that's what makes flatmates work.</p>

      <form onSubmit={submit} className="card form">
        <div className="row">
          <div className="field">
            <label>Name</label>
            <input required value={form.name} onChange={(e) => set('name')(e.target.value)} placeholder="Asha Verma" />
          </div>
          <div className="field">
            <label>Email</label>
            <input required type="email" value={form.email} onChange={(e) => set('email')(e.target.value)} placeholder="asha@example.com" />
          </div>
        </div>

        <div className="row">
          <div className="field">
            <label>Age</label>
            <input required type="number" min="18" max="100" value={form.age}
              onChange={(e) => set('age')(Number(e.target.value))} />
          </div>
          <div className="field">
            <label>City</label>
            <input value={form.city} onChange={(e) => set('city')(e.target.value)} placeholder="Bengaluru" />
          </div>
          <div className="field">
            <label>Preferred area</label>
            <input value={form.preferredArea} onChange={(e) => set('preferredArea')(e.target.value)} placeholder="Koramangala" />
          </div>
        </div>

        <div className="row">
          <div className="field">
            <label>Min budget (₹/mo)</label>
            <input required type="number" min="0" value={form.minBudget}
              onChange={(e) => set('minBudget')(Number(e.target.value))} />
          </div>
          <div className="field">
            <label>Max budget (₹/mo)</label>
            <input required type="number" min="0" value={form.maxBudget}
              onChange={(e) => set('maxBudget')(Number(e.target.value))} />
          </div>
        </div>

        <div className="field">
          <label>About you</label>
          <textarea rows="2" maxLength="500" value={form.bio} onChange={(e) => set('bio')(e.target.value)}
            placeholder="Software dev, bakes on weekends, up for trekking..." />
        </div>

        <h2>Your lifestyle</h2>
        <Slider label="🧹 Cleanliness" low="messy" high="spotless" value={form.cleanliness} onChange={set('cleanliness')} />
        <Slider label="💬 Social at home" low="homebody" high="chatty" value={form.socialLevel} onChange={set('socialLevel')} />
        <Slider label="🎉 Partying" low="never" high="every weekend" value={form.partyLevel} onChange={set('partyLevel')} />

        <div className="toggles">
          <Toggle label="🦉 Night owl" value={form.nightOwl} onChange={set('nightOwl')} />
          <Toggle label="🍳 Cooks regularly" value={form.cooksRegularly} onChange={set('cooksRegularly')} />
          <Toggle label="🚬 Smoker" value={form.smoker} onChange={set('smoker')} />
          <Toggle label="🐾 Pets OK" value={form.petsOk} onChange={set('petsOk')} />
          <Toggle label="👥 Guests OK" value={form.guestsOk} onChange={set('guestsOk')} />
        </div>

        <h2>Dealbreakers <small>(non-negotiable)</small></h2>
        <Chips options={DEALBREAKER_OPTIONS} selected={dealbreakers} onToggle={toggle(dealbreakers, setDealbreakers)} />

        <h2>Interests <small>(small match bonus)</small></h2>
        <Chips options={INTEREST_OPTIONS} selected={interests} onToggle={toggle(interests, setInterests)} />

        {error && <div className="error">{error}</div>}

        <button className="btn primary" disabled={busy} type="submit">
          {busy ? 'Creating...' : 'Create profile & start swiping →'}
        </button>
      </form>
    </div>
  )
}
