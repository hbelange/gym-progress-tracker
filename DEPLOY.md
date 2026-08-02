# Deployment

The app runs on an EC2 instance as the `gym-progress-tracker` systemd service, listening on `:8080` over HTTPS with a self-signed certificate.

## Required environment variables

Set via `/etc/gym-progress-tracker.env` on the instance (root-owned, mode `600`), referenced by the systemd unit's `EnvironmentFile`:

| Variable | Purpose | Notes |
|---|---|---|
| `DB_USERNAME` | Postgres username | Defaults to `gym_user` if unset |
| `DB_PASSWORD` | Postgres password | No safe default in prod — set explicitly |
| `SSL_KEYSTORE_PATH` | Path to the HTTPS keystore | `file:/etc/gym-progress-tracker/certificate.p12` |
| `SSL_KEYSTORE_PASSWORD` | Keystore password | Required — app fails to start without it |
| `JWT_SECRET` | Base64-encoded 256-bit signing key for verification/reset tokens | Required — app fails to start without it. Generate with `openssl rand -base64 32` |
| `MAIL_USERNAME` | Gmail address used to send verification/reset emails | |
| `MAIL_PASSWORD` | Gmail app password (not the account password) | Generate at https://myaccount.google.com/apppasswords |
| `APP_BASE_URL` | Base URL used to build links in emails | e.g. `https://ec2-54-91-45-98.compute-1.amazonaws.com:8080` |

## One-time EC2 setup

```bash
# 1. Generate a keystore (new key pair, not a re-passworded old one) and upload it
keytool -genkeypair -alias gym-progress-tracker -keyalg RSA -keysize 2048 -validity 3650 \
  -storetype PKCS12 -keystore certificate-new.p12 -storepass <new-password> \
  -dname "CN=<your-ec2-host>" -noprompt
scp -i <your-key.pem> certificate-new.p12 ec2-user@<your-ec2-host>:~/certificate.p12
ssh -i <your-key.pem> ec2-user@<your-ec2-host> '
  sudo mkdir -p /etc/gym-progress-tracker &&
  sudo mv ~/certificate.p12 /etc/gym-progress-tracker/certificate.p12 &&
  sudo chown root:root /etc/gym-progress-tracker/certificate.p12 &&
  sudo chmod 600 /etc/gym-progress-tracker/certificate.p12
'

# 2. Create the env file (fill in real values)
ssh -i <your-key.pem> ec2-user@<your-ec2-host> 'sudo tee /etc/gym-progress-tracker.env > /dev/null' <<'EOF'
DB_USERNAME=gym_user
DB_PASSWORD=<value>
SSL_KEYSTORE_PATH=file:/etc/gym-progress-tracker/certificate.p12
SSL_KEYSTORE_PASSWORD=<value>
JWT_SECRET=<value>
MAIL_USERNAME=<value>
MAIL_PASSWORD=<value>
APP_BASE_URL=https://<your-ec2-host>:8080
EOF
ssh -i <your-key.pem> ec2-user@<your-ec2-host> 'sudo chown root:root /etc/gym-progress-tracker.env && sudo chmod 600 /etc/gym-progress-tracker.env'

# 3. Point the systemd unit at it
ssh -i <your-key.pem> ec2-user@<your-ec2-host> 'sudo systemctl edit gym-progress-tracker'
# Add under [Service]:
#   EnvironmentFile=/etc/gym-progress-tracker.env
ssh -i <your-key.pem> ec2-user@<your-ec2-host> 'sudo systemctl daemon-reload && sudo systemctl restart gym-progress-tracker'
```

If Postgres runs via `docker-compose.yml` on the same instance, also create a `.env` file next to it on the instance with `DB_PASSWORD=<same value>`, so the app and the database agree on the password.

## Local development

No environment variables are required to run `./mvnw test` — `jwt.secret` is supplied by `src/test/resources/application.properties`, and the SSL keystore / mail credentials are never touched during tests (see the plan's "Global Constraints" for why).

To run the app locally with `./mvnw spring-boot:run` (real HTTPS, real mail), export at minimum:

```bash
export JWT_SECRET=$(openssl rand -base64 32)
export SSL_KEYSTORE_PASSWORD=changeit   # matches the generated dev keystore's password
```
