# AI-Powered Task Management Portal — Technical Overview

---

## 1. Assumptions

### Application Design
- **Single user role** — every registered user has identical permissions. There is no admin, moderator, or read-only role. All task operations are scoped to the authenticated user's own data only.
- **User isolation** — every database query filters by `owner` (the logged-in user). A user cannot read, edit, or delete another user's tasks even if they know the task ID.
- **JWT stateless auth** — the server holds no session state. Every request must carry a valid `Authorization: Bearer <token>` header. Default token lifetime is 24 hours (`JWT_EXPIRATION_MS=86400000`).
- **Password security** — passwords are never stored in plain text. BCrypt hashing is applied before persistence.
- **Soft deletes not used** — deleting a task permanently removes it and all its ledger entries (cascade delete). A ledger entry is written first to record the deletion event before the row is removed.

### Database
- **MySQL as primary store** — the application runs against MySQL 8.0 with `ddl-auto: update`, meaning Hibernate auto-creates and migrates tables on startup. No manual SQL scripts are required.
- **H2 in-memory** profile is still available for quick local testing without MySQL (`SPRING_PROFILES_ACTIVE=h2`).
- **No migrations tool** — Flyway/Liquibase was not added; `ddl-auto: update` is sufficient for a development/demo context. Production use would require proper migration scripts.

### AI
- **AI is optional** — the application is fully functional without any API key. A rule-based fallback (`TaskBriefTemplates`) generates sensible task descriptions based on keyword matching in the title.
- **Hugging Face is the active provider** — the free Inference Router (`router.huggingface.co`) is used with the `Qwen/Qwen2.5-7B-Instruct` model via the `together` backend provider.
- **AI output is advisory** — the generated description and priority are pre-filled into the form but the user can edit them before saving. Nothing is auto-saved.

### Blockchain Ledger
- **Mock blockchain, not on-chain** — the ledger is a hash-linked chain stored in the `task_ledger` MySQL table. It is not deployed to Ethereum, Solana, or any public network. It demonstrates the concept of an immutable audit trail using the same cryptographic primitives (SHA-256 chaining) used in real blockchains.
- **Per-task chain** — each task has its own independent chain starting from a genesis block. Chains are not cross-linked.

---

## 2. AI Workflow

### Overview

```
User types task title in UI
        │
        ▼
  Clicks "AI Fill" button
        │
        ▼
POST /api/ai/generate-task
  { "title": "...", "provider": "huggingface" }
        │
        ▼
  AiTaskGeneratorService.generateFromTitle()
        │
        ├─ resolveProvider()
        │     1. Explicit request param ("huggingface")
        │     2. app.ai.provider config value
        │     3. Auto-detect: first key that is non-blank
        │     4. Fallback (no keys set)
        │
        ├─ callHuggingFace(title)
        │     │
        │     ├─ Build system instructions (JSON-only output enforced)
        │     ├─ Build user prompt (title + domain hint + JSON schema)
        │     ├─ POST router.huggingface.co/v1/chat/completions
        │     │     model: "Qwen/Qwen2.5-7B-Instruct:together"
        │     │     max_tokens: 1000, temperature: 0.75
        │     └─ Parse JSON response → description, suggestedPriority, estimatedEffort
        │
        └─ On any exception → fallbackGenerate(title)
              └─ TaskBriefTemplates.build(title)
                    keyword match → category-specific template
                    returns description + priority + effort estimate
        │
        ▼
  AiGenerateResponse {
    description, suggestedPriority,
    estimatedEffort, fallbackUsed, message
  }
        │
        ▼
  Frontend auto-fills description textarea
  and priority dropdown — user reviews and saves
```

### Prompt Design

The prompt is split into two parts:

**System instructions** (enforces output format):
```
You are an expert coach who writes UNIQUE preparation guides per task.
NEVER reuse the same generic template for different tasks.
Respond ONLY with valid JSON. No markdown code fences.
```

**User prompt** (task-specific):
```
Task title: "Fix login bug in React app"
Detected domain hint: software development

Return JSON:
{
  "description": "150-250 words, 5-8 specific bullets...",
  "suggestedPriority": "LOW|MEDIUM|HIGH",
  "estimatedEffort": "realistic range"
}
```

The domain hint is derived from keyword matching on the title (e.g. "bug" → `software development`, "presentation" → `presentation / public speaking`). This steers the model toward domain-appropriate language without requiring a separate classification call.

### Hugging Face Integration

- **Endpoint**: `https://router.huggingface.co/v1/chat/completions`
- **Model**: `Qwen/Qwen2.5-7B-Instruct:together` (Together AI as the backend provider)
- **Auth**: `Authorization: Bearer hf_...` (HF token with `inference.serverless.write` permission)
- **Format**: OpenAI-compatible chat completions API — same request/response shape as OpenAI
- **Why Together**: The HF Inference Router supports multiple backend providers. `together` was selected because it supports `Qwen/Qwen2.5-7B-Instruct` on the free tier. The model name is auto-suffixed with `:together` if no provider is specified in config.

### Fallback System (`TaskBriefTemplates`)

When no API key is set or the provider call fails, the fallback generates a structured brief using keyword-based category detection:

| Detected Category | Keywords Matched | Output Style |
|---|---|---|
| `PRESENTATION` | presentation, pitch, deck, slides | Story / Slides / Rehearsal structure |
| `DEVELOPMENT` | code, api, bug, fix, deploy, react | Reproduce / Fix / Test (bugs) or Scaffold / Build / Handoff |
| `MEETING` | meeting, standup, sync, workshop | Agenda / Materials / Follow-up |
| `STUDY` | exam, study, assignment, learn | Topics / Practice / Review |
| `EMAIL` | email, mail, newsletter, outreach | Strategy / Draft / Tone check |
| `RESEARCH` | research, analysis, report, survey | Question / Sources / Output |
| `DESIGN` | design, ui, ux, wireframe, figma | Discovery / Wireframe / Handoff |
| `INTERVIEW` | interview, hire, resume, cv | STAR stories / Research / Follow-up |
| `MARKETING` | marketing, campaign, seo, ads | Objective / Audience / Assets |
| `GENERAL` | (no match) | 4-phase plan: Understand / Plan / Execute / Close |

The fallback always returns `fallbackUsed: true` in the response so the frontend can display an appropriate message.

### Multi-Provider Support

The service supports three providers, selectable per-request via the `provider` field:

| Provider | Endpoint | Model Config Key |
|---|---|---|
| `huggingface` | `router.huggingface.co/v1/chat/completions` | `app.ai.huggingface-model` |
| `openai` | `api.openai.com/v1/chat/completions` | `app.ai.openai-model` |
| `gemini` | `generativelanguage.googleapis.com/v1beta/...` | `app.ai.gemini-model` |

Provider resolution priority: **request param → config value → auto-detect by key → fallback**.

---

## 3. Blockchain Ledger Implementation

### Concept

A blockchain is a linked list of blocks where each block contains a hash of its own content plus the hash of the previous block. Tampering with any block invalidates all subsequent hashes. This project implements that exact structure in MySQL as an audit trail for every task lifecycle event.

### Data Structure

Each row in `task_ledger` is one block:

```
┌─────────────────────────────────────────────────────────┐
│  Block #N                                               │
│  ─────────────────────────────────────────────────────  │
│  task_id      → which task this block belongs to        │
│  block_index  → sequential position (0, 1, 2, ...)      │
│  event_type   → TASK_CREATED | TASK_UPDATED |           │
│                 STATUS_CHANGED | TASK_DELETED            │
│  payload_hash → SHA-256( eventType | payload |          │
│                           previousHash | blockIndex )    │
│  previous_hash → payload_hash of block N-1              │
│                  (64 zeros for genesis block)            │
│  timestamp    → when this event occurred                 │
└─────────────────────────────────────────────────────────┘
```

### Hash Algorithm

```java
SHA-256( eventType + "|" + payload + "|" + previousHash + "|" + blockIndex )
```

- `eventType` — the action that occurred (e.g. `STATUS_CHANGED`)
- `payload` — serialized task state at the time of the event (e.g. `id=1,title=Fix bug,status=DONE,priority=HIGH`)
- `previousHash` — the `payload_hash` of the immediately preceding block (genesis = `"0000...0000"` × 64)
- `blockIndex` — the sequential block number for this task's chain

### Chain Integrity

```
Block 0 (TASK_CREATED)
  previous_hash = 0000...0000  (genesis)
  payload_hash  = SHA256("TASK_CREATED|id=1,...|0000...0000|0")
        │
        ▼
Block 1 (TASK_UPDATED)
  previous_hash = <Block 0 payload_hash>
  payload_hash  = SHA256("TASK_UPDATED|id=1,...|<Block0Hash>|1")
        │
        ▼
Block 2 (STATUS_CHANGED)
  previous_hash = <Block 1 payload_hash>
  payload_hash  = SHA256("STATUS_CHANGED|DONE|<Block1Hash>|2")
        │
        ▼
Block 3 (TASK_DELETED)
  previous_hash = <Block 2 payload_hash>
  payload_hash  = SHA256("TASK_DELETED|1|<Block2Hash>|3")
```

If any historical block is modified in the database, its `payload_hash` would no longer match the `previous_hash` stored in the next block — the chain is broken and tampering is detectable.

### Events Recorded

| Event | Triggered by | Payload |
|---|---|---|
| `TASK_CREATED` | `POST /api/tasks` | Full task state: id, title, status, priority |
| `TASK_UPDATED` | `PUT /api/tasks/{id}` | Full task state after update |
| `STATUS_CHANGED` | `PATCH /api/tasks/{id}/status` | New status value only |
| `TASK_DELETED` | `DELETE /api/tasks/{id}` | Task ID |

### API

```
GET /api/tasks/{taskId}/ledger
Authorization: Bearer <token>

Response:
[
  {
    "id": 1,
    "blockIndex": 0,
    "eventType": "TASK_CREATED",
    "payloadHash": "a3f9c2...",
    "previousHash": "0000000000000000000000000000000000000000000000000000000000000000",
    "timestamp": "2026-05-19T18:17:27Z"
  },
  {
    "id": 2,
    "blockIndex": 1,
    "eventType": "STATUS_CHANGED",
    "payloadHash": "7b12e4...",
    "previousHash": "a3f9c2...",
    "timestamp": "2026-05-19T18:20:11Z"
  }
]
```

### UI

Each task card has a **⛓ Ledger** button that expands an inline panel showing:
- Block number and event type
- Truncated `payload_hash` (full 64-char SHA-256 hex)
- Truncated `previous_hash` showing the chain link
- A pulsing "Blockchain audit trail" indicator

### Limitations vs Real Blockchain

| Aspect | This Implementation | Real Blockchain |
|---|---|---|
| Storage | MySQL (centralized) | Distributed nodes |
| Consensus | None (single writer) | PoW / PoS / BFT |
| Immutability | Logical (hash chain) | Physical (distributed) |
| Smart contracts | No | Yes (Ethereum etc.) |
| Gas / fees | No | Yes |
| Purpose | Audit trail demo | Trustless decentralization |

The implementation correctly demonstrates the cryptographic core of blockchain (hash chaining, tamper detection) without the infrastructure overhead of a real distributed ledger.

---

## 4. Database Schema

### Tables (MySQL 8.0)

**`users`**
```sql
id            BIGINT        PK, AUTO_INCREMENT
email         VARCHAR(100)  UNIQUE, NOT NULL
full_name     VARCHAR(100)  NOT NULL
password_hash VARCHAR(255)  NOT NULL  -- BCrypt
created_at    DATETIME(6)   NOT NULL
```

**`tasks`**
```sql
id          BIGINT       PK, AUTO_INCREMENT
user_id     BIGINT       FK → users.id, NOT NULL
title       VARCHAR(200) NOT NULL
description TEXT
priority    ENUM('LOW','MEDIUM','HIGH')          NOT NULL
status      ENUM('TODO','IN_PROGRESS','DONE')    NOT NULL
due_date    DATE
created_at  DATETIME(6)  NOT NULL
updated_at  DATETIME(6)
```

**`task_ledger`**
```sql
id            BIGINT       PK, AUTO_INCREMENT
task_id       BIGINT       FK → tasks.id, NOT NULL
event_type    VARCHAR(50)  NOT NULL
payload_hash  VARCHAR(64)  NOT NULL  -- SHA-256 hex
previous_hash VARCHAR(64)  NOT NULL  -- SHA-256 hex or 64 zeros
block_index   BIGINT       NOT NULL
timestamp     DATETIME(6)  NOT NULL
```

---

## 5. Tech Stack Summary

| Layer | Technology | Notes |
|---|---|---|
| Backend | Java 24, Spring Boot 3.4.1 | REST API, stateless |
| Security | Spring Security + JJWT 0.12.6 | JWT HS512, BCrypt |
| Persistence | Spring Data JPA + Hibernate 6 | MySQL 8.0 |
| AI | Hugging Face Inference Router | Qwen2.5-7B-Instruct via Together |
| Frontend | React 19, Vite 8, Tailwind CSS 4 | SPA, Axios |
| API Docs | SpringDoc OpenAPI 2.7 | Swagger UI at `/swagger-ui.html` |
| Build | Maven Wrapper | `./mvnw spring-boot:run` |

---

*Document generated for the AI-Powered Task Management Portal — May 2026*
