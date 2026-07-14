# Unit 1: 打刻・勤務時間・月次レポート

## 概要

出退勤打刻、勤務時間自動計算、月次レポート表示を実装する。

## 依存

- Unit 0（Employee Entity・認証・プロジェクト骨格）

## ユーザーストーリー

- US-3: 社員として、Web ブラウザから出勤ボタンを押して出勤を記録したい
- US-4: 社員として、Web ブラウザから退勤ボタンを押して退勤を記録したい
- US-5: 社員として、打刻忘れや誤打刻を自分で修正したい
- US-6: 社員として、日次の勤務時間（実働時間・残業時間）を確認したい
- US-7: システムとして、所定労働時間（8時間）超過分を残業時間として自動計算する
- US-15: 社員として、自分の月次勤務レポートを画面で確認したい
- US-16: 管理者として、全社員の月次勤務レポートを画面で確認したい

## テーブル

- `attendance_records`（Flyway: `V2__create_attendance_records.sql`）

## API エンドポイント

| メソッド | パス | 説明 |
|---------|------|------|
| POST | /api/attendance/clock-in | 出勤打刻 |
| POST | /api/attendance/clock-out | 退勤打刻 |
| GET | /api/attendance/records?yearMonth=YYYY-MM | 勤怠一覧取得 |
| PUT | /api/attendance/records/{id} | 打刻修正（本人のみ） |
| GET | /api/reports/monthly?yearMonth=YYYY-MM | 月次レポート（自分） |
| GET | /api/reports/monthly/all?yearMonth=YYYY-MM | 全社員月次レポート（管理者） |

## 実装スコープ

### Backend

- [ ] Flyway: `V2__create_attendance_records.sql`
- [ ] Entity: `AttendanceRecord`
- [ ] Value Object: `WorkDuration`（勤務時間計算ロジック）
- [ ] Repository: `AttendanceRecordRepository`
- [ ] Service: `AttendanceService` interface + impl
- [ ] Service: `MonthlyReportService` interface + impl
- [ ] Controller: `AttendanceController`
- [ ] Controller: `ReportController`
- [ ] DTO: record 定義（Request/Response）

### Frontend

- [ ] ダッシュボード（打刻ボタン + 今日の勤怠状況）
- [ ] 勤怠一覧画面（月別テーブル + 修正リンク）
- [ ] 打刻修正画面
- [ ] 月次レポート画面

### テスト

- [ ] WorkDuration のユニットテスト（残業計算ロジック）
- [ ] AttendanceService のユニットテスト
- [ ] MonthlyReportService のユニットテスト
- [ ] AttendanceController の @WebMvcTest
- [ ] ReportController の @WebMvcTest
- [ ] AttendanceRecordRepository の @DataJpaTest

## ビジネスルール

- 1日1レコード（同日二重打刻は 409 エラー）
- 出勤していない状態での退勤は 404 エラー
- 退勤済みの再退勤は 409 エラー
- 他人の打刻修正は 403 エラー
- 勤務時間 = 退勤時刻 − 出勤時刻（分単位）
- 残業時間 = max(0, 勤務時間 − 480分)

## 完了条件

- 出勤・退勤打刻が動作する
- 打刻修正が本人のみ可能
- 勤務時間・残業時間が正しく計算される
- 月次レポートが表示される（管理者は全社員分）
- テストが全て通る
