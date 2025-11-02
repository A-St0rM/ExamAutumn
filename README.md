# Hotel API – Javalin & Hibernate
## 🏨 Overview

This project implements a simple RESTful API for managing hotels and their rooms. The API was built as part of a third–semester backend exercise and demonstrates how to combine Javalin 6 for HTTP routing, Hibernate for persistence, PostgreSQL for the database, and JWT‑based authentication for securing endpoints. The service exposes CRUD operations for Hotel and Room entities, maps database entities to DTOs, performs integration tests using JUnit 5, Rest Assured and Testcontainers, and logs requests and responses through SLF4J/Logback.

## 🔧 Technologies & Dependencies

Projektet benytter følgende teknologier og libraries:  

- **Java 17 / Maven** – skrevet i Java 17 og build styres med Maven.  
- **Javalin 6.5** – lightweight web framework til at definere routes og controllers.  
- **Hibernate 6 & JPA** – ORM-lag konfigureret gennem `HibernateConfig` til Postgres og Testcontainers.  
- **PostgreSQL** – primær database (default: `hotel`).  
- **Lombok** – reducerer boilerplate i DTOs og entities.  
- **Jackson** – serialization/deserialization af JSON payloads.  
- **SLF4J + Logback** – logging til console og fil med konfigurerbare log-niveauer.  
- **JWT (TokenSecurity)** – genererer og validerer tokens til autentificering af beskyttede endpoints.  
- **JUnit 5, Rest Assured, Hamcrest** – integrationstests af DAO-metoder og REST endpoints.  
- **Testcontainers** – starter en midlertidig Postgres container til tests, så produktionsdata aldrig berøres.  

📦 Alle dependencies er defineret i projektets **`pom.xml`**.  

## 🗂 Project Structure

Koden er organiseret i følgende pakker under `src/main/java/app`:

- **config** –  
  Indeholder konfiguration.  
  - `ApplicationConfig`: starter Javalin og registrerer routes.  
  - `HibernateConfig`: sætter `EntityManagerFactory` op for udvikling eller tests.  

- **routes** –  
  Samler alle endpoints.  
  - `Route`: definerer base path `/api/v1` og binder hotel- og security-ruter.  
  - `HotelRoute`: CRUD endpoints for hoteller og rooms.  
  - `SecurityRoute`: eksponerer `/auth/login` og `/auth/register`.  

- **controllers** –  
  Implementerer request-handlers.  
  - `HotelController` & `RoomController`: håndterer CRUD med DAOs og DTO-mapping.  
  - `SecurityController`: håndterer register/login, token-skabelse og autorisation.  

- **DAO / entities** –  
  Data Access Objects til persistence af `Hotel` og `Room`, implementerer `IDAO`.  
  Entities: `Hotel`, `Room`, `User`, `Role` – alle JPA-annoterede og mappet til tabeller.  

- **DTO / mapper** –  
  DTO-klasser til sikre JSON-repræsentationer.  
  Mapper-klasser oversætter mellem entities og DTOs.  

- **services** –  
  Forretningslogik:  
  - Password hashing, rolle-tildeling, token generering/validering.  
  - `ApiService` viser eksempel på eksternt API-kald.  

- **exceptions** –  
  Custom runtime exceptions (f.eks. `ValidationException`, `EntityNotFoundException`).  

- **utils** –  
  Hjælpeklasser: fx konfigureret `ObjectMapper` og property reader.  

- **security** –  
  Interfaces og DAO for user/role management. Indeholder JWT-filter til token-validering og role-check.  

- **test** –  
  Integrationstests i `src/test/java` med **Testcontainers**, der tester DAO-lag og API-endpoints.  

---

## 📖 Entities & DTOs

| Entity / DTO | Key fields                    | Notes                                                                 |
|--------------|-------------------------------|----------------------------------------------------------------------|
| **Hotel**    | `id`, `name`, `address`, `rooms` | Hvert hotel kan have mange rooms. DTO bruger felterne `hotelName` og `hotelAddress`. |
| **Room**     | `id`, `number`, `price`, `hotel` | Hvert room tilhører et hotel. DTO eksponerer `hotelId` sammen med `number` og `price`. |
| **User**     | `username`, `password (hashed)`, `roles` | Passwords hashes med **BCrypt**.                                     |
| **Role**     | `rolename`                    | Many-to-many relation med `User`. Default rolle ved register er `USER`. |

DTO-klasser findes i pakken `app.DTO` og mappes til entities gennem `HotelMapper` og `RoomMapper`.  
Dette gør det muligt kun at returnere de nødvendige felter ud til klienten og skjule interne relationer.  

---


## 🚦 API Endpoints

Alle endpoints er tilgængelige under base path: **`/api/v1`**.  
- `HotelRoute` håndterer hotel- og room-relaterede ruter.  
- `SecurityRoute` håndterer autentificering.  
- Ruter under **`/api/v1/protected`** kræver en gyldig JWT og den korrekte rolle.

---

### 🏨 Hotel & Room Endpoints

| Method  | Path                         | Description            | Request body / params                                               | Response / status |
|---------|------------------------------|------------------------|----------------------------------------------------------------------|------------------|
| GET     | `/hotel`                     | Fetch all hotels       | –                                                                    | Array of hotels with `id`, `name`, `address` |
| GET     | `/hotel/{id}`                | Get hotel by ID        | Path param: `id`                                                     | Single hotel DTO or **404** if not found |
| GET     | `/hotel/{id}/rooms`          | Get rooms for a hotel  | Path param: `id`                                                     | Array of room DTOs |
| POST    | `/hotel`                     | Create hotel           | `{ "hotelName": "Hotel X", "hotelAddress": "Street Y", "rooms": [] }` | Created hotel DTO + **201** |
| PUT     | `/hotel/{id}`                | Update hotel           | Path param: `id`, body same as POST                                  | Updated hotel DTO or **404** |
| DELETE  | `/hotel/{id}`                | Delete hotel           | Path param: `id`                                                     | `"Hotel deleted"` or **404** |
| POST    | `/hotel/{id}/rooms`          | Add room to hotel      | Path param: `id`, `{ "number": "101", "price": 200.0 }`              | Created room DTO + **201** |
| DELETE  | `/hotel/{id}/rooms/{roomId}` | Remove room from hotel | Path params: `id`, `roomId`                                          | Deleted room DTO or **404** |

---

### 🔐 Authentication & Security Endpoints

| Method | Path                   | Description                  | Request body                                    | Response / status |
|--------|------------------------|------------------------------|------------------------------------------------|------------------|
| POST   | `/auth/register`       | Register a new user          | `{ "username": "alice", "password": "secret" }` | User created, role = `USER`, returns JWT + username |
| POST   | `/auth/login`          | Login with credentials       | `{ "username": "alice", "password": "secret" }` | JWT + username if valid, else **401** |
| GET    | `/protected/user_demo` | Protected endpoint (USER)    | `Authorization: Bearer <token>` header          | JSON message if authorized, else **401/403** |
| GET    | `/protected/admin_demo`| Protected endpoint (ADMIN)   | `Authorization: Bearer <token>` header          | JSON only for admin users, else **403** |

---

### 🔑 JWT & Security Notes
- Clients must include JWT in the header:  
  `Authorization: Bearer <token>`
- Tokens are generated using configuration from **environment variables** or `config.properties`.
- Tokens are validated for **expiry** and **signature**.
- Insufficient roles return **403 Forbidden**.

---

### ⚠️ Error Handling & Validation
- Global exception handler logs errors and returns **HTTP 500** with a simple error message.
- `ValidationException` and `EntityNotFoundException` map to appropriate error responses (e.g. **400** / **404**).
- Unknown JSON fields in requests are **ignored** (ObjectMapper configured to not fail on unknown properties).

---


## 🔐 Security Details

- **Password hashing** udføres af `User`-entiteten via **BCrypt**, så plaintext passwords aldrig gemmes.  
- **SecurityService** håndterer registration og login:  
  - Ved registration gemmes en bruger, en `USER`-rolle oprettes (hvis den ikke findes), og brugeren får tildelt rollen.  
  - Ved login valideres password mod det hashede password, og et `UserDTO` med brugernavn + roller returneres.  
- **TokenService** genererer JWT med konfigurerbar `secret`, `issuer` og udløbstid.  
- **Protected endpoints** kræver en bestemt rolle. En `before` filter tjekker token og knytter bruger til request-context.  

---

### ⚙️ Configuring Secrets

For lokal udvikling: lav en `config.properties` på classpath med fx:

- DB_NAME=hotel
- DB_USERNAME=username
- DB_PASSWORD= password
- ISSUER=name of issuer
- TOKEN_EXPIRE_TIME=1800 # seconds
- SECRET_KEY=super-secret-key # used for signing JWTs


For deployment: brug environment variables  
`CONNECTION_STR, DB_NAME, DB_USERNAME, DB_PASSWORD, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY`.  

`HibernateConfig` skifter mellem test, udvikling og deployment afhængigt af `DEPLOYED` og `isTest` flags.

---

## 🏁 Running the Application

### 🔧 Prerequisites
- Java 17 og Maven 3.8+ installeret.  
- PostgreSQL kørende lokalt. Opret en database kaldet **hotel** (eller justér `DB_NAME`).  
- Sæt DB-credentials og JWT-secrets via env vars eller `config.properties`.  

### ▶️ Build & Run
Klon repo’et og kør:

```bash
mvn clean package   # compile + run tests
mvn exec:java -Dexec.mainClass="app.Main"
```

## 🌐 Interacting with the API

## Register a user (receives a token)
curl -X POST http://localhost:7007/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"alice","password":"secret"}'

## Login
curl -X POST http://localhost:7007/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"alice","password":"secret"}'

## Use token to call protected endpoint
TOKEN=<TOKEN>
curl http://localhost:7007/api/v1/protected/user_demo \
     -H "Authorization: Bearer $TOKEN"

## Create a new hotel
curl -X POST http://localhost:7007/api/v1/hotel \
     -H "Content-Type: application/json" \
     -d '{"hotelName":"My Hotel","hotelAddress":"Main St 1","rooms":[]}'

## Get all hotels
curl http://localhost:7007/api/v1/hotel

## Get rooms for hotel with id 1
curl http://localhost:7007/api/v1/hotel/1/rooms

## Add a room to hotel 1
curl -X POST http://localhost:7007/api/v1/hotel/1/rooms \
     -H "Content-Type: application/json" \
     -d '{"number":"101","price":150.0}'

## Delete hotel 2
curl -X DELETE http://localhost:7007/api/v1/hotel/2

## 🧪 Testing

Projektet inkluderer **integrationstests** for både DAO-laget og REST API’et:

- **DAO tests**  
  Verificerer at hoteller kan oprettes, hentes, opdateres og slettes, samt at rooms kan tilføjes til et hotel.  

- **API tests**  
  Starter en Javalin-server på port **7777**, populere databasen med test-hoteller og tester endpoints med **Rest Assured**.  

- **Testcontainers**  
  `HibernateConfig.isTest=true` aktiverer en speciel `EntityManagerFactory`, som bruger **Testcontainers JDBC driver** (`jdbc:tc:postgresql:…`).  
  Dette sikrer at tests kører mod en isoleret Postgres-database i stedet for din lokale/prod DB.  

➡️ For at køre tests:  

```bash
mvn test

```

📜 Logging

Logging er sat op med Logback (src/main/resources/logback.xml):

Appenders:

Console output.

Fil-log til logs/javalin-app.log.

Log levels:

Root logger: INFO.

Dedikeret app logger: DEBUG.

ApplicationConfig viser hvordan requests, responses og exceptions logges.

👉 Du kan frit justere log-niveauer eller log-patterns i logback.xml.
