# Gym Progress Tracker

A Spring Boot app for tracking gym and health progress.

[https://ec2-54-91-45-98.compute-1.amazonaws.com:8080](https://ec2-54-91-45-98.compute-1.amazonaws.com:8080)

Login as `demo` / `demo` — no signup needed. The browser will warn about the self-signed certificate; click through ("Advanced" -> "Proceed"). Demo data resets hourly.

![Demo](docs/publicize/demo.gif)

## Key features

- Log workouts as exercises + sets, with weight/reps per set
- Progressive overload trend graphs per exercise
- Track body measurements (weight, steps, calories) with weekly-average trends
- Email-verified registration, forgot/reset password flow
- No-signup demo account with guarded fields (can't be deleted or reconfigured) and a scheduled auto-reset

## Tech stack

Java 21, Spring Boot 4.1 (Web, Security, Data JPA, Validation, Mail, Thymeleaf), PostgreSQL, Flyway, Maven. Deployed on a self-managed EC2 instance behind a systemd service, with GitHub Actions handling build + deploy on push to `main`.

## Architecture decisions

**Auth:** Implemented my own authentication using Spring Security's DaoAuthenticationProvider, rather than a managed OAuth2 Provider, such as Auth0. For a project this size, the integration overhead of a third-party provider outweighs the benefit. Building it directly taught me how to configure my own custom UserDetailsService/PasswordEncoder implementations, how to issue verification tokens, and how to set up proper endpoint/method access control.

**Stateless JWT vs. form-based session login:** The app itself uses Spring Security's server-side session login, not JWTs — it's server-rendered Thymeleaf, so there's no client-side token to store or refresh, and sessions are the simpler fit with Spring Security's strong defaults out of the box. JWTs are used narrowly for email verification, where statelessness is actually the better option, as no database lookup is needed to validate. CSRF is left enabled, since this is a session-based Thymeleaf application, and turning it off risks malicious attacks to be run on the user's behalf.

**EC2 vs. a managed platform:** Runs on a single self-managed EC2 instance (systemd unit, TLS termination, GitHub Actions deploy pipeline) rather than a PaaS like Render. For a personal project, this allowed me to learn AWS fundamentals, including VPC, EC2, and RDS, instead of relying on a platform's abstractions.

## Local setup

```bash
docker-compose up -d                        # local Postgres
export JWT_SECRET=$(openssl rand -base64 32)
export SSL_KEYSTORE_PASSWORD=changeit        # matches the checked-in dev keystore
./mvnw spring-boot:run
```

Tests don't need any of the above — `./mvnw test` runs standalone.

See [DEPLOY.md](DEPLOY.md) for environment variables and EC2 deployment steps.
