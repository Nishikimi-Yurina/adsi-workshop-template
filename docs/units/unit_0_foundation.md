# Unit 0: 共通基盤

## 概要

プロジェクトの骨格を構築し、認証・Employee 管理を実装する。
Phase B（Unit 1・Unit 2）が独立してテストを実行できる状態にする。

## ユーザーストーリー

- US-1: 社員として、ID/パスワードでログインしたい
- US-2: 社員として、ログアウトしたい

## テーブル

- `employees`（Flyway: `V1__create_employees.sql`）

## API エンドポイント

| メソッド | パス | 説明 |
|---------|------|------|
| POST | /api/auth/login | ログイン |
| POST | /api/auth/logout | ログアウト |
| GET | /api/auth/me | ログインユーザー情報取得 |

## 実装スコープ

### Backend

- [ ] Spring Boot プロジェクト初期化（Gradle / Maven）
- [ ] application.yml（プロファイル: default, test）
- [ ] Flyway 設定 + `V1__create_employees.sql`
- [ ] Enum: `Role`（EMPLOYEE, ADMIN）
- [ ] Enum: `ApprovalStatus`（PENDING, APPROVED, REJECTED）— 後続 Unit で使用
- [ ] Enum: `LeaveType`（PAID_LEAVE, ABSENCE）— 後続 Unit で使用
- [ ] Entity: `Employee`
- [ ] Repository: `EmployeeRepository`
- [ ] Spring Security 設定（`SecurityFilterChain`）
- [ ] Service: `AuthService` interface + impl
- [ ] Controller: `AuthController`
- [ ] `@RestControllerAdvice` 共通エラーハンドリング
- [ ] テスト用初期データ（`data.sql` or テスト内で投入）

### Frontend

- [ ] Next.js プロジェクト初期化
- [ ] 共通レイアウト（Header / Sidebar）
- [ ] API クライアント基盤（`withBasePath` 対応）
- [ ] 認証コンテキスト（ログイン状態管理）
- [ ] ログイン画面
- [ ] 認証ガード（未ログイン時リダイレクト）

### テスト

- [ ] Backend: AuthController の @WebMvcTest
- [ ] Backend: EmployeeRepository の @DataJpaTest
- [ ] Backend: AuthService のユニットテスト
- [ ] Frontend: LoginForm のコンポーネントテスト

## 完了条件

- ログイン/ログアウトが動作する
- テストが全て通る（`./gradlew test` or `mvn test`）
- Frontend のログイン画面が表示される
- 未認証状態で API にアクセスすると 401 が返る
