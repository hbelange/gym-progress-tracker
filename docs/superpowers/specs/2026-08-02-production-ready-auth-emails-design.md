# Production-ready password reset & email verification

## Problem

The password reset and email verification flows (added in `6be80f8` and `97611f7`) are not safe to run in production:

- `EmailServiceImpl` hardcodes the recipient of every verification and password-reset email to the developer's personal Gmail address instead of `user.getEmail()`. No real user can currently verify an account or reset a password.
- Verification/reset links are hardcoded to `https://localhost:8080`, which only works on the developer's machine.
- `src/main/resources/application.properties` and `src/main/resources/certificate.p12` are committed to git with real secrets in plaintext: DB password, JWT signing secret, SSL keystore password, and a live Gmail SMTP app password.
- `/forgot-password` and `/resend-verification` have no rate limiting, so either endpoint can be used to spam a user's inbox or exhaust the Gmail sending quota.

## Goals

- Real users receive verification and password-reset emails at their own address, with links that work against the deployed EC2 instance.
- No secrets are hardcoded in source or committed to git going forward.
- Documented, repeatable process to configure secrets on the EC2 instance via a systemd `EnvironmentFile`.
- Leaked credentials are rotated so the values already in git history are useless.
- Basic abuse protection on the two mail-sending endpoints.

## Non-goals

- Rewriting git history to purge old commits (rotation makes this unnecessary; explicitly declined).
- Switching email provider away from Gmail SMTP (explicitly kept as-is).
- General TLS/domain setup — the app keeps using its existing self-signed keystore; the only change is externalizing its password and pointing links at the EC2 IP instead of localhost.
- Distributed/shared rate limiting (Redis, etc.) — single EC2 instance, so in-memory is sufficient.

## Design

### 1. Fix `EmailServiceImpl`

- `sendVerificationEmail` and `sendPasswordResetEmail` send to `user.getEmail()` instead of the hardcoded address.
- Drop the hardcoded `"email"` JWT claim in `createVerificationToken` (unused elsewhere; the subject already carries the username).
- Introduce `app.base-url` (`@Value("${app.base-url}")`) and build the verification/reset links from it instead of the literal `https://localhost:8080` string.

### 2. Externalize configuration

`application.properties` becomes safe to commit — every secret and environment-specific value is a placeholder:

```properties
spring.datasource.username=${DB_USERNAME:gym_user}
spring.datasource.password=${DB_PASSWORD:gym_pass}

server.ssl.key-store=${SSL_KEYSTORE_PATH:classpath:certificate.p12}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}

jwt.secret=${JWT_SECRET}

spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

app.base-url=${APP_BASE_URL:https://localhost:8080}
```

Local dev keeps working unchanged via the `:defaults` — including the bundled `classpath:certificate.p12`. Values with no default (`SSL_KEYSTORE_PASSWORD`, `JWT_SECRET`, `MAIL_USERNAME`, `MAIL_PASSWORD`) make the app fail fast on startup if unset, rather than silently running with a blank secret. In prod, `SSL_KEYSTORE_PATH` is overridden to `file:/etc/gym-progress-tracker/certificate.p12`, so the keystore is no longer required to ship inside the jar.

`docker-compose.yml`'s `POSTGRES_PASSWORD` becomes `${DB_PASSWORD:-gym_pass}`, read from a local `.env` file next to the compose file (gitignored) so the same variable name is used in both dev and prod.

### 3. Stop tracking secret files

- `git rm --cached src/main/resources/application.properties src/main/resources/certificate.p12`
- Add both paths to `.gitignore`.
- Re-add `application.properties` to git with only placeholders (per above) — this version is safe to track.
- `certificate.p12` stays untracked going forward; keep a local-only copy for dev (default `classpath:` lookup still needs it on the local build classpath, so it lives in `src/main/resources/` untracked, same directory, just gitignored).

### 4. Rotate leaked credentials

Regardless of the above, these values are permanently visible in git history and must be rotated:

- Gmail: revoke the current app password, generate a new one.
- JWT secret: `openssl rand -base64 32`.
- DB password: change on the Postgres instance and in the new env file.
- Keystore: generate a brand-new self-signed keystore with `keytool -genkeypair` (a new key pair, not just a new password) — the current `certificate.p12` bytes are permanently in git history since we're not scrubbing it, so re-passwording the existing file (`keytool -storepasswd`) would leave the same private key recoverable from history regardless of the live copy's password.

### 5. EC2 configuration (manual, on the instance)

Confirmed on the live instance (`ec2-54-91-45-98.compute-1.amazonaws.com`): the service is running as `gym-progress-tracker.service`, jar lives at `/home/ec2-user/app/gym-progress-tracker.jar`, listens on `*:8080`, and there is currently no standalone `certificate.p12` on disk — it's only ever been served from inside the jar via `classpath:`.

1. Generate a brand-new self-signed keystore locally (new key pair, not a re-passworded copy of the old one — see rotation note above), then upload it once:
   ```
   keytool -genkeypair -alias gym-progress-tracker -keyalg RSA -keysize 2048 -validity 3650 \
     -storetype PKCS12 -keystore certificate-new.p12 -storepass <new-password> \
     -dname "CN=ec2-54-91-45-98.compute-1.amazonaws.com"
   scp -i A4L.pem certificate-new.p12 ec2-user@ec2-54-91-45-98.compute-1.amazonaws.com:~/certificate.p12
   ssh -i A4L.pem ec2-user@ec2-54-91-45-98.compute-1.amazonaws.com 'sudo mkdir -p /etc/gym-progress-tracker && sudo mv ~/certificate.p12 /etc/gym-progress-tracker/certificate.p12 && sudo chown root:root /etc/gym-progress-tracker/certificate.p12 && sudo chmod 600 /etc/gym-progress-tracker/certificate.p12'
   ```
2. Create `/etc/gym-progress-tracker.env`, owned by root, mode `600`:
   ```
   DB_USERNAME=gym_user
   DB_PASSWORD=<rotated>
   SSL_KEYSTORE_PATH=file:/etc/gym-progress-tracker/certificate.p12
   SSL_KEYSTORE_PASSWORD=<rotated>
   JWT_SECRET=<rotated>
   MAIL_USERNAME=harrisonbelanger@gmail.com
   MAIL_PASSWORD=<rotated app password>
   APP_BASE_URL=https://ec2-54-91-45-98.compute-1.amazonaws.com:8080
   ```
3. Add `EnvironmentFile=/etc/gym-progress-tracker.env` to the `[Service]` section of the `gym-progress-tracker.service` systemd unit, then `systemctl daemon-reload`.
4. If Postgres runs via `docker-compose.yml` on the same instance, create a matching `.env` next to it with `DB_PASSWORD` so both processes agree on the password.

This is infra state on the user's box, not something applied through this repo — steps are handed off for the user to run over SSH.

### 6. Rate limiting

A single `RateLimiter` component, `ConcurrentHashMap<String, Instant>` keyed by the submitted email, shared by `/forgot-password` and `/resend-verification` (both trigger a mail send). A second request for the same email within 60 seconds is dropped silently — same outward behavior as an unknown email today (redirect to the same "sent"/"pending" page), so the cooldown can't be used to enumerate which emails are registered.

### 7. Tests

- `EmailServiceImplTest`: asserts the message `to` is `user.getEmail()`, and the link is built from `app.base-url`.
- `RateLimiter` unit test: first call for a key allows, second call within the window blocks, call after the window elapses allows again.
- Existing `UserServiceImplTest`/`UserControllerTest` continue to pass unchanged.
- Manual smoke test before deploying: register a throwaway address locally with real Gmail SMTP creds, confirm the verification email arrives at that address (not the dev inbox) and the link works.

## Open questions / risks

- None outstanding — confirmed via SSH that the EC2 instance has no standalone keystore file today (it's bundled in the jar via `classpath:`), which is why the design externalizes it to `file:/etc/gym-progress-tracker/certificate.p12` rather than leaving it in git.
