# 🧾 README – Exam Project

## 📘 Project Overview
**Project Title:**  
Candidate Matcher Application

**Purpose:**  
Et backend-system til en rekrutteringsplatform, der hjælper med at matche kandidater med relevante færdigheder og teknologier. Brugere kan oprette, læse, opdatere og slette kandidater og skills, filtrere kandidater efter skill-kategori, og se markedets indsigt i skill-popularitet og lønniveauer.

**Technologies Used:**  
Java, Javalin, JPA/Hibernate, PostgreSQL, Maven, JWT, RestAssured, Docker

---

## 🏗️ Architecture & Design
| Layer | Description |
|-------|--------------|
| Entity Layer | Indeholder `Candidate` og `Skill` entiteter med JPA-annotationer og relationer. |
| DAO Layer | Ansvarlig for CRUD-operationer på entiteter via Hibernate. `CandidateDAO` og `SkillDAO`. |
| DTO Layer | Data Transfer Objects (`CandidateDTO`, `SkillDTO`, `SkillStatsDTO`) til REST-kommunikation. |
| Service Layer | Indeholder forretningslogik og mapper mellem DAO og DTO, håndterer skill enrichment via ekstern API. |
| Controller / Route Layer | `CandidateController` + Javalin Routes. REST-endpoints håndteres her. |
| Security Layer | JWT-baseret authentication og role-based authorization via `SecurityController`. |

---

## 🧩 Entity Relationships
| Entity | Relationship | Type | Cascade | FetchType | Direction | Description |
|---------|--------------|------|----------|------------|------------|-------------|
| Candidate | Skills | ManyToMany | Ingen cascade | EAGER | Uni-directional | Hver kandidat kan have mange skills, men skills peger ikke tilbage. |
| Skill | Candidates | – | – | – | – | Ikke defineret, uni-directional fra Candidate. |

---

## 🚀 Deployment

Projektet er deployeret og tilgængeligt online via en hostet Javalin-server.  
API’et kan testes via følgende endpoint:  
https://examautumn.showmecode.dk/api/v1/auth/healthcheck

**Notes:**
- Connection settings kan ændres via miljøvariabler eller `config.properties`.
- `hibernate.hbm2ddl.auto` er sat til `create-drop` i udvikling, `update` i produktion.

---

## 🚀 REST API Endpoints
### Authentication
| Method | Endpoint | Description | Auth Required |
|--------|---------|-------------|----------------|
| POST | /auth/register | Opret bruger | Nej |
| POST | /auth/login | Login og hent JWT | Nej |

### Core Endpoints
| Method | Endpoint | Description |
|--------|---------|-------------|
| GET | /candidate | Hent alle kandidater 
| GET | /candidate/{id} | Hent kandidat inkl. skills 
| POST | /candidate | Opret ny kandidat 
| PUT | /candidate/{id} | Opdater kandidat 
| DELETE | /candidate/{id} | Slet kandidat 
| PUT | /candidate/{candidateId}/skills/{skillId} | Link skill til kandidat | Ja | ADMIN 
| GET | /candidate?category={category} | Filtrer kandidater på skill category | Ja | USER/ADMIN 
| GET | /reports/candidates/top-by-popularity | Hent kandidat med højeste gennemsnitlige popularitet 

---
### Example JSON Responses

**GET /candidates/{id}**
```json
{
  "id": 1,
  "name": "Alice",
  "phone": "12345678",
  "educationBackground": "Computer Science",
  "skills": [
    {
      "id": 1,
      "name": "Java",
      "slug": "java",
      "category": "PROG_LANG",
      "description": "General-purpose programming language",
      "popularityScore": 80,
      "averageSalary": 100000
    }
  ]
}
```
**POST /candidates**
```json
{
"name": "Charlie",
"phone": "87654321",
"educationBackground": "Computer Science"
}
```

**GET /reports/candidates/top-by-popularity**
```json
{
  "candidateId": 1,
  "averagePopularityScore": 80.0
}
```


## 🌍 External Integration
**External API Used:**  
`https://apiprovider.cphbusinessapps.dk/api/v1/skills/stats`

**Purpose:**  
Bruges til at berige kandidaters skills med markedsdata som `popularityScore` og `averageSalary`. Dette hjælper rekrutterere med at vurdere værdien af kandidaters færdigheder.

**Example Response Structure:**
```json
{
  "data": [
    {
      "id": "0a1",
      "slug": "java",
      "name": "Java",
      "categoryKey": "prog-lang",
      "description": "General-purpose, strongly-typed language for backend and Android.",
      "popularityScore": 93,
      "averageSalary": 120000,
      "updatedAt": "2025-10-01T10:15:00.000Z"
    }
  ]
}
```

## 🧪 Testing

Projektet indeholder **integrationstests** med **JUnit 5** og **RestAssured**. Formålet er at sikre, at API’et fungerer korrekt, fra endpoints til database, inklusiv:

- CRUD-operationer for kandidater og skills.
- JWT-sikkerhed og adgangskontrol.
- Integration med ekstern Skill Stats API (mocket i tests).

### Teststatus
- **GET /candidate** – returnerer alle kandidater ✅
- **GET /candidate/{id}** – returnerer kandidat med skills, håndterer invalid ID ✅
- **POST /candidate** – opretter kandidat, validerer input ✅
- **PUT /candidate/{id}** – opdaterer kandidat, matcher path/body ID ✅
- **DELETE /candidate/{id}** – sletter kandidat, håndterer ikke-eksisterende ID ✅
- **PUT /candidate/{candidateId}/skills/{skillId}** – linker skills ✅

### Kørsel af tests
```bash
mvn clean test
```
