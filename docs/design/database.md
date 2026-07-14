# DB 設計

## テーブル定義

### employees

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| employee_code | VARCHAR(50) | UNIQUE, NOT NULL | ログインID |
| name | VARCHAR(100) | NOT NULL | 氏名 |
| email | VARCHAR(255) | UNIQUE, NOT NULL | メール |
| password | VARCHAR(255) | NOT NULL | BCrypt ハッシュ |
| role | VARCHAR(20) | NOT NULL | EMPLOYEE / ADMIN |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

### attendance_records

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK → employees, NOT NULL | |
| date | DATE | NOT NULL | 勤務日 |
| clock_in | TIMESTAMP | NOT NULL | 出勤時刻 |
| clock_out | TIMESTAMP | | 退勤時刻 |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

- UNIQUE(employee_id, date) — 1日1レコード

### leave_requests

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK → employees, NOT NULL | 申請者 |
| leave_type | VARCHAR(20) | NOT NULL | PAID_LEAVE / ABSENCE |
| date | DATE | NOT NULL | 休暇日 |
| reason | VARCHAR(500) | | 申請理由 |
| status | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' | PENDING / APPROVED / REJECTED |
| approver_id | BIGINT | FK → employees | 承認者 |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

### overtime_requests

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK → employees, NOT NULL | 申請者 |
| date | DATE | NOT NULL | 残業日 |
| expected_hours | DECIMAL(4,2) | NOT NULL | 予定残業時間 |
| reason | VARCHAR(500) | | 申請理由 |
| status | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' | PENDING / APPROVED / REJECTED |
| approver_id | BIGINT | FK → employees | 承認者 |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

## ER 図

```
┌──────────────┐
│  employees   │
├──────────────┤
│ id (PK)      │
│ employee_code│
│ name         │
│ email        │
│ password     │
│ role         │
│ version      │
└──────┬───────┘
       │
       ├───────────────────────────────────┐
       │ 1:N                               │ 1:N
       ▼                                   ▼
┌──────────────────┐              ┌──────────────────┐
│attendance_records│              │  leave_requests  │
├──────────────────┤              ├──────────────────┤
│ id (PK)          │              │ id (PK)          │
│ employee_id (FK) │              │ employee_id (FK) │
│ date             │              │ leave_type       │
│ clock_in         │              │ date             │
│ clock_out        │              │ reason           │
└──────────────────┘              │ status           │
                                  │ approver_id (FK) │
       │                          └──────────────────┘
       │ 1:N
       ▼
┌──────────────────┐
│overtime_requests │
├──────────────────┤
│ id (PK)          │
│ employee_id (FK) │
│ date             │
│ expected_hours   │
│ reason           │
│ status           │
│ approver_id (FK) │
└──────────────────┘
```

## インデックス

- `attendance_records(employee_id, date)` — 日別検索
- `leave_requests(employee_id, status)` — 社員の申請一覧
- `leave_requests(approver_id, status)` — 管理者の承認待ち
- `overtime_requests(employee_id, status)` — 社員の申請一覧
- `overtime_requests(approver_id, status)` — 管理者の承認待ち

## マイグレーション方針

- Flyway で管理（`ddl-auto` 禁止）
- ファイル命名: `V1__create_employees.sql`, `V2__create_attendance_records.sql`, ...
