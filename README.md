# 🧾 README – Exam Project
This project is an exam assignment for 3rd semester Computer Science students at EK.  
The assignment is completed within 5 hours and submitted as a GitHub repository link to Wiseflow.  
After submission, no further changes are allowed to the code in the main branch.  
The student presents their solution in a 30-minute oral exam, where the code is discussed and further development will be requested.

## 📘 Project Overview
**Project Title:**  
Candidate Matcher Application

**Purpose:**  
A backend system for a recruitment platform that helps match candidates with relevant skills and technologies. Users can create, read, update, and delete candidates and skills, filter candidates by skill category, and view market insights on skill popularity and salary levels.

**Status:** The project covers all user stories and is fully functional.

**Technologies Used:**  
Java, Javalin, JPA/Hibernate, PostgreSQL, Maven, JWT, RestAssured, Docker

---

## 🏗️ Architecture & Design
| Layer | Description |
|-------|--------------|
| Entity Layer | Contains `Candidate` and `Skill` entities with JPA annotations and relationships. |
| DAO Layer | Responsible for CRUD operations on entities via Hibernate. `CandidateDAO` and `SkillDAO`. |
| DTO Layer | Data Transfer Objects (`CandidateDTO`, `SkillDTO`, `SkillStatsDTO`) for REST communication. |
| Service Layer | Contains business logic and maps between DAO and DTO, handles skill enrichment via external API. |
| Controller / Route Layer | `CandidateController` + Javalin Routes. REST endpoints are handled here. |
| Security Layer | JWT-based authentication and role-based authorization via `SecurityController`. |

---

## 🧩 Entity Relationships
| Entity | Relationship | Type | Cascade | FetchType | Direction | Description |
|---------|--------------|------|----------|------------|------------|-------------|
| Candidate | Skills | ManyToMany | No cascade | EAGER | Uni-directional | Each candidate can have many skills, but skills do not reference back. |
| Skill | Candidates | – | – | – | – | Not defined, uni-directional from Candidate. |

---

## 🚀 Deployment
The project is deployed and available online via a hosted Javalin server.  
The API can be tested via the following endpoint:  
https://examautumn.showmecode.dk/api/v1/auth/healthcheck

**Notes:**
- Connection settings can be changed via environment variables or `config.properties`.
- `hibernate.hbm2ddl.auto` is set to `create-drop` in development, `update` in production.

---

## 🚀 REST API Endpoints
### Authentication
| Method | Endpoint | Description | Auth Required |
|--------|-----------|-------------|----------------|
| POST | /auth/register | Create user | No |
| POST | /auth/login | Login and retrieve JWT | No |

### Core Endpoints
| Method | Endpoint | Description |
|--------|-----------|-------------|
| GET | /candidate | Retrieve all candidates |
| GET | /candidate/{id} | Retrieve candidate including skills |
| POST | /candidate | Create new candidate |
| PUT | /candidate/{id} | Update candidate |
| DELETE | /candidate/{id} | Delete candidate |
| PUT | /candidate/{candidateId}/skills/{skillId} | Link skill to candidate | Yes | ADMIN |
| GET | /candidate?category={category} | Filter candidates by skill category | Yes | USER/ADMIN |
| GET | /reports/candidates/top-by-popularity | Retrieve candidate with highest average popularity |

---

### Example JSON Responses

**GET /candidate/{id}**
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
**POST /candidate**
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
Used to enrich candidates’ skills with market data such as popularityScore and averageSalary.
This helps recruiters evaluate the value of candidates’ skills.

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
The project includes integration tests with JUnit 5 and RestAssured.
The purpose is to ensure that the API works correctly, from endpoints to database, including:

- CRUD operations for candidates and skills.
- JWT security and access control.
- Integration with external Skill Stats API (mocked in tests).

### Teststatus
- **GET /candidate** – returns all candidates ✅
- **GET /candidate/{id}** – returns candidate with skills, handles invalid ID ✅
- **POST /candidate** – creates candidate, validates input ✅
- **PUT /candidate/{id}** – updates candidate, matches path/body ID ✅
- **DELETE /candidate/{id}** – deletes candidate, handles non-existing ID ✅
- **PUT /candidate/{candidateId}/skills/{skillId}** – links skills ✅


### Running Tests
```bash
mvn clean test
```

## 🚀 Running Locally

To run the project locally on your machine, follow these steps:
### 🔧 Prerequisites
- Java 17 or newer
- Maven installed
- PostgreSQL running locally or via Docker
- `config.properties` file loacted in both `src/main/resources` and `src/test/resources`

### ⚙️ Setting up `config.properties`
Create a file named `config.properties` in both `src/main/resources` and `src/test/resources` with the following content:

```properties
DEPLOYED=true
DB_NAME= <dbname> 
DB_USERNAME=postgres
DB_PASSWORD= <password>
CONNECTION_STR= jdbc:postgresql://db:5432/
SECRET_KEY= <secret key>
ISSUER= A-St0rM
TOKEN_EXPIRE_TIME=18000
```
### ️ Opret lokal database

Start PostgreSQL.

Opret en database med navnet fra DB_NAME:

```SQL
CREATE DATABASE dbname;
```
🏗️ Build and Start the Project

Clone repository:
```linux
git clone <din-repo-url>
cd <repo-mappen>
```

Build the project with Maven:
```Maven
mvn clean install
```

#### Start the Server:

### Verify the Server is Running:

```text
http://localhost:7007/api/v1/auth/healthcheck
```
