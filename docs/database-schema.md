# Database Schema

## ER Diagram

```mermaid
erDiagram
    USERS ||--o{ TASKS : owns
    TASKS ||--o{ TASK_LEDGER : has

    USERS {
        bigint id PK
        varchar email UK
        varchar full_name
        varchar password_hash
        timestamp created_at
    }

    TASKS {
        bigint id PK
        bigint user_id FK
        varchar title
        text description
        varchar priority
        date due_date
        varchar status
        timestamp created_at
        timestamp updated_at
    }

    TASK_LEDGER {
        bigint id PK
        bigint task_id FK
        varchar event_type
        varchar payload_hash
        varchar previous_hash
        bigint block_index
        timestamp timestamp
    }
```

## Tables

### users

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | PK, auto-increment |
| email | VARCHAR(100) | UNIQUE, NOT NULL |
| full_name | VARCHAR(100) | NOT NULL |
| password_hash | VARCHAR | NOT NULL (BCrypt) |
| created_at | TIMESTAMP | NOT NULL |

### tasks

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | PK |
| user_id | BIGINT | FK → users.id |
| title | VARCHAR(200) | NOT NULL |
| description | TEXT | |
| priority | ENUM | LOW, MEDIUM, HIGH |
| due_date | DATE | |
| status | ENUM | TODO, IN_PROGRESS, DONE |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | |

### task_ledger (blockchain bonus)

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | PK |
| task_id | BIGINT | FK → tasks.id |
| event_type | VARCHAR(50) | e.g. TASK_CREATED, STATUS_CHANGED |
| payload_hash | VARCHAR(64) | SHA-256 hex |
| previous_hash | VARCHAR(64) | Previous block hash (genesis = 64 zeros) |
| block_index | BIGINT | Sequential per task |
| timestamp | TIMESTAMP | NOT NULL |
