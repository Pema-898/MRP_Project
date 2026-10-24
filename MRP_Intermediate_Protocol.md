# Development Protocol — Media Ratings Platform (MRP)

**Course:** Software Engineering / Java  
**Submission:** Intermediate submission (Class 13)  
**Student:** [Your name]  
**GitHub repository:** [Insert your real GitHub repository URL]  
**Date:** [Submission date]

> **Before submitting:** This document is based on the current project source code and the intermediate checklist. Please fill in the personal details, estimated time spent, actual problems you encountered, and the test results after running the application. I have left these items visible instead of making up development history or claiming tests have passed.

## 1. Project overview

The Media Ratings Platform is a Java application that provides a REST API. It does not have a graphical interface. Instead, a client such as Postman sends HTTP requests to the server, and the server responds with JSON and an HTTP status code.

For the intermediate submission, I focused on the HTTP server, user registration and login, token-based authentication, and managing media entries. A user should be able to create, read, update, and delete media entries. Editing and deleting should only be possible for the user who originally created the entry. The data is stored in PostgreSQL so it is not lost when the Java program stops.

The full assignment also includes ratings, favorites, recommendations, leaderboards and user statistics. These are mainly for the final submission and are not exposed as part of my intermediate API.

## 2. Tools and technologies

| Technology | Why I used it |
|---|---|
| Java 21 | Main programming language; records and other modern Java features are used for model classes. |
| Maven | Manages dependencies and provides standard commands to compile and test the project. |
| JDK `HttpServer` | Accepts HTTP requests without using Spring or another full web framework. |
| PostgreSQL 16 | Saves users, media entries and the relationships between media and genres. |
| Docker Compose | Starts PostgreSQL with a repeatable configuration. |
| JDBC | Allows Java code to communicate directly with PostgreSQL. |
| JUnit 5 | Tests service logic without depending on a running HTTP server. |
| Postman | Sends requests and checks responses from the running REST API. |
| Git and GitHub | Records changes and stores the source code. |

The project uses a small HTTP server rather than a larger web framework because that matches the restrictions of the assignment. It also means I have to handle routing, request bodies, authorization and error responses more explicitly.

## 3. Project structure

The code is separated into packages so the HTTP-related logic and the business logic do not all end up in one large class.

```text
src/main/java/at/technikum/application/
├── Main.java
├── server/
│   ├── Server.java
│   ├── Handler.java
│   ├── http/                 Request, Response, Method, Status ...
│   └── util/                 JSON and query-parameter helpers
├── routing/                  Router, Route, Controller, Application
├── common/
│   ├── database/             PostgreSQL connection
│   └── exception/            Application-specific HTTP errors
└── mrp/
    ├── MediaRatingApplication.java
    ├── auth/                  Registration, login, token handling
    ├── user/                  User model and persistence
    ├── media/                 Media model, service, controller, repository
    └── rating/                Rating model only (intermediate checklist)
```

The main classes have different tasks:

- `Main` creates the application and starts the HTTP server, using port 8080 by default.
- `Server` opens the port. `Handler` translates incoming HTTP requests into the project's own `Request` objects and converts responses back to HTTP.
- `Router` checks the URL and HTTP method and selects the correct controller method.
- Controllers read request data and return HTTP responses.
- Services contain the business rules, for example input validation and ownership checks.
- Repositories execute the SQL queries to read and write data.

This separation is useful because I can test the service methods without needing to start PostgreSQL for every test. It also makes it easier to see where a bug belongs: HTTP handling, validation or database access.

## 4. HTTP server and request handling

The server uses `com.sun.net.httpserver.HttpServer`. Incoming requests are received by `Handler`, which extracts the method, path, headers, query parameters and request body. The router then looks for the matching path and HTTP method.

For example, `POST /api/media` is sent to the media controller's create method, while `GET /api/media/{mediaId}` reads one existing entry. The `{mediaId}` part is a path parameter: its actual value comes from the URL.

Most successful responses contain JSON. Creating a new user or media entry returns HTTP 201; ordinary successful GET or PUT requests return HTTP 200; and deletion returns HTTP 204 without a response body. Invalid requests use appropriate 4xx codes. Unexpected server errors are handled separately.

## 5. Registration and login

Registration is implemented through `POST /api/users/register`. The request contains a username and password. Before creating the user, the service checks basic requirements such as username and password length and whether the username already exists.

The application does not store plaintext passwords. `PasswordHasher` creates a salted PBKDF2-HMAC-SHA256 hash. When someone logs in, the submitted password is checked against the stored hash.

Login is available at `POST /api/users/login`. On success, the server returns a token together with the username and user ID. The current token format follows the example from the assignment: `<username>-mrpToken`. Tokens are stored in a `ConcurrentHashMap` in the running Java application.

A protected endpoint expects the request header:

```http
Authorization: Bearer <token>
```

If the token is missing or invalid, the application rejects the request. Tokens are held in memory rather than persisted, so they have to be obtained again after a restart. This token mechanism is suitable for the assignment but would need improvement for a real public service, for example unpredictable token values and expiration times.

## 6. Media management (CRUD)

For the intermediate submission, authenticated users can:

| Method | Endpoint | Meaning |
|---|---|---|
| POST | `/api/media` | Create a media entry |
| GET | `/api/media` | List media entries |
| GET | `/api/media/{mediaId}` | Get one entry |
| PUT | `/api/media/{mediaId}` | Update an entry |
| DELETE | `/api/media/{mediaId}` | Delete an entry |

Each media entry contains a title, description, media type (`movie`, `series` or `game`), release year, list of genres and age restriction. The model also stores information about the creator.

`MediaService` checks important rules before saving changes. A title must not be empty, the release year must be in an accepted range, an entry must have at least one genre, and the age restriction must be valid. Genre names are cleaned up and normalized to lowercase.

The repository stores the media data in PostgreSQL. Since a media entry can have multiple genres, the genres are stored in the separate `media_genres` table.

### Ownership check

The user who creates an entry is stored as its creator. When someone tries to update or delete an entry, `MediaService` compares the authenticated user's ID with the creator ID. If they are different, the operation is rejected with HTTP 403.

The implementation also contains basic search/filter and sorting support, although these are not the main focus of Class 13.

## 7. Database design and SQL security

PostgreSQL runs through the project's `compose.yaml`. The current default configuration uses database `mrp`, username `mrp`, password `mrp` and local port `5432`. `ConnectionFactory` also allows overriding the connection settings through the `DB_URL`, `DB_USER` and `DB_PASSWORD` environment variables.

The important tables for intermediate functionality are:

- `users` — account information and password hashes.
- `media` — media entries and their creator IDs.
- `media_genres` — one row per genre attached to an entry.

The provided SQL schema already contains additional tables for ratings, rating likes and favorites. They are not used as API features in the intermediate version, but they remain in the schema because the current media repository query references them for aggregate fields.

The repositories use JDBC `PreparedStatement` for SQL values instead of building queries by inserting user input directly into SQL strings. This helps prevent SQL injection. Operations that update both a media entry and its genres use a database transaction, so an error can roll back the changes instead of leaving only half of the update saved.

## 8. Unit tests

The intermediate test set contains **20 JUnit 5 test methods**:

| Test class | Count | What it covers |
|---|---:|---|
| `AuthServiceTest` | 7 | Registration, duplicate names, invalid username, login, password failure, valid/missing bearer tokens |
| `MediaServiceTest` | 9 | Media creation, input validation, filtering/sorting, ownership and deletion |
| `MediaFilterTest` | 4 | Reading filters, default sorting, rating aliases and invalid sorting |
| **Total** | **20** | |

The unit tests use `InMemoryUserRepository` and `InMemoryMediaRepository`. They behave like simple repositories but keep information in Java collections, which means unit tests do not require PostgreSQL or Docker.

To run them:

```bash
mvn clean test
```

**Actual result:** [Fill in after running: number of tests passed / failed, and any fixes needed]

These tests check the Java business logic. They do not replace integration testing of the actual HTTP endpoints and database.

## 9. Integration tests using Postman

I prepared an intermediate-specific Postman collection, `MRP_Intermediate_Postman_Collection.json`. It tests registration and login, saves the token, creates a media entry, lists and retrieves entries, updates it, checks access restrictions, and finally deletes it. It also checks some expected errors, such as missing authorization and an attempt by another user to edit or delete somebody else's media.

The collection has 16 requests intended to run in order. It creates unique usernames for a new run, so previous test data is less likely to cause conflicts. The media ID and tokens are stored in Postman collection variables.

Steps to demonstrate the application:

```bash
docker compose up -d
mvn clean test
mvn exec:java
```

Then import the collection into Postman and run it against `http://localhost:8080`.

**Actual result:** [Fill in after running the collection, including failures if any]

A further persistence check is to create media, restart the Java application and retrieve it again from PostgreSQL.

## 10. Problems encountered and solutions

The following are issues visible in the source code and the integration work. Keep only the ones you personally encountered, and adjust the explanations to match what actually happened.

### Separating Class 13 from final features

At one point the application configuration referred to `RatingService`, `FavoriteService` and other services that were not yet included. That caused compilation problems. I reduced the intermediate application to authentication and media CRUD, and adjusted `MediaController` so it no longer depends on rating services.

### Connecting the server to the application

The early version of the project had a very small HTTP server with hard-coded responses. Later I needed the server to forward requests to the application router and controllers. This required changing how `Main`, `Server` and the router were connected.

### Managing a media entry with several genres

The media object contains a list of genres, but a relational database stores rows and columns. I used a separate `media_genres` table and made sure changes to the media entry and its genres are handled together.

### Testing without a database

If every small test connects to PostgreSQL, tests become slower and more difficult to run. I used in-memory test repositories to check the rules in `AuthService` and `MediaService` separately from the database.

### Git and commit organization

Some implementation work was done locally before it was committed. I grouped the changes into commits by area, such as HTTP handling, database persistence, authentication, media CRUD and application integration. The Git history is available in the linked repository.

## 11. Approximate time spent

**Fill in your real estimates.** The table is deliberately left blank because the amount of work cannot be determined reliably from the source files or commit history alone.

| Work area | Approximate hours |
|---|---:|
| Setting up Java, Maven and project structure | [  ] |
| HTTP server, request/response classes and routing | [  ] |
| Docker, PostgreSQL and SQL tables | [  ] |
| Registration, login and token authentication | [  ] |
| Media CRUD, validation and access restrictions | [  ] |
| Unit tests and fixes | [  ] |
| Postman tests and documentation | [  ] |
| **Total** | **[  ]** |

## 12. Current status and next steps

The intermediate code is designed to provide the required server, user registration/login and media CRUD using PostgreSQL. The source also includes a Rating model for the intermediate checklist, but the actual rating endpoints belong to the final submission.

Before handing in the ZIP, I still need to confirm a successful Maven build, check the full Postman sequence against the running database, and update the test results and time estimates in this protocol.

For the final submission, the next features are ratings and comments, favorites, profile statistics, leaderboard, and recommendations.

---

**Repository:** [Insert actual GitHub URL]  
**Tested on:** [Operating system / Java version / PostgreSQL version]  
**Last verified:** [Date]
