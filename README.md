<div style="text-align: center;">
  <img src="docs/logo.png" alt="Job Fit Analyzer logo" width="96" />
</div>

# Job Fit Analyzer

Job Fit Analyzer is an AI-powered, LLM-based job fit analysis platform that evaluates job postings against a candidate profile and highlights how well a role matches the target background.

The project was built as a practical backend-oriented MVP to demonstrate production-style Java development, Spring Boot architecture, CI/CD, Dockerization, database persistence, and external API integration.

## Why this project exists

I built this application as part of my job search strategy to showcase the skills and keywords commonly expected for Senior Java / Spring Boot roles, especially in Switzerland and other European markets.

The goal was not only to create a functional app but also to demonstrate:
- clean Spring Boot architecture,
- persistence with Spring Data JPA,
- server-side rendered UI with Thymeleaf,
- integration with external AI services,
- observability-friendly logging,
- containerized deployment,
- automated tests and CI pipeline.

## Key features

- Create, update, view, and filter job postings
- Analyze a job posting against a candidate profile
- Store analysis results in PostgreSQL
- Bulk update job status by filter
- Server-side rendered UI with Thymeleaf
- Centralized error handling with custom error pages
- Correlation ID logging for request tracing
- Docker and Docker Compose support
- GitHub Actions CI pipeline
- SonarCloud analysis
- Unit and MVC tests

## Tech stack

- Java 21
- Spring Boot
- Spring MVC
- Spring Data JPA
- Spring Validation
- Thymeleaf
- PostgreSQL
- H2 for tests
- Flyway
- Docker
- Docker Compose
- GitHub Actions
- SonarCloud
- Lombok
- Maven

## Architecture highlights

This project follows a layered architecture:

- **controller** — web layer and UI endpoints
- **service** — business logic and orchestration
- **repository** — persistence layer
- **domain** — entities and domain exceptions
- **dto** — request/response models
- **ai** — OpenAI integration client
- **config** — application configuration and request correlation support

### Design decisions
- Centralized exception handling with `@ControllerAdvice`
- Transactional service layer for consistent persistence
- Dedicated `OpenAiClient` for external API integration
- `CorrelationIdFilter` for request tracing across logs
- `OpenAiProperties` for typed configuration
- Test coverage for controllers and services

## Screenshots

_Add screenshots here if available._

## Run locally

### Prerequisites
- Java 21
- Maven
- PostgreSQL (optional if using Docker Compose)

### Run with Maven

```bash
./mvnw spring-boot:run
```


### Run tests
```bash
./mvnw test
```

### Build the project
```bash
./mvnw clean package
```

## Run with Docker

### Build the image

```bash
docker build -t job-fit-analyzer .
```

### Run with Docker Compose
```bash
docker compose up --build
```


The application uses PostgreSQL and requires the OpenAI API key to be provided through environment variables.

## Configuration

### Required environment variables
- `OPENAI_API_KEY` — OpenAI API key

### Application profiles
- `dev`
- `postgres`

## CI/CD

The project includes a GitHub Actions workflow that:
- builds the application with Maven,
- runs tests,
- performs SonarCloud analysis,
- uploads the built JAR artifact.

## Testing

The project includes:
- service layer unit tests
- controller MVC tests
- application context test

## Future improvements

Planned next steps:
- pagination for the job list
- better structured logging and metrics
- cloud deployment
- authentication and authorization
- more robust OpenAI error handling
- richer analytics for job/candidate fit scoring

## License

This project is intended as a personal portfolio and demo project.