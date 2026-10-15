# Black Hawk Security Solution — API

Spring Boot 4.1 · Java 25 · PostgreSQL 17 · Flyway · JWT. Backend for [blackhawkss.in](https://blackhawkss.in): public enquiry/contact/careers endpoints and the private admin API used by the Next.js `/admin` panel.

## Run locally

```bash
# 1. Postgres (once)
brew install postgresql@17 && brew services start postgresql@17
createuser -s blackhawkss 2>/dev/null; createdb -O blackhawkss blackhawkss
psql -d blackhawkss -c "ALTER USER blackhawkss PASSWORD 'blackhawkss';"

# 2. Run (dev profile is the default; JDK 25 is auto-provisioned by Gradle)
./gradlew bootRun
```

- API: http://localhost:8080 · Swagger UI: http://localhost:8080/swagger-ui.html (dev only)
- Dev admin login: `admin@blackhawkss.in` / `admin12345` (override with `ADMIN_SEED_EMAIL` / `ADMIN_SEED_PASSWORD`)
- Mail is **off** in dev (`MAIL_ENABLED=false`); enquiries are still saved.

## Test and build

```bash
./gradlew test        # H2 in PostgreSQL mode, no Postgres needed
./gradlew build       # jar in build/libs/
```

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/api/v1/public/enquiries` | – | Request a quote → `{ referenceCode }` |
| POST | `/api/v1/public/contact-messages` | – | Contact form |
| POST | `/api/v1/public/applications` | – | Careers application |
| GET | `/api/v1/public/gallery?category=` | – | Published gallery items |
| GET | `/api/v1/public/clients` | – | Organisations served |
| GET | `/api/v1/public/announcements` | – | Active announcements |
| POST | `/api/v1/auth/login` · `/refresh` · `/logout` | – | Admin session (JWT + rotating refresh token) |
| GET | `/api/v1/admin/dashboard` | ADMIN | Counts by status, this week, follow-ups due |
| GET/PATCH | `/api/v1/admin/enquiries[/{id}]`, POST `/{id}/notes` | ADMIN | Enquiry pipeline |
| GET/PATCH | `/api/v1/admin/applications[/{id}]` | ADMIN | Applications |
| GET/PATCH | `/api/v1/admin/messages`, `/{id}/read` | ADMIN | Contact messages |
| GET/POST/PATCH/DELETE | `/api/v1/admin/gallery[/{id}]` | ADMIN | Upload (multipart `file`, `title`, `category`) |
| GET/POST/PUT/DELETE | `/api/v1/admin/clients[/{id}]` | ADMIN | Client organisations |
| GET/POST/PUT/DELETE | `/api/v1/admin/announcements[/{id}]` | ADMIN | Announcements |
| GET | `/actuator/health` | – | Health |

All responses use the envelope `{ "success": true, "data": … }` or `{ "success": false, "error": { "code", "message", "fields?" } }`. Public POSTs are rate limited (5/IP/hour, 3/phone/day) and carry a honeypot field `website` that must stay empty.

## Configuration

See [`.env.example`](.env.example) for every variable. Production requires `JWT_SECRET`, `DB_*`, `ADMIN_SEED_*` and SMTP settings.

## Deploy

Dockerfile + `deploy/docker-compose.yml` (Postgres, API, Next.js, nginx, certbot) for a single Oracle Cloud ARM VM. Steps in [`deploy/README.md`](deploy/README.md).
