# 🧾 README – Exam Project

## 📘 Project Overview
**Project Title:**  
**Purpose:**  
**Technologies Used:**  
(Java, Javalin, JPA/Hibernate, PostgreSQL, Docker, etc.)

---

## 🏗️ Architecture & Design
| Layer | Description |
|-------|--------------|
| Entity Layer |  |
| DAO Layer |  |
| DTO Layer |  |
| Service Layer |  |
| Controller / Route Layer |  |
| Security Layer |  |

---

## 🧩 Entity Relationships
| Entity | Relationship | Type | Cascade | FetchType | Direction | Description |
|---------|--------------|------|----------|------------|------------|-------------|
|  |  |  |  |  |  |  |

---

## 📦 Database Configuration
| Environment | JDBC URL | Username | Password | DDL Strategy |
|--------------|-----------|-----------|-----------|---------------|
| Development |  |  |  |  |
| Production / Deployment |  |  |  |  |

**Notes:**
- Connection settings are defined via environment variables or `config.properties`.
- `hibernate.hbm2ddl.auto` can be set to `create`, `update`, or `validate` depending on environment.

---

## 🚀 REST API Endpoints
### Authentication
| Method | Endpoint | Description | Auth Required |
|---------|-----------|-------------|----------------|
|  |  |  |  |

### Core Endpoints
| Method | Endpoint | Description | Auth | Role |
|---------|-----------|-------------|------|------|
|  |  |  |  |  |

---

## 🌍 External Integration
**External API Used:**  
`<API Base URL>`

**Purpose:**  
(Describe how your system integrates with the external service.)

**Example Response Structure:**
```json
{
  "items": [
    {
      "name": "",
      "weightInGrams": 0,
      "quantity": 0,
      "description": "",
      "category": "",
      "createdAt": "",
      "updatedAt": "",
      "buyingOptions": []
    }
  ]
}
```

## 🧪 Testing

Projektet indeholder **integrationstests** skrevet med **JUnit 5** og **RestAssured** for at sikre, at hele applikationen fungerer som forventet – fra API-lag til database.

### 🎯 Formål
Formålet med testene er at:
- Verificere at **REST-endpoints** returnerer de korrekte HTTP-statuskoder og JSON-data.
- Sikre at **datahåndtering via Hibernate/JPA** fungerer korrekt (CRUD-operationer).
- Bekræfte at **validering og fejlbeskeder** håndteres korrekt.
- Teste **sikkerhed (JWT-login)** og adgangsbegrænsning på beskyttede endpoints.

### 🧩 Typer af tests
- **Integrationstests:**  
  Hele applikationen startes på en testserver, hvor API’et testes med rigtige databasekald og DTO’er.  
  Her bruges en **in-memory testdatabase** (via `HibernateConfig.setTest(true)`).
  
- **End-to-end API tests:**  
  Bruger **RestAssured** til at sende HTTP-requests til API’et og validere svar som statuskoder, felter og struktur.

- **Fake ekstern API:**  
  En fiktiv “Packing API Client” bruges i tests, så systemet kan testes uden internetforbindelse eller eksterne afhængigheder.

### 🧱 Testopbygning
Hver test:
1. Starter serveren på en testport.  
2. Seeder databasen med testdata (via `Populator`-klasse).  
3. Udfører forskellige HTTP-kald (GET, POST, PUT, DELETE).  
4. Validerer respons og ændringer i databasen.

### ▶️ Kørsel af tests
```bash
  mvn clean test
```
