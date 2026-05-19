# AI-Powered Task Management Portal

Full-stack take-home assignment: Spring Boot REST API + React (Vite) frontend with JWT auth, task CRUD, AI task generation, and a mock blockchain audit ledger.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 17+, Spring Boot 3.4, Spring Security, JPA, JWT |
| Frontend | React 19, Vite, Tailwind CSS 4, Axios, React Router |
| Database | H2 (dev) / PostgreSQL (prod) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| AI | OpenAI or Google Gemini (optional; rule-based fallback) |
| Bonus | Chained SHA-256 mock blockchain ledger per task |

## Project Structure

```
My_Project/
├── backend/          # Spring Boot API
├── frontend/         # React SPA
├── docs/             # Schema & submission notes
├── docker-compose.yml
└── README.md
```

## Architecture

```
React UI  →  REST API (Controllers)
                ↓
           Service Layer (Auth, Tasks, AI, Ledger)
                ↓
           JPA Repositories  →  H2 / PostgreSQL
```

- **Layered architecture**: `controller` → `service` → `repository` → `entity`
- **Security**: Stateless JWT; BCrypt password hashing
- **AI (Option A)**: Title → description, priority, estimated effort
- **Blockchain (Bonus A)**: Each task event appends a hash-linked ledger block

## Quick Start (Local)

### Prerequisites

- JDK 17+ (set `JAVA_HOME`)
- Node.js 18+
- (Optional) OpenAI or Gemini API key for live AI

### 1. Backend

```bash
cd backend
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-24
mvnw.cmd spring-boot:run

# macOS/Linux
export JAVA_HOME=/path/to/jdk
./mvnw spring-boot:run
```

Runs on **http://localhost:8080** with in-memory H2.

- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console (JDBC: `jdbc:h2:mem:taskdb`, user: `sa`, no password)

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**. Vite proxies `/api` to the backend.

### Environment Variables

Copy `backend/.env.example` and `frontend/.env.example`.

| Variable | Description |
|----------|-------------|
| `JWT_SECRET` | Min 32 chars for HMAC-SHA256 |
| `OPENAI_API_KEY` | Optional — enables live AI |
| `GEMINI_API_KEY` | Optional — used if OpenAI key absent |
| `SPRING_PROFILES_ACTIVE` | `h2` (default) or `postgres` |
| `VITE_API_URL` | Leave empty to use Vite proxy |

## API Endpoints

### Auth (public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register user |
| POST | `/api/auth/login` | Login, returns JWT |

### Tasks (Bearer token required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tasks` | List user's tasks |
| GET | `/api/tasks/{id}` | Get task |
| POST | `/api/tasks` | Create task |
| PUT | `/api/tasks/{id}` | Update task |
| PATCH | `/api/tasks/{id}/status` | Body: `{"status":"DONE"}` |
| DELETE | `/api/tasks/{id}` | Delete task |

### AI (protected)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/ai/generate-task` | Body: `{"title":"Prepare client presentation"}` |

**Response example:**

```json
{
  "description": "Research client needs, build slides, rehearse delivery.",
  "suggestedPriority": "HIGH",
  "estimatedEffort": "4 hours",
  "fallbackUsed": false,
  "message": "Generated via OpenAI"
}
```

### Blockchain Ledger (protected, bonus)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tasks/{taskId}/ledger` | Immutable hash chain for task |

## AI Integration

1. User enters a task title and clicks **AI Fill** in the UI.
2. Backend calls OpenAI Chat Completions (or Gemini) with a structured JSON prompt.
3. If no API key or the provider fails, a **rule-based fallback** returns sensible defaults (`fallbackUsed: true`).
4. Frontend auto-fills description and priority fields.

## Database Schema

See [docs/database-schema.md](docs/database-schema.md) for ER diagram and table definitions.

**Tables:** `users`, `tasks`, `task_ledger`

## Docker (optional)

```bash
docker compose up --build
```

Starts PostgreSQL, backend, and frontend.

## Deployment Suggestions

- **Frontend**: Vercel / Netlify — set `VITE_API_URL` to your API URL
- **Backend**: Render / Railway — set `SPRING_PROFILES_ACTIVE=postgres` and DB env vars
- **Database**: Neon / Supabase / Render PostgreSQL

## Demo Video Checklist

1. Register & login flow
2. Create task with **AI Fill**
3. Change status (TODO → IN_PROGRESS → DONE)
4. View blockchain ledger on a task
5. Brief architecture walkthrough

## Submission Document

See [docs/SUBMISSION.md](docs/SUBMISSION.md) for assumptions, AI workflow, and blockchain notes.

## License

MIT — for evaluation purposes.
