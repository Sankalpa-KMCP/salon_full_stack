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

## Development Setup Notes
*   **Current Status**: Foundation preparation. The frontend and backend applications have not yet been scaffolded.
*   **Java Compatibility Note**: The backend target is **Java 21**. A local development environment check detected **Java 24** installed. A compatibility check must be performed before beginning backend scaffolding (Phase 3) to ensure Maven and Spring Boot target Java 21 correctly.
