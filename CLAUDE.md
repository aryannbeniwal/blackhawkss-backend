# CLAUDE.md — blackhawkss-backend

Spring Boot 4.1.1 on Java 25 (Gradle Kotlin DSL, JDK auto-provisioned via foojay). Backend for the Black Hawk Security Solution website; the Next.js frontend lives in the sibling repo `blackhawkss-frontend`.

## Commands
- `./gradlew bootRun` — dev profile (local Postgres `blackhawkss`/`blackhawkss`, mail off, Swagger on)
- `./gradlew test` — H2 in PostgreSQL mode, Flyway runs, no external services
- `./gradlew build` — jar in `build/libs/`

## Layout (`in.blackhawkss.api`)
`web/rest/{pub,auth,admin}` controllers → `service` → `repository` → `domain` (JPA entities). DTOs are records in `dto/request` and `dto/response` with Jakarta validation. `config` (AppProperties `app.*`, SecurityConfig, WebConfig), `security` (JwtService, JwtAuthFilter, RateLimitFilter), `exception` (GlobalExceptionHandler → envelope), `util`.

## Conventions
- Every response is `ApiResponse<T>`: `{success, data}` or `{success:false, error:{code,message,fields}}`. Error codes: VALIDATION_ERROR, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, RATE_LIMITED, INTERNAL_ERROR.
- Enums are stored as VARCHAR; migrations must stay H2-compatible (BIGSERIAL, TIMESTAMP, TEXT, no Postgres-only types). Never edit an applied migration; add `V{n}__*.sql`.
- Phones are normalised to 10 digits (`PhoneNumbers`). Public POSTs have a honeypot `website` field.
- Mail is async and never fails the request; enquiry gets `notifyFailed=true` instead.
- Thymeleaf imports are `org.thymeleaf.*`; Boot 4 test MockMvc annotation is `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`.
- Do not add claims to templates that are not verified business facts (see the frontend `content/site.ts`).

## Env
Documented in `.env.example`. Prod fails fast without a ≥32-byte `JWT_SECRET`.
