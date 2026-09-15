# Security

## 秘密の情報

次の情報は GitHub に入れません。

- DB の URL、ユーザー名、パスワード
- 管理者のパスワード
- AWS access key、secret key、session token
- `.pem`、`.key`、証明書ファイル
- DB dump、ログ、AWS の画面証拠

`.env.example` は名前だけのサンプルです。実際の値は環境変数、AWS Secrets Manager、または安全な設定ファイルで管理してください。

## AWS の画面証拠

AWS のスクリーンショットには、AWS Account ID、Instance ID、IP アドレス、メールアドレス、ブラウザの情報が入ることがあります。`AWS证据/` と `evidence/` は Git と Docker build から除外しています。面接用の証拠としてローカルに保存し、公開前には必要な部分をマスクしてください。

## 問題を見つけた場合

パスワードや access key を Issue、Pull Request、コミットメッセージに書かないでください。もし秘密の情報を公開した場合は、すぐに値を無効にして新しい値に変更してください。
