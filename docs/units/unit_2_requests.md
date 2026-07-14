# Unit 2: 休暇申請・残業申請・承認

## 概要

休暇申請・残業申請と管理者による承認/却下フローを実装する。

## 依存

- Unit 0（Employee Entity・認証・プロジェクト骨格）

## ユーザーストーリー

- US-8: 社員として、有給休暇を申請したい
- US-9: 社員として、欠勤を申請したい
- US-10: 管理者として、部下の休暇申請を承認または却下したい
- US-11: 社員として、申請した休暇のステータスを確認したい
- US-12: 社員として、残業申請を提出したい（事前・事後どちらも可）
- US-13: 管理者として、部下の残業申請を承認または却下したい
- US-14: 社員として、申請した残業のステータスを確認したい

## テーブル

- `leave_requests`（Flyway: `V3__create_leave_requests.sql`）
- `overtime_requests`（Flyway: `V4__create_overtime_requests.sql`）

## API エンドポイント

| メソッド | パス | 説明 |
|---------|------|------|
| GET | /api/leave-requests | 休暇申請一覧 |
| POST | /api/leave-requests | 休暇申請作成 |
| PUT | /api/leave-requests/{id}/approve | 承認（管理者） |
| PUT | /api/leave-requests/{id}/reject | 却下（管理者） |
| GET | /api/overtime-requests | 残業申請一覧 |
| POST | /api/overtime-requests | 残業申請作成 |
| PUT | /api/overtime-requests/{id}/approve | 承認（管理者） |
| PUT | /api/overtime-requests/{id}/reject | 却下（管理者） |

## 実装スコープ

### Backend

- [ ] Flyway: `V3__create_leave_requests.sql`
- [ ] Flyway: `V4__create_overtime_requests.sql`
- [ ] Entity: `LeaveRequest`
- [ ] Entity: `OvertimeRequest`
- [ ] Repository: `LeaveRequestRepository`
- [ ] Repository: `OvertimeRequestRepository`
- [ ] Service: `LeaveRequestService` interface + impl
- [ ] Service: `OvertimeRequestService` interface + impl
- [ ] Controller: `LeaveRequestController`
- [ ] Controller: `OvertimeRequestController`
- [ ] DTO: record 定義（Request/Response）

### Frontend

- [ ] 休暇申請画面（一覧 + 新規申請フォーム）
- [ ] 残業申請画面（一覧 + 新規申請フォーム）
- [ ] 承認管理画面（管理者のみ・タブ切り替え）

### テスト

- [ ] LeaveRequestService のユニットテスト
- [ ] OvertimeRequestService のユニットテスト
- [ ] LeaveRequestController の @WebMvcTest
- [ ] OvertimeRequestController の @WebMvcTest
- [ ] LeaveRequestRepository の @DataJpaTest
- [ ] OvertimeRequestRepository の @DataJpaTest

## ビジネスルール

- 申請の初期ステータスは `PENDING`
- 承認/却下できるのは `ADMIN` ロールのみ（それ以外は 403）
- 承認済み/却下済みの申請は再承認/再却下できない
- 一般社員は自分の申請のみ閲覧可能
- 管理者は全申請を閲覧可能

## 完了条件

- 休暇申請・残業申請の CRUD が動作する
- 管理者のみ承認/却下できる
- ステータス遷移が正しい（PENDING → APPROVED / REJECTED）
- テストが全て通る
