# 勤怠管理アプリ — 要求仕様 Q&A

## 基本スコープ

[Question] 対象ユーザーは社員のみ？管理者（上長）もいる？ロールは何種類？
[Answer]社員のみ、管理者も必要。ロールは一般と管理者

[Question] 主な機能は？（出退勤打刻・勤務時間集計のほかに、休暇申請、残業申請、シフト管理、月次レポート出力など）
[Answer]出退勤打刻・勤務時間集計・休暇申請・残業申請・月次レポート出力

[Question] 社員数の規模感は？（数十人？数百人？）
[Answer]数十人

## 打刻

[Question] 打刻方法は？（Web ブラウザからボタン押下？ICカード？GPS？）
[Answer]Web ブラウザからボタン押下

[Question] 打刻種別は？（出勤・退勤の2種類？休憩開始・休憩終了も必要？）
[Answer]出勤・退勤の2種類

[Question] 打刻忘れや誤打刻を後から修正できる？誰が修正できる？
[Answer]打刻忘れや誤打刻を後から修正できる。修正は本人のみ

## 勤務ルール

[Question] 1日の所定労働時間は？（例: 8時間）
[Answer]8時間

[Question] 残業時間を自動計算する？残業の閾値は？
[Answer]残業時間を自動計算する

[Question] 休日の扱いは？（土日祝休み？シフト制？）
[Answer]土日祝休み

## 技術スタック

[Question] Backend は Java / Spring Boot でよいか？
[Answer]よい

[Question] Frontend は Next.js (TypeScript) でよいか？
[Answer]よい

[Question] DB は PostgreSQL でよいか？
[Answer]よい

[Question] 認証方式は？（ID/パスワード認証？SSO？）
[Answer]ID/パスワード認証

## 追加確認

[Question] 休暇申請に管理者の承認フローは必要？（申請→承認→反映 or 申請→即反映）
[Answer]管理者の承認フローは必要、フローは申請→承認→反映

[Question] 休暇の種類は？（有給・欠勤のみ？病欠・特別休暇なども必要？）
[Answer]有給・欠勤のみ

[Question] 残業申請は事前申請？事後申請？管理者の承認は必要？
[Answer]事前事後どちらも可、管理者の承認は必要

[Question] 月次レポートの出力形式は？（CSV？PDF？画面表示のみ？）内容は勤務時間の一覧？
[Answer]画面表示のみ

[Question] 打刻修正は本人のみとのことだが、管理者は他の社員の打刻を修正できなくてよい？
[Answer]よい
