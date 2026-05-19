# Submission Notes

## Assumptions

1. **Single role** — all authenticated users have identical permissions. No admin or read-only role. Every query is scoped to the logged-in user's own data.
2. **MySQL as primary database** — runs against MySQL 8.0 locally. Hibernate `ddl-auto: update` auto-creates all tables on first startup. H2 in-memory profile is still available via `SPRING_PROFILES_ACTIVE=h2`.
3. **AI keys optional** — the app is fully demo-able without any API key. A rule-based fallback (`TaskBriefTemplates`) generates category-specific task briefs using keyword matching.
4. **Hugging Face is the active AI provider** — uses the free HF Inference Router with `Qwen/Qwen2.5-7B-Instruct` via the Together backend. No paid subscription required.
5. **Mock blockchain** — the ledger is a SHA-256 hash-linked chain stored in MySQL, not on any public network. It demonstrates tamper-evident audit trail using real blockchain cryptography.
6. **JWT expiry** — default 24 hours (`JWT_EXPIRATION_MS=86400000`).
7. **No soft deletes** — deleting a task writes a `TASK_DELETED` ledger entry first, then permanently removes the task and all its ledger rows (cascade).

---

## AI Workflow

```
User enters title → clicks "AI Fill"
        ↓
POST /api/ai/generate-task  { title, provider: "huggingface" }
        ↓
AiTaskGeneratorService.generateFromTitle(title, provider)
        ↓
resolveProvider():
  1. Explicit request param
  2. app.ai.provider config
  3. Auto-detect by key presence
  4. Fallback (no keys)
        ↓
callHuggingFace(title)
  → Build system prompt (JSON-only output enforced)
  → Build user prompt (title + domain hint + JSON schema)
  → POST https://router.huggingface.co/v1/chat/completions
       model: "Qwen/Qwen2.5-7B-Instruct:together"
  → Parse JSON: { description, suggestedPriority, estimatedEffort }
        ↓
On failure → fallbackGenerate(title)
  → TaskBriefTemplates.build(title)
  → Keyword match → category template
  → Returns description + priority + effort
        ↓
Response: { description, suggestedPriority, estimatedEffort, fallbackUsed, message }
        ↓
Frontend pre-fills form fields — user reviews and saves
```

**Prompt design**: System message enforces JSON-only output. User message includes the task title, a detected domain hint (e.g. "software development" for titles containing "bug" or "api"), and the required JSON schema. This produces task-specific, non-generic output.

**Fallback categories**: PRESENTATION, DEVELOPMENT, MEETING, STUDY, EMAIL, RESEARCH, DESIGN, INTERVIEW, MARKETING, GENERAL — each with a unique structured template.

---

## Blockchain Implementation

- **Type**: Lightweight hash-linked ledger (mock blockchain)
- **Storage**: MySQL `task_ledger` table — one row per block
- **Hash algorithm**: `SHA-256(eventType | payload | previousHash | blockIndex)`
- **Chain**: Each block stores `previous_hash` = the `payload_hash` of the prior block. Genesis block uses 64 zeros as `previous_hash`.
- **Events recorded**: `TASK_CREATED`, `TASK_UPDATED`, `STATUS_CHANGED`, `TASK_DELETED`
- **Tamper detection**: Modifying any historical block breaks the hash chain — all subsequent `previous_hash` values become invalid.
- **API**: `GET /api/tasks/{id}/ledger` returns the full ordered chain
- **UI**: "⛓ Ledger" button on each task card shows block index, event type, and truncated hashes

---

## Challenges Faced

1. **Java 24 + Lombok**: Resolved by removing Lombok entirely and using plain Java getters/setters — JDK 24 compatibility issue with annotation processors.
2. **Hugging Face API URL**: The old `api-inference.huggingface.co/models/{model}/v1/...` path returned 404. The correct 2025 endpoint is `router.huggingface.co/v1/chat/completions` with model format `org/model:provider`.
3. **HF provider selection**: Not all providers support all models. Tested `hf-inference`, `cerebras`, `sambanova`, `fireworks-ai`, `novita` — `together` was the working provider for `Qwen/Qwen2.5-7B-Instruct`.
4. **MySQL on Windows**: `JAVA_HOME` must be set explicitly before running `mvnw.cmd`. MySQL password with special characters (`**`) works fine in Spring config.
5. **CORS + JWT**: Configured stateless Spring Security with explicit allowed origins for the Vite dev server (`http://localhost:5173`).

---

## Deliverables Checklist

- [x] Source code (backend + frontend)
- [x] README with setup, architecture, API docs
- [x] Database schema (`docs/database-schema.md`)
- [x] Technical overview (`docs/TECHNICAL_OVERVIEW.md`)
- [x] MySQL database integration
- [x] Hugging Face AI — real live generation (not fallback)
- [x] Blockchain ledger — SHA-256 hash chain per task
- [ ] GitHub repository link — *upload and paste URL*
- [ ] Demo video (3–5 min) — *record and add link*
