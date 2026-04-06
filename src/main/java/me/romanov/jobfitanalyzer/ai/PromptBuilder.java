package me.romanov.jobfitanalyzer.ai;

public class PromptBuilder {
    public static String buildSystemPrompt() {
        return """
                You are an information extraction and evaluation system.
                You always return strict valid JSON and nothing else.
                You do not add explanations, markdown, or any text outside JSON.
            """;
    }

    public static String buildUserPrompt(String cvText, String vacancyText) {
        return """
            TASK:
            Analyze the Job Description and the Candidate CV.

            Return a structured JSON response describing:
            - role type
            - required technology stack
            - seniority level
            - domain
            - language requirements
            - potential concerns (risk factors why the candidate may not be invited to an interview)

            OUTPUT FORMAT:
            Return only valid JSON with exactly these fields:

            {
              "roleType": "Java Backend | Frontend | Fullstack | DevOps | Platform | Cloud | Data | Unknown",
              "javaRelevance": "High | Medium | Low | Unknown",
              "primaryStack": ["string"],
              "secondaryStack": ["string"],
              "niceToHaveStack": ["string"],
              "seniorityLevel": "Junior | Middle | Senior | Lead | Unknown",
              "domain": "string",
              "vacancyLanguage": "string",
              "requiredGermanLevel": "A1 | A2 | B1 | B2 | C1 | C2 | Native | Not specified | Unknown",
              "riskLevel": "Low | Medium | High",
              "gaps": ["string"]
            }

            IMPORTANT SOURCE RULES:
            - primaryStack, secondaryStack, niceToHaveStack, seniorityLevel, domain, vacancyLanguage, requiredGermanLevel must be based only on the Job Description.
            - Never use information from the CV to fill these fields.
            - gaps must be based on comparing the Candidate CV with the Job Description.
            - riskLevel must be based on how well the CV matches the required stack and requirements.
            - javaRelevance is how relevant this vacancy is for a Java developer.

            JOB DESCRIPTION STRUCTURE RULES:
            If the job description contains sections like:
            - Responsibilities
            - Qualifications
            - Essential Skills
            - Required Skills
            - Desired Skills
            - Nice to have

            Then classify technologies as:

            primaryStack:
            Technologies from "Essential Skills", "Required", or core technologies central to the role.

            secondaryStack:
            Technologies from "Qualifications" and supporting technologies.

            niceToHaveStack:
            Technologies from "Desired Skills", "Nice to have", or optional skills.

            Do NOT classify technologies from "Desired Skills" as primaryStack.

            ROLE TYPE RULES:
            Determine the role type based on the job description:

            - Java Backend → Java, Spring, REST, Hibernate, backend services
            - Frontend → Angular, React, UI, frontend development
            - Fullstack → mix of backend + frontend
            - DevOps → CI/CD, Jenkins, Docker, infrastructure, pipelines
            - Platform → infrastructure, toolchain, cloud, automation, platform engineering
            - Cloud → AWS, Azure, GCP, infrastructure
            - Data → data engineering, ETL, analytics
            
            JAVA RELEVANCE RULES:
            Evaluate how suitable this vacancy is for a Java backend developer.
            
            High:
            - Java is a core technology of the role
            - The role is Java Backend or Java Fullstack
            - Java/Spring is central to daily work
            
            Medium:
            - Java is used but not the main focus
            - Role is Platform, DevOps, or Cloud but includes some Java/Spring services
            - Java is one of several technologies
            
            Low:
            - Java is not mentioned or is not relevant
            - Role is primarily Frontend, DevOps, Data, or Cloud with no Java backend work
            
            Important:
            If the roleType is DevOps, Platform, or Cloud, javaRelevance cannot be "High".
            In this case it should be "Medium" or "Low".

            GERMAN LEVEL DECISION RULES:
            
            1. If CEFR level (A1–C2) is explicitly mentioned → use it. (Source: EXPLICIT)
 
            2. If qualitative description is used:
               - Grundkenntnisse → A2
               - Gute Deutschkenntnisse → B1
               - Sehr gute Deutschkenntnisse → B2
               - Fliessend → B2 or C1 depending on responsibilities
               - Muttersprache → C2
               (Source: MAPPED)
 
            3. If German is marked as "nice to have", "advantage", "plus", "optional" → B1. (Source: INFERRED)
 
            4. If the job description is written in German → German is required.
 
               Then determine level based on responsibilities:
 
               B2:
               - Internal team communication
               - Developer role
               - No customer contact
               - No presentations
               - No leadership
 
               C1:
               - Customer communication
               - Workshops
               - Business stakeholders
               - Consulting
               - Writing documentation
               - Lead role
 
               C2:
               - Management presentations
               - Negotiations
               - Sales / pre-sales
               - Public speaking
               (Source: INFERRED)
 
            5. If German is mentioned but level unclear → "Unknown".
 
            6. If German is not mentioned and vacancy is in English → "Not specified".

            GAPS / POTENTIAL CONCERNS RULES:
            Describe potential concerns from a recruiter's perspective.
            These are reasons why the candidate might not be invited to an interview.

            Only include:
            - Missing required technologies
            - Missing required cloud/devops skills
            - Missing required domain experience
            - Language level lower than required
            - Seniority mismatch
            - Role mismatch (e.g., Java developer applying for DevOps role)

            Do NOT include:
            - Soft skills
            - Personality traits
            - Team size experience
            - Generic phrases

            Write gaps as neutral professional statements.

            EXAMPLE OUTPUT:

            {
              "roleType": "Platform",
              "javaRelevance": "Medium",
              "primaryStack": ["AWS", "CI/CD", "Jenkins", "Linux"],
              "secondaryStack": ["Spring Boot", "SQL", "TypeScript"],
              "niceToHaveStack": ["Docker", "Python"],
              "seniorityLevel": "Senior",
              "domain": "Rail / Transportation",
              "vacancyLanguage": "German",
              "requiredGermanLevel": "B2",
              "riskLevel": "Medium",
              "gaps": [
                "Limited experience with AWS infrastructure mentioned in the CV.",
                "No strong Linux engineering experience mentioned.",
                "Role is more Platform/DevOps oriented than pure Java backend."
              ]
            }
            
            Candidate CV:
            ----------------
            %s
            ----------------
            
            Job Description:
            ----------------
            %s
            ----------------
            """.formatted(cvText, vacancyText);
    }
}
