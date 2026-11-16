# 🏠 FlatMatch — swipe to find your flatmate

Finding a roommate is usually a mess of WhatsApp groups and listing sites where you know nothing about the person. FlatMatch treats it like dating apps do: **you swipe on lifestyle compatibility, not on photos.** When two people swipe right on each other, the match opens a private real-time chat.

**🔗 Live demo: https://flatmatch-jijo8093gmailcoms-projects.vercel.app**

> The backend runs on Render's free tier and sleeps after ~15 min idle — if the first load is slow, give it ~50 seconds to wake up.

## How it works

1. **Sign up / log in** — JWT-based auth (Spring Security + jjwt, tokens expire in 24 h)
2. **Onboard** — answer lifestyle questions (cleanliness, sleep schedule, smoking/drinking, guests, noise tolerance, budget, city)
3. **Swipe** — the app only shows candidates above a compatibility threshold; dealbreakers (e.g. smoking) hard-filter first
4. **Match & chat** — mutual right-swipes create a match and unlock a WebSocket chat room

### The matching engine

Each unanswered lifestyle question carries a weight. For every candidate the engine computes a weighted score across matching answers, and mismatched **dealbreakers remove the candidate entirely** before scoring. Scores are stored per-match, so the swipe deck never recomputes on the fly and the percentage you see is stable.

## Tech stack

| Layer | Choice | Why |
|---|---|---|
| Backend | **Java 25, Spring Boot 4.1** | REST API, Spring Data JPA, Spring Security |
| Database | **PostgreSQL** (Neon, serverless) | relational data: users → profiles → swipes → matches → messages |
| Realtime | **Raw WebSockets** (Spring WebSocket) | two-person chat; STOMP felt like overkill, JWT is verified at handshake |
| Frontend | **React 19 + Vite** | SPA with swipe deck, onboarding wizard, matches list, chat |
| Auth | **JWT** (stateless) | no sessions; token checked in a filter + at the WS handshake |
| Tests | **JUnit 5 + Mockito** | 23 unit tests covering matching, swipes, chat, JWT |
| Infra | **Docker + Render + Vercel + Neon** | all free tier |

## Architecture

```
┌─────────────┐   HTTPS/REST   ┌──────────────────┐   JDBC (SSL)   ┌───────────┐
│ React + Vite│ ─────────────► │ Spring Boot API  │ ─────────────► │ PostgreSQL│
│   Vercel    │                │  Render (Docker) │                │   Neon    │
│             │ ◄─ wss://chat ─│  WebSocket /ws   │                │           │
└─────────────┘                └──────────────────┘                └───────────┘
```

- **REST API** under `/api/**` (auth, profiles, candidates, swipes, matches, chat history)
- **WebSocket** at `/ws/chat?token=...` — the JWT is validated by a handshake interceptor before the socket opens
- **CORS** is configured from the `FLATMATCH_FRONTEND_URL` env var, so the deployed frontend (Vercel) and local dev (Vite proxy) both work

## Running locally

```bash
# 1. Database — any PostgreSQL instance; create a DB named flatmatch
# 2. Backend (port 8081)
cd backend
mvn spring-boot:run          # reads DB from FLATMATCH_DB_* env vars, falls back to localhost defaults

# 3. Frontend (port 5173, proxies /api → :8081)
cd frontend
npm install
npm run dev
```

Tests: `cd backend && mvn test`

## Configuration

All secrets come from environment variables (with local-dev fallbacks). The `FLATMATCH_` prefix avoids collisions with other projects' env vars.

| Variable | Used by | Purpose |
|---|---|---|
| `FLATMATCH_DB_URL` / `_USERNAME` / `_PASSWORD` | backend | Postgres connection (JDBC URL for Render) |
| `FLATMATCH_JWT_SECRET` | backend | HMAC secret for signing tokens |
| `FLATMATCH_FRONTEND_URL` | backend | allowed CORS + WebSocket origin |
| `VITE_API_URL` | frontend | backend base URL in production (empty in dev → Vite proxy) |

## Project structure

```
flatmatch/
├── backend/
│   ├── Dockerfile                # multi-stage: Maven build → slim JRE runtime
│   ├── src/main/java/com/flatmatch/
│   │   ├── controller/           # REST endpoints
│   │   ├── service/              # matching engine, swipe & chat logic
│   │   ├── repository/           # Spring Data JPA repositories
│   │   ├── chat/                 # WebSocket handler + JWT handshake interceptor
│   │   ├── security/             # JWT filter, Spring Security config
│   │   └── config/               # CORS + WebSocket config (env-driven origins)
│   └── src/test/java/            # 23 unit tests
├── frontend/
│   └── src/components/           # Auth, Onboarding, SwipeDeck, Matches, Chat
└── render.yaml                   # Render blueprint for the backend service
```

## What I'd do next

- Move from Hibernate `ddl-auto=update` to proper migrations (Flyway)
- Add CI (GitHub Actions running `mvn test` on every push)
- Refresh-token flow and password hashing upgrade
- A "why did we match?" breakdown of the compatibility score in the UI

---
*Built as a portfolio project to practice the full stack: data modelling, security, realtime communication, and deployment.*
