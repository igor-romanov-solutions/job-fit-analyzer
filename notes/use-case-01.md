# Use Case 1 — Analyze CV vs Job Description

## User flow
- The user has two fields:
    - CV
    - Job Description
- The user inputs text into both fields
- The user presses the "Analyze" button
- The request is sent to the LLM
- The LLM returns a response in JSON format
- The response is displayed in the UI

## Response structure (v1)
The UI should display the following fields:

- javaRelevance
- primaryStack
- secondaryStack
- niceToHaveStack
- seniorityLevel
- domain
- vacancyLanguage
- requiredGermanLevel
- gaps

```json
{
    "javaRelevance":"High",
    "primaryStack": ["Java", "Spring Boot", "Hibernate"],
    "secondaryStack": ["Docker", "AWS"],
    "niceToHaveStack": ["Kubernetes"],
    "seniorityLevel": "Senior",
    "domain": "Finance",
    "vacancyLanguage": "English",
    "requiredGermanLevel": "B2",
    "gaps": [
      "German level is not mentioned in the CV",
      "No recent AWS project experience"
    ]
}
```

## Done when
User can:
- paste CV
- paste Job Description
- click Analyze
- see structured analysis on the screen

## Open questions (for later blocks)
- What are the possible values for the German level?
- How to design the prompt to extract German level correctly?
