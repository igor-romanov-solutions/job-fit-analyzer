# JobFitAnalyzer

## Description

JobFitAnalyzer is a Spring Boot application that analyzes how well a candidate profile matches a job description using an LLM and stores structured analysis results.

The application allows users to:

* Submit a candidate profile and job description
* Analyze the match using an LLM (OpenAI)
* Store structured analysis results in a database
* View analysis history
* View detailed analysis results

---

## Tech Stack

* Java 21
* Spring Boot
* Spring MVC
* Spring Data JPA (Hibernate)
* H2 Database
* Thymeleaf
* OpenAI API
* Maven
* Lombok
* Bootstrap

---

## Features

* Submit candidate profile and job description
* Analyze match using LLM
* Store analysis results in database
* View analysis history
* View detailed analysis results

---

## Architecture

The application follows a layered architecture:

* **Controller layer** – handles HTTP requests and Thymeleaf views
* **Service layer** – contains business logic and orchestration
* **Repository layer** – data access via Spring Data JPA
* **Entity layer** – database model
* **DTO layer** – request/response/view models
* **Mapper layer** – converts between DTO and Entity
* **Client layer** – OpenAI API integration

### Application Flow

Controller → Service → OpenAI Client → Mapper → Repository → Database

---

## How to Run

Build the project:

```bash
mvn clean package
```

Run the application:

```bash
java -jar target/jobfitanalyzer-0.0.1-SNAPSHOT.jar
```

Open in browser:

```
http://localhost:8080
```

H2 Console:

```
http://localhost:8080/h2-console
```

JDBC URL:

```
jdbc:h2:file:./data/jobfitdb
```

---

## Environment Variables

### Local development

| Variable | Description | Required |
|----------|-------------|----------|
| OPENAI_API_KEY | API key for OpenAI API | Required for OpenAI integration |

### Docker Compose

| Variable | Description | Required |
|----------|-------------|----------|
| OPENAI_API_KEY | API key for OpenAI API | Required |
| SPRING_DATASOURCE_URL | PostgreSQL JDBC URL | Required for Docker |
| SPRING_DATASOURCE_USERNAME | Database username | Required for Docker |
| SPRING_DATASOURCE_PASSWORD | Database password | Required for Docker |

### How to set environment variable

**Mac / Linux:**

```bash
export OPENAI_API_KEY=your_api_key_here
```

**Windows (PowerShell):**

```powershell
setx OPENAI_API_KEY "your_api_key_here"
```

After setting the variable, restart the application.

If the variable is not set, OpenAI integration will not work.


---

## Docker / Docker Compose

The project includes a Docker Compose setup for running the Spring Boot application with PostgreSQL.

### Run with Docker Compose

```bash
docker compose up --build
```

This will start:

- `app` — Spring Boot application
- `postgres` — PostgreSQL database

### URLs

- Application: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

---

## Roadmap

* [x] OpenAI integration
* [x] Basic Thymeleaf UI
* [x] H2 + JPA persistence
* [x] Analysis history and details
* [x] Unit tests
* [x] Spring MVC tests
* [x] GitHub Actions CI
* [x] SonarCloud
* [x] PostgreSQL
* [x] Flyway migrations
* [x] Docker / Docker Compose
* [ ] AWS deployment
