# Development Protocol — Media Ratings Platform (MRP)

**Course:** Software Engineering / Java  
**Submission:** Intermediate submission (Class 13)  
**GitHub repository:** https://github.com/Pema-898/MRP\_Project.git
**Environment:** Windows 11, IntelliJ IDEA, Java 21, Maven, Docker Desktop, PostgreSQL 16, Postman  
**Document updated:** 26 October 2026

## 1\. Introduction and scope

The aim of my project is to build a REST API for a media ratings platform using Java. Instead of a graphical user interface, the application has an HTTP server that receives requests and returns JSON responses. The idea is that a web or mobile application could use this API later.

For the intermediate submission, I focused on making the technical foundation work: starting an HTTP server, registering users, logging in, checking authorization tokens, storing data in PostgreSQL, and implementing create/read/update/delete (CRUD) operations for media entries. The media entries can be movies, series, or games.

The final assignment also includes ratings, favorites, recommendations, profiles, and leaderboards. I have **not** implemented their API endpoints for this submission. Some media filtering code and database tables for later features are already present, but this does not mean that the final features are finished.

## 2\. Tools and technologies

|Tool|Why I use it|
|-|-|
|Java 21|Main programming language and object-oriented implementation|
|IntelliJ IDEA|Writing and running the application|
|Maven|Dependencies, compilation, and automated tests|
|`com.sun.net.httpserver.HttpServer`|Lightweight HTTP server without Spring|
|PostgreSQL 16|Persistent storage for accounts and media entries|
|Docker Compose|Runs a separate PostgreSQL instance for this project|
|JDBC|Java connection to PostgreSQL and execution of SQL|
|JUnit 5|Automated unit tests|
|Postman|Sending real HTTP requests and checking the endpoints|
|Git/GitHub|Version control and documentation of changes|

For JSON I used the project's own utility classes rather than a full web framework. I chose a simple layered structure so that HTTP handling, business rules, and database queries do not all end up in one class.

## 3\. Project structure and architecture

The important parts of the application are separated into packages:

* **`server`**: Starts the HTTP server and handles the connection between the HTTP exchange and the application.
* **`server/http`**: Contains representations of methods, status codes, requests, responses, and content types.
* **`routing`**: Finds which controller method should handle the URL and HTTP method.
* **`mrp/auth`**: Deals with registration, login, password checking, and authorization tokens.
* **`mrp/user`**: Contains the user model and the database repository for user accounts.
* **`mrp/media`**: Contains the media model, validation and CRUD logic, controller, and PostgreSQL repository.
* **`common/database`**: Provides the JDBC database connections.
* **`common/exception`**: Defines errors that the HTTP layer can turn into meaningful status codes.

A typical request goes through the following steps:

```text
Postman → HTTP Server → Handler → Router → Controller
                                          ↓
                                       Service
                                          ↓
                                  Repository interface
                                          ↓
                                     PostgreSQL
```

For example, when the user sends `POST /api/media`, the router chooses the `MediaController.create()` method. The controller reads the JSON and identifies the logged-in user. The `MediaService` checks the data and calls the repository, which saves it in PostgreSQL. The controller then sends the result back as a JSON response.

This separation was useful when I needed to find out whether a problem was caused by HTTP parsing, validation, or a database operation.

## 4\. Object-oriented design and dependency injection

I tried to use OOP in a practical way rather than putting all of the logic in `Main.java`.

The `User` and `Media` models represent application data. The services contain the rules for working with that data, while repositories are responsible for reading and writing it. `MediaRepository` and `UserRepository` are interfaces; their PostgreSQL implementations are separate classes.

Dependency injection is done through constructors. For example, `MediaService` receives a `MediaRepository` instead of creating `PostgresMediaRepository` itself. `MediaRatingApplication` creates the concrete objects and connects them together. This is manual constructor injection and does not need Spring.

```java
MediaRepository mediaRepository = new PostgresMediaRepository(connections);
MediaService mediaService = new MediaService(mediaRepository);
```

This design helped with unit testing because I could supply an in-memory repository in place of PostgreSQL. It also shows the idea behind the dependency inversion principle: the service uses a repository interface rather than depending directly on the database implementation.

The classes do not follow every SOLID principle perfectly. For example, `MediaService` handles both validation and filtering as well as CRUD logic. For now, I think this structure is enough for the size of the project.

## 5\. HTTP server and API routes

`Main.java` creates a `MediaRatingApplication` and starts `Server` on port 8080 by default. The server is based on Java's `HttpServer` and uses the application's router to match requests to controller methods. The route registration is done in `MediaRatingApplication`.

The implemented intermediate routes are:

|Method|Route|Function|
|-|-|-|
|POST|`/api/users/register`|Register a user|
|POST|`/api/users/login`|Log in and get a token|
|GET|`/api/media`|List media entries|
|GET|`/api/media/{mediaId}`|Read one media entry|
|POST|`/api/media`|Create media|
|PUT|`/api/media/{mediaId}`|Update media|
|DELETE|`/api/media/{mediaId}`|Delete media|

Registration and login are public. The media routes call the authentication service first. `GET` and `PUT` normally return `200`, creation returns `201`, and a successful delete returns `204` without a body. The application also returns client error responses for invalid inputs, unknown resources, missing tokens, and unauthorized modifications.

## 6\. Registration, login, and authorization

A registration request contains a username and password. The `AuthService` checks that the username has at least three characters, the password at least four, and the username is not already registered. The account is then stored through `UserRepository`.

Passwords are not saved as plain text. The `PasswordHasher` uses a salted PBKDF2-HMAC-SHA256 hash. On login, the supplied password is verified against the stored hash. A successful login returns the token, user ID, and username.

For this assignment, the token format is `<username>-mrpToken`, following the example from the specification. The `TokenService` keeps issued tokens in a `ConcurrentHashMap` while the Java application is running. To access a protected route, Postman must send:

```http
Authorization: Bearer <token>
```

If the header or token is missing or invalid, the request is rejected. Restarting the Java server clears the issued tokens, so the user has to log in again. This works for the intermediate requirements, although a real production application would need less predictable tokens and additional security features such as expiration.

## 7\. Media CRUD and validation

A media entry has a title, description, media type, release year, genres, age restriction, and information about its creator. `MediaInput` reads the fields from JSON. `MediaService` checks the business rules before writing data to the database.

Some checks are: the title cannot be blank, the release year has to be in the allowed range, the age restriction has to be valid, and there must be at least one usable genre. The service normalizes genres by trimming spaces and converting them to lowercase.

The creator ID is saved when a new media entry is added. If another user attempts to update or delete it, `MediaService` compares the logged-in user with the creator and rejects the request. That check is important because simply having a valid token should not allow someone to edit another user's entry.

The application also supports partial-title search, genre filtering, and sorting. Those were already part of the media code, although they go beyond the minimum scope of the intermediate submission.

## 8\. PostgreSQL and Docker

I use PostgreSQL because the assignment requires persistent data instead of only saving it in Java collections. The project contains a `compose.yaml` and `sql/init.sql` to create the database and tables.

To keep this database separate from another MRP setup, I use a Docker container named **`mrp-intermediate-db`**, with PostgreSQL exposed on **host port 5433**. PostgreSQL itself listens on port 5432 inside the container. The database, username, and password are currently set to `mrp` for local development.

`ConnectionFactory` uses this default connection URL:

```text
jdbc:postgresql://localhost:5433/mrp
```

It also supports the environment variables `DB_URL`, `DB_USER`, and `DB_PASSWORD`, so the connection can be changed without modifying source code.

The key tables are `users`, `media`, and `media_genres`. A separate `media\_genres` table is needed because a media entry can have more than one genre. The SQL initialization file also contains tables for ratings, likes, and favorites. These are not exposed as intermediate API features; they remain because the current media repository query references them for aggregate columns.

The repository classes use JDBC `PreparedStatement` to bind SQL parameters rather than inserting user strings into queries. This reduces SQL-injection risk. Media changes that affect multiple tables use transactions, so partially completed updates can be rolled back if a database operation fails.

## 9\. Unit tests

I added 20 automated JUnit 5 tests, organized as follows:

|Test file|Tests|Focus|
|-|-:|-|
|`AuthServiceTest`|7|Registration, duplicate and invalid usernames, login, invalid password, Bearer tokens|
|`MediaServiceTest`|9|Creating media, invalid input, filtering, sorting, ownership checks, deletion|
|`MediaFilterTest`|4|Parsing filters and sorting parameters|
|**Total**|**20**||

The tests use `InMemoryUserRepository` and `InMemoryMediaRepository`. These implement the same repository interfaces but store records in memory. That makes it possible to test the services without starting Docker each time.

On **26 October 2026**, I ran Maven's `test` lifecycle goal from IntelliJ IDEA. Maven Surefire discovered the three `\\\\\\\*Test` classes and JUnit ran their test methods. The actual output was:

```text
Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

This confirms that the tested Java business logic passed. It does **not** by itself prove that JDBC or HTTP works, which is why I also use Postman.

## 10\. Postman and integration checks

I use `MRP_Intermediate_Postman_Collection.json` to test the application through real HTTP requests. It contains **16 ordered requests**, which include registration, login, creating and reading media, updating as the creator, checking missing and invalid tokens, logging in as a second user, checking creator-only updates/deletion, handling duplicate or too-short usernames, and deleting the entry.

The collection stores tokens and the created media ID in Postman collection variables, so I do not need to hard-code the ID for each request. I have run the API requests with PostgreSQL and the Java application started, and the endpoints worked in Postman.

The JUnit tests and Postman tests serve different purposes: JUnit checks individual Java rules, while Postman also goes through HTTP routing, request parsing, and the database.

## 11\. Problems and decisions during development

### The first HTTP server was too limited

The early project had a simple server that returned hard-coded example data. That was enough to understand HTTP requests at first, but it was not enough for actual registration and CRUD. I separated the server, handler, router, and controllers, then connected them in `MediaRatingApplication`.

### Connecting to the correct Docker database

I needed a separate PostgreSQL container instead of reusing the container from the other MRP project. Since I'm repeating the semester, I already had a Docker container from last year. I changed the Compose configuration to use its own container and volume, and exposed it on host port 5433. The database connection settings had to match this port.

### Running Maven tests

When I first tried `mvn clean test` in Git Bash, the terminal could not find Maven. I checked the project files and found there was no Maven Wrapper. I then used IntelliJ IDEA's bundled Maven through the Maven tool window and successfully ran the 20 tests.

## 12\. Estimated development time



|Area|Approximate hours|
|-|-:|
|Initial Java/Maven setup and understanding project structure|1|
|HTTP server, request handling, routing|5|
|PostgreSQL, Docker, JDBC, SQL tables|5|
|Registration, login, Bearer tokens|2|
|Media CRUD, validation and creator permissions|3|
|Unit tests and debugging|4|
|Postman testing and documentation|3|
|**Total**|**23**|

## 13\. Summary and next steps

For the intermediate submission, the server, authentication, PostgreSQL persistence, and media CRUD functionality are implemented. The 20 JUnit tests passed in IntelliJ, and the REST API was checked with Postman. The architecture uses controller, service, and repository classes with constructor injection rather than a full-stack web framework.

For the final submission, I will need to add the actual rating functionality, comments and likes, favorites, profiles and statistics, recommendations, and leaderboard. I may also improve the security and test coverage once those features are added.

