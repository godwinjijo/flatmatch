# FlatMatch — Roommate Matching App 🏠❤️

Swipe-based roommate matching app. Find a flatmate by **lifestyle compatibility**:
cleanliness, cooking, schedules, party habits, dealbreakers — not just hobbies.

## Stack
| Layer      | Tech                                    |
|------------|-----------------------------------------|
| Backend    | Java 25, Spring Boot 3.x, Maven         |
| Database   | PostgreSQL 18 (Spring Data JPA)         |
| Frontend   | React (Vite), fetch-based API client    |
| Docs       | Swagger UI (springdoc-openapi)          |

## Project structure
```
flatmatch/
├── ROADMAP.md            ← this file
├── backend/              ← Spring Boot REST API
└── frontend/             ← React app (swipe UI)
```

## MVP Phases (build in this order)

### Phase 1 — Foundation (backend skeleton) ✅ DONE
- [x] Spring Boot project generated, runs on port 8081, connects to PostgreSQL
- [x] Database `flatmatch` created
- [x] `UserProfile` entity + identity fields (name, email, city, bio, photo)
- [x] Lifestyle profile: sliders (cleanliness, social, party level), habits
      (cooking, smoking, pets, guests, night owl), budget, preferred area
- [x] Dealbreaker tags (separate `dealbreakers` table)
- [x] REST API: create / get / list / update / delete profile
- [x] Validation + global error handling (clean 400/404 JSON errors)
- [x] Swagger UI at http://localhost:8081/swagger-ui/index.html

### Phase 2 — Matching engine (the resume highlight ✨) ✅ DONE
- [x] Compatibility score algorithm:
      - dealbreakers = hard filter (candidate excluded entirely)
      - budget non-overlap = hard filter
      - lifestyle distance = weighted similarity (cleanliness 32 / social 32 /
        party 24 / schedule 12 = 100 pts)
      - shared interests = +3 each, capped at +10
- [x] `GET /api/profiles/{id}/matches` returns ranked candidates with score
- [x] Score breakdown in response (points lost per dimension)
- [x] 7 unit tests for the algorithm (pure functions, no DB needed) — all passing

### Phase 3 — Swipe + match ✅ DONE
- [x] `Swipe` entity (like / pass / super-like) with DB-level unique constraint
      (no double swiping, even under concurrent requests)
- [x] `Match` entity with ordered-pair trick (userA always < userB) — one row
      per real-world match, never two
- [x] `POST /api/swipes` — validates self-swipe, unknown profiles, duplicates
- [x] Mutual like/super-like → auto-creates Match, returns `matched: true`
- [x] `GET /api/matches/{userId}` — matches list, newest first, with live
      compatibility score
- [x] 7 unit tests (Mockito) covering the whole mutual-match decision tree

### Phase 4 — React frontend ✅ DONE
- [x] Vite dev proxy (`/api` → Spring Boot :8081) + CORS config on backend
- [x] `api.js` — single API client for all fetch calls
- [x] Onboarding form: sliders, toggles, dealbreaker & interest chips
- [x] Swipe deck: drag gestures (left/right/up), fly-out animations, LIKE/PASS/
      SUPER stamps, keyboard shortcuts, score badge + breakdown pills
- [x] "It's a Match! 🎉" celebration overlay
- [x] Matches page with live score badges
- [x] Profile remembered in localStorage, restore on reload, switch-profile
- [x] Tested end-to-end in a real browser (form → deck → swipe → match → list)
- [x] Bug found & fixed by testing: deck no longer shows already-swiped profiles
      (backend filters via swipe history)

### Phase 5 — Auth + polish (in progress 🚧)
- [x] README.md (features, architecture, matching algorithm, run instructions)
- [x] Auth: register / login with BCrypt password hashing
- [x] JWT stateless auth (HMAC-signed, 24h expiry), Spring Security config
- [x] `AuthUser` account entity kept separate from lifestyle `UserProfile`,
      linked via `auth_user_id` when onboarding completes
- [x] `/api/auth/register`, `/api/auth/login`, `/api/auth/me`
- [x] All other endpoints now require a valid token (403 for anonymous)
- [x] Frontend: Auth page (login/register tabs), token in localStorage,
      automatic session restore on reload, logout button
- [x] 3 JWT unit tests (round-trip, tampered, expired)
- [x] Real-time chat for matched users:
      - plain WebSocket at `/ws/chat?token=JWT` (JWT verified in the handshake
        interceptor — invalid tokens never connect)
      - messages persisted to a `chat_messages` table keyed by match
      - `GET /api/matches/{matchId}/messages` returns history (oldest first)
      - server broadcasts each saved message to BOTH participants' sockets
      - chat UI: bubbles (mine vs theirs), live-connection indicator, history
        on open, auto-scroll
      - 5 chat-rule unit tests — 23 total, all passing
      - verified live in the browser with two users (Vikram in the UI, Meera
        as a scripted socket): two-way messages arrive instantly
- [ ] Deployed demo (Render/Railway for API + Vercel/Netlify for React)

## Stretch ideas (post-MVP)
- Weekly schedule grid (when are you home?)
- Room listings ("I have a room available" mode)
- Compatibility radar chart on match page
- Fake-profile heuristics, report/block

## Resume one-liner (when done)
> Built **FlatMatch**, a full-stack roommate-matching app (Spring Boot, PostgreSQL,
> React) with a weighted lifestyle-compatibility matching engine, swipe-based
> discovery, and JWT auth.
