# EC2 Operations Cheat Sheet

Quick reference for the instance running `gym-progress-tracker`. See `DEPLOY.md` for the full environment variable reference and one-time setup steps.

## Connecting

```bash
chmod 400 ~/Desktop/A4L.pem   # only needed if permissions get reset
ssh -i ~/Desktop/A4L.pem ec2-user@ec2-54-91-45-98.compute-1.amazonaws.com
```

## Service control

```bash
sudo systemctl status gym-progress-tracker --no-pager
sudo systemctl restart gym-progress-tracker
sudo systemctl stop gym-progress-tracker
sudo systemctl start gym-progress-tracker
sudo systemctl show gym-progress-tracker -p NRestarts,ActiveState,SubState   # crash-loop check
```

## Logs

```bash
sudo journalctl -u gym-progress-tracker --no-pager -n 50    # last 50 lines
sudo journalctl -u gym-progress-tracker -f                  # follow live
```

## Viewing / editing the env file

Location: `/etc/gym-progress-tracker.env` (root-owned, mode `600`). Holds `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SSL_KEYSTORE_PATH`, `SSL_KEYSTORE_PASSWORD`, `JWT_SECRET`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `APP_BASE_URL`.

```bash
sudo cat /etc/gym-progress-tracker.env      # view
sudo vi /etc/gym-progress-tracker.env       # edit
sudo systemctl restart gym-progress-tracker # changes only take effect after a restart
```

## Viewing / editing the systemd unit

```bash
systemctl cat gym-progress-tracker                              # view (resolved, includes drop-ins)
sudo vi /etc/systemd/system/gym-progress-tracker.service        # edit the actual file
sudo systemctl daemon-reload && sudo systemctl restart gym-progress-tracker
```

**Do not use `systemctl edit` to add `EnvironmentFile=`** if the unit already has `Environment=` lines you're trying to replace — systemd accumulates repeated `Environment=` assignments across the base unit and drop-ins rather than replacing them, and Spring Boot's own `SPRING_DATASOURCE_*` auto-bound env vars take precedence over `application.properties` regardless of what an `EnvironmentFile` also sets. Edit the unit file directly to remove stale `Environment=` lines.

## Keystore

Location: `/etc/gym-progress-tracker/certificate.p12`, referenced by `SSL_KEYSTORE_PATH` in the env file.

**Must be owned by `ec2-user`** (the user the service runs as, per `User=ec2-user` in the unit), not `root` — the JVM opens this file directly at startup, so `root:root` ownership with mode `600` causes a silent SSL-context startup failure even though the file exists and systemd itself can read it fine.

Regenerate (new key pair, 10-year validity):
```bash
keytool -genkeypair -alias gym-progress-tracker -keyalg RSA -keysize 2048 -validity 3650 \
  -storetype PKCS12 -keystore certificate-new.p12 -storepass <password> \
  -dname "CN=ec2-54-91-45-98.compute-1.amazonaws.com" -noprompt
scp -i ~/Desktop/A4L.pem certificate-new.p12 ec2-user@ec2-54-91-45-98.compute-1.amazonaws.com:~/certificate.p12
ssh -i ~/Desktop/A4L.pem ec2-user@ec2-54-91-45-98.compute-1.amazonaws.com '
  sudo mv ~/certificate.p12 /etc/gym-progress-tracker/certificate.p12 &&
  sudo chown ec2-user:ec2-user /etc/gym-progress-tracker/certificate.p12 &&
  sudo chmod 600 /etc/gym-progress-tracker/certificate.p12
'
rm certificate-new.p12   # delete the local copy
```

## Database (RDS, not docker-compose)

Production connects to AWS RDS — `docker-compose.yml` in this repo is local-dev only. Instance: `<rds-instance-id>`, endpoint in `DB_URL`, master user `<rds-master-user>`.

```bash
aws rds describe-db-instances --profile <aws-profile> \
  --query "DBInstances[].{Id:DBInstanceIdentifier,Endpoint:Endpoint.Address,Status:DBInstanceStatus}" --output table

# Rotate the master password (update DB_PASSWORD in the env file + restart afterward):
aws rds modify-db-instance --profile <aws-profile> \
  --db-instance-identifier <rds-instance-id> \
  --master-user-password '<new-password>' --apply-immediately
```

## Deploying

Push to `main` — `.github/workflows/deploy.yml` builds the jar, `scp`s it to `~/app/gym-progress-tracker.jar` on the instance, and restarts the service automatically.

```bash
git push origin main
gh run list --repo hbelange/gym-progress-tracker --workflow deploy.yml --limit 1   # watch it
```

## Smoke test after any deploy or config change

```bash
curl -sk -o /dev/null -w "HTTP %{http_code}\n" https://ec2-54-91-45-98.compute-1.amazonaws.com:8080/login
```
Expect `HTTP 200`. Then in a browser: register a throwaway account with a real inbox, confirm the verification email arrives there (not your own address), click the link, log in, and test "forgot password" too.
