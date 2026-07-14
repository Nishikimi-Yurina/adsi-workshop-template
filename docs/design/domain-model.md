# ドメインモデル設計

## Entity

### Employee（社員）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK（自動採番） |
| employeeCode | String | 社員コード（ログインID） |
| name | String | 氏名 |
| email | String | メールアドレス |
| password | String | ハッシュ化パスワード |
| role | Role | 一般 / 管理者 |
| version | Long | 楽観ロック用 |

### AttendanceRecord（勤怠記録）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| employeeId | Long | FK → Employee |
| date | LocalDate | 勤務日 |
| clockIn | LocalDateTime | 出勤時刻 |
| clockOut | LocalDateTime | 退勤時刻（nullable） |
| version | Long | 楽観ロック用 |

### LeaveRequest（休暇申請）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| employeeId | Long | FK → Employee |
| leaveType | LeaveType | 有給 / 欠勤 |
| date | LocalDate | 休暇日 |
| reason | String | 申請理由 |
| status | ApprovalStatus | 申請中 / 承認 / 却下 |
| approverId | Long | FK → Employee（承認者、nullable） |
| version | Long | 楽観ロック用 |

### OvertimeRequest（残業申請）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| employeeId | Long | FK → Employee |
| date | LocalDate | 残業日 |
| expectedHours | BigDecimal | 予定残業時間 |
| reason | String | 申請理由 |
| status | ApprovalStatus | 申請中 / 承認 / 却下 |
| approverId | Long | FK → Employee（承認者、nullable） |
| version | Long | 楽観ロック用 |

## Value Object / Enum

### Role

```
EMPLOYEE, ADMIN
```

### LeaveType

```
PAID_LEAVE, ABSENCE
```

### ApprovalStatus

```
PENDING, APPROVED, REJECTED
```

### WorkDuration（勤務時間 VO）

| フィールド | 型 | 説明 |
|-----------|---|------|
| totalMinutes | int | 総勤務時間（分） |
| overtimeMinutes | int | 残業時間（分） |

- 所定労働時間 480分（8時間）超過分が残業

## 関連図

```
Employee (1) ─── (N) AttendanceRecord
Employee (1) ─── (N) LeaveRequest
Employee (1) ─── (N) OvertimeRequest
Employee (1:approver) ─── (N) LeaveRequest
Employee (1:approver) ─── (N) OvertimeRequest
```

## Service

| Service | 責務 |
|---------|------|
| AuthService | ログイン・ログアウト・セッション管理 |
| AttendanceService | 打刻・打刻修正・勤務時間計算 |
| LeaveRequestService | 休暇申請・承認・却下 |
| OvertimeRequestService | 残業申請・承認・却下 |
| MonthlyReportService | 月次集計・レポート生成 |

## Repository

| Repository | 主なメソッド |
|-----------|------------|
| EmployeeRepository | findByEmployeeCode, findById |
| AttendanceRecordRepository | findByEmployeeIdAndDate, findByEmployeeIdAndDateBetween |
| LeaveRequestRepository | findByEmployeeId, findByStatus, findByApproverId |
| OvertimeRequestRepository | findByEmployeeId, findByStatus, findByApproverId |
