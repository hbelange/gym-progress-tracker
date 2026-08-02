# Deployment

The app runs on an EC2 instance as the `gym-progress-tracker` systemd service, listening on `:8080` over HTTPS with a self-signed certificate.

## Required environment variables

Set via `/etc/gym-progress-tracker.env` on the instance (root-owned, mode `600`), referenced by the systemd unit's `EnvironmentFile`:

| Variable | Purpose | Notes |
|---|---|---|
| `DB_URL` | Full JDBC URL for the database | Defaults to the local docker-compose Postgres. In prod this is the RDS endpoint, e.g. `jdbc:postgresql://gym-progress-tracker-database-1.cqzemqkaid44.us-east-1.rds.amazonaws.com:5432/gym_tracker` |
| `DB_USERNAME` | Postgres username | Defaults to `gym_user` if unset. This deployment's RDS instance has no such role — prod connects as the RDS master user, `gym_admin` |
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
DB_URL=jdbc:postgresql://<rds-endpoint>:5432/gym_tracker
DB_USERNAME=gym_admin
DB_PASSWORD=<value>
SSL_KEYSTORE_PATH=file:/etc/gym-progress-tracker/certificate.p12
SSL_KEYSTORE_PASSWORD=<value>
JWT_SECRET=<value>
MAIL_USERNAME=<value>
MAIL_PASSWORD=<value>
APP_BASE_URL=https://<your-ec2-host>:8080
EOF
ssh -i <your-key.pem> ec2-user@<your-ec2-host> 'sudo chown root:root /etc/gym-progress-tracker.env && sudo chmod 600 /etc/gym-progress-tracker.env'

# 3. Point the systemd unit at the env file
# The unit file previously hardcoded SPRING_DATASOURCE_URL/USERNAME/PASSWORD as
# Environment= lines. Those must be REMOVED, not just supplemented via
# `systemctl edit` — systemd accumulates repeated Environment= assignments
# from drop-ins rather than replacing them, and Spring's own auto-bound
# SPRING_DATASOURCE_* env vars take precedence over application.properties
# regardless of EnvironmentFile, so a stale Environment= line would keep
# using the old (rotated-out) credentials even after the env file is added.
ssh -i <your-key.pem> ec2-user@<your-ec2-host> 'sudo sed -i "/^  Environment=SPRING_DATASOURCE_/d" /etc/systemd/system/gym-progress-tracker.service'
ssh -i <your-key.pem> ec2-user@<your-ec2-host> 'sudo sed -i "/^\[Service\]/a EnvironmentFile=\/etc\/gym-progress-tracker.env" /etc/systemd/system/gym-progress-tracker.service'
ssh -i <your-key.pem> ec2-user@<your-ec2-host> 'systemctl cat gym-progress-tracker'   # verify: no SPRING_DATASOURCE_* Environment= lines remain, EnvironmentFile= present
ssh -i <your-key.pem> ec2-user@<your-ec2-host> 'sudo systemctl daemon-reload && sudo systemctl restart gym-progress-tracker'
```

This deployment's Postgres is AWS RDS (`gym-progress-tracker-database-1`), not a local docker-compose container — `docker-compose.yml` in this repo is for local development only. Rotate the RDS master password with `aws rds modify-db-instance --db-instance-identifier gym-progress-tracker-database-1 --master-user-password <new> --apply-immediately`.

## Local development

No environment variables are required to run `./mvnw test` — `jwt.secret` is supplied by `src/test/resources/application.properties`, and the SSL keystore / mail credentials are never touched during tests (see the plan's "Global Constraints" for why).

To run the app locally with `./mvnw spring-boot:run` (real HTTPS, real mail), export at minimum:

```bash
export JWT_SECRET=$(openssl rand -base64 32)
export SSL_KEYSTORE_PASSWORD=changeit   # matches the generated dev keystore's password
```
