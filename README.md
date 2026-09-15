# Self Blog

Java と Spring Boot で作った個人ブログです。記事を Markdown で書き、公開・下書き・編集・削除ができます。

## 主な機能

- 記事の作成、下書き保存、公開、編集、削除
- Markdown の表示
- 管理者ログイン
- 英語と中国語の表示
- Bootstrap を使った画面
- ログイン失敗回数の制限
- 7 日間のセッション

## 使用技術

- Java 21
- Spring Boot 4
- Spring MVC / Spring Security
- Thymeleaf / Bootstrap / JavaScript
- MyBatis / MySQL 8
- JUnit / Spring Security Test
- Docker

## セキュリティ

- パスワードは BCrypt でハッシュ化します。
- DB と管理者の値は環境変数から読みます。
- Markdown の HTML と URL を安全な形で表示します。
- CSRF、CSP、Referrer-Policy などを設定しています。
- 管理画面は `ADMIN` ロールが必要です。

詳細は [SECURITY.md](SECURITY.md) を見てください。

## ローカルで起動する

### 1. 必要なもの

- Java 21
- MySQL 8 以上

### 2. DB を作る

```sql
CREATE DATABASE blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

次に `db/schema.sql` を実行します。

```bash
mysql -u root -p blog < db/schema.sql
```

### 3. 環境変数を設定する

`.env.example` を見て、自分の値を環境変数に設定します。実際の `.env` は Git に入れません。

```bash
export BLOG_DB_URL='jdbc:mysql://localhost:3306/blog'
export BLOG_DB_USERNAME='bloguser'
export BLOG_DB_PASSWORD='your-db-password'
export BLOG_ADMIN_USERNAME='admin'
export BLOG_ADMIN_PASSWORD='a-long-and-unique-password'
```

### 4. 起動する

```bash
./mvnw spring-boot:run
```

ブラウザで `http://localhost:8080` を開きます。

## テスト

テスト用 DB の環境変数を設定してから実行します。

```bash
export BLOG_TEST_DB_URL='jdbc:mysql://localhost:3306/blog'
export BLOG_TEST_DB_USERNAME='bloguser'
export BLOG_TEST_DB_PASSWORD='your-test-db-password'
./mvnw test
```

## Docker

```bash
docker build -t self-blog .
docker run --rm -p 8080:8080 --env-file .env self-blog
```

本番では、アプリを直接公開しません。リバースプロキシを前に置き、HTTPS を使います。詳しくは [DEPLOYMENT.md](DEPLOYMENT.md) を見てください。

## AWS で行ったこと

- EC2 で Docker コンテナを実行
- RDS MySQL を非公開で使う
- Security Group で DB の接続元を EC2 に限定
- Session Manager で SSH を開けずに管理
- Caddy と Let's Encrypt で HTTPS を設定

AWS の画面証拠は個人情報を含む可能性があるため、この公開用リポジトリには入れません。
