# Job Fit Analyzer

AI-assisted web application that analyzes a candidate CV against a job description and evaluates the match.

## Features
- CV vs Job Description analysis
- Role type detection
- Technology stack classification
- German level detection
- Interview risk analysis
- Web UI

## Tech Stack
- Java 17
- Spring Boot
- Thymeleaf
- Bootstrap
- OpenAI API
- Maven

## Architecture
Controller → Service → OpenAI Client → LLM → JSON → Normalizer → UI

## Run locally
mvn spring-boot:run
Open http://localhost:8080

## Roadmap
- [ ] Save results to PostgreSQL
- [ ] Analysis history
- [ ] Docker
- [ ] Deployment