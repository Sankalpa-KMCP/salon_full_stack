# Velvet Salon

Velvet Salon is a modern, luxurious booking website intended for a single salon, allowing customers to seamlessly book appointments for services like haircuts, coloring, facials, and nail treatments.

## Planned Structure
The project will be structured as a monorepo containing two main parts:
*   `frontend/` - The customer-facing web application and booking interface.
*   `backend/` - The REST API providing booking logic, availability checks, and administrative endpoints.

## Tech Stack (Planned)
*   **Frontend**: Next.js, TypeScript, Tailwind CSS
*   **Backend**: Java 21, Spring Boot, Maven
*   **Database**: PostgreSQL on Supabase
*   **Authentication**: Spring Security + JWT (Admin only)
*   **Storage**: Supabase Storage

## Recommended Portfolio Deployment
*   **Frontend**: Vercel (Automatic Next.js optimization)
*   **Backend**: Render or Railway (Docker container hosting)
*   **Database**: Neon or Supabase (Managed PostgreSQL)

## Deployment Steps
1. **Database**: Provision a managed PostgreSQL instance and note the connection credentials.
2. **Backend (PaaS)**: Deploy the `backend/` directory using the provided `Dockerfile`.
   Set the following environment variables:
   * `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
   * `PORT` (Provided by PaaS)
   * `APP_CORS_ALLOWED_ORIGINS` (Exact frontend URL, e.g. `https://velvet-salon.vercel.app`. Do not use wildcard `*` in production).
   * `APP_SEED_ENABLED` (Keep `false`. Turn to `true` temporarily to seed initial data).
3. **Frontend (Vercel)**: Deploy the `frontend/` directory.
   Set environment variable:
   * `NEXT_PUBLIC_API_BASE_URL` (Exact backend URL, e.g. `https://velvet-salon-api.onrender.com`).

## Production Warning
*   **Database Migrations**: Flyway runs automatically on backend boot.
*   **SSL**: Managed PostgreSQL might require SSL adjustments based on the provider.
*   **Cold Starts**: Free PaaS tiers sleep when inactive. The first request may take up to 30-50s to wake the backend.
*   **Testing**: Clean up any test bookings to avoid cluttering the production database.

## Production Smoke-Test Routes
Verified against code routes:
- **Backend Health**: `GET /actuator/health`
- **Catalog**: `GET /api/catalog/services`, `GET /api/catalog/staff`
- **Frontend App**:
  - `GET /` (Homepage loads, hero video loop plays/falls back)
  - `GET /booking` (Booking UI loads, staff/services fetch successfully via `GET /api/booking/availability...`)
  - **Booking Creation**: Submit appointment via UI `POST /api/booking` -> note Cancellation Token.
  - **Token Verification**: Submit token on `/booking/manage` -> `GET /api/booking/{cancellationToken}`.
  - **Cancellation**: Cancel via UI -> `DELETE /api/booking/{cancellationToken}`.
  - **CORS Validation**: Cross-origin requests from frontend domain to backend succeed.
