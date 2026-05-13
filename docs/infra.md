# Infrastructure

## Local Development

The default local flow is:

1. Run MySQL with Docker Compose.
2. Run the Spring Boot application from IntelliJ or Gradle.
3. Open Swagger UI and verify the health endpoint.

```bash
docker compose -f docker-compose.local.yml up -d mysql
./gradlew bootRun
```

Use this when you want the fastest edit-run-debug loop from IntelliJ.

To run both the application and MySQL through Docker Compose:

```bash
./gradlew bootJar
docker compose -f docker-compose.local.yml --profile app up -d --build
```

Useful local URLs:

- API base URL: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`

## Local MySQL

The local MySQL container uses:

- Image: `mysql:8.4`
- Database: `sopkathon`
- User: `sopt`
- Password: `sopt`
- Host port: `13306`
- Container port: `3306`
- Time zone: UTC
- Charset: `utf8mb4`
- Collation: `utf8mb4_unicode_ci`

The data volume is named `mysql-local-data`.

Reset local database data:

```bash
docker compose -f docker-compose.local.yml down -v
docker compose -f docker-compose.local.yml up -d mysql
```

The default host port is `13306` to avoid conflicts with MySQL already installed on a developer laptop or another project using `3306`/`3307`. Inside Docker Compose, services still use port `3306`.

## Dev/Production Shape

The recommended AWS shape is:

- EC2: Spring Boot application container
- RDS MySQL: managed database
- S3: optional file storage through presigned URLs
- GitHub Actions: test/build automation

Do not run MySQL on the EC2 instance for the shared test server. RDS gives the team a managed database with easier backup, security group control, and fewer operational surprises during the HackaThon.

## Environment Variables

Copy `.env.example` to `.env` for local Docker Compose if needed. Never commit real `.env` files.

Required production variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_JWT_SECRET`
- `APP_CORS_ALLOWED_ORIGINS`
- `APP_SWAGGER_SERVER_URL`

`APP_JWT_SECRET` must be a long random value. Do not reuse the local example secret.
