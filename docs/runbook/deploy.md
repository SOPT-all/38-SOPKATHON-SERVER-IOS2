# Deployment Runbook

## Target

This runbook assumes a simple HackaThon deployment:

- One EC2 instance runs the Spring Boot app container.
- RDS MySQL stores application data.
- The app exposes port `8080`.
- `/actuator/health` is used for health checks.

## Build

Build and test locally or in CI:

```bash
./gradlew clean test bootJar
docker build -t sopkathon:latest .
```

## EC2 First-Time Setup

Install Docker and the Docker Compose plugin on EC2. Then prepare an app directory:

```bash
mkdir -p ~/sopkathon
cd ~/sopkathon
```

Place these files in that directory:

- `docker-compose.prod.yml`
- `.env`

The `.env` file must contain production values for RDS and JWT secrets.

## Deploy

If the image is built on EC2:

```bash
./gradlew clean test bootJar
docker build -t sopkathon:latest .
docker compose -f docker-compose.prod.yml --env-file .env up -d
```

If the image is pulled from a registry:

```bash
docker compose -f docker-compose.prod.yml --env-file .env pull
docker compose -f docker-compose.prod.yml --env-file .env up -d
```

## Verify

Check container status:

```bash
docker compose -f docker-compose.prod.yml --env-file .env ps
```

Check logs:

```bash
docker compose -f docker-compose.prod.yml --env-file .env logs -f app
```

Check health:

```bash
curl -fsS http://localhost:8080/actuator/health
```

Expected response:

```json
{"status":"UP"}
```

## Rollback

Keep the previous image tag before deploy:

```bash
docker tag sopkathon:latest sopkathon:previous
```

Rollback:

```bash
APP_IMAGE=sopkathon:previous docker compose -f docker-compose.prod.yml --env-file .env up -d
curl -fsS http://localhost:8080/actuator/health
```

## Common Failure Checks

If the app cannot connect to RDS:

- Confirm EC2 security group can reach the RDS security group on port `3306`.
- Confirm RDS is publicly inaccessible unless there is a deliberate reason.
- Confirm `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
- Confirm the database exists and Flyway can create tables.

If the client cannot call the server:

- Confirm EC2 inbound rules allow the selected HTTP port.
- Confirm `APP_CORS_ALLOWED_ORIGINS` includes the web client origin.
- Confirm the client uses `Authorization: Bearer <token>` for protected APIs.
