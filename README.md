# Media Ratings Platform (MRP)

**Software Engineering (Java) — Intermediate Submission, Class 13**

MRP is a small REST API written in Java. Users can register, log in, and manage entries for movies, series, and games. The application responds with JSON and stores its data in PostgreSQL. There is no frontend; I use Postman to send requests to the server.

**GitHub repository:** https://github.com/Pema-898/MRP_Project.git

## What is implemented

- Registration and login with unique usernames and password verification.
- Bearer-token authentication for media requests.
- Media CRUD: create, list, get by ID, update, and delete.
- Only the creator of a media entry can update or delete it.
- Basic media validation, including release year, age restriction, and required genres.
- PostgreSQL persistence and Docker Compose configuration.
- A custom HTTP router, controllers, services, and repository interfaces.
- 20 JUnit 5 unit tests and a 16-request Postman collection.

The code also includes media search/filter/sorting support, even though these are primarily final-submission features. Rating operations, favorites, recommendations, user statistics, and the leaderboard are **not implemented as API endpoints** in the intermediate version. A standalone `Rating` data model may be included for the intermediate grading checklist; it does not mean that ratings are implemented.

## Requirements

- Java **21** (JDK, not only JRE)
- Maven 3.x, or IntelliJ IDEA with its bundled Maven
- Docker Desktop with Docker Compose
- Postman for API testing

## Database setup

From the project directory containing `compose.yaml`:

```bash
docker compose up -d
```

The Compose configuration starts PostgreSQL 16 in the `mrp-intermediate-db` container. The Windows/host port is **5433**, mapped to port 5432 inside the container.

| Setting | Default |
|---|---|
| Host | `localhost` |
| Host port | `5433` |
| Database | `mrp` |
| Username | `mrp` |
| Password | `mrp` |

The tables are initialized using `sql/init.sql` when a new PostgreSQL data volume is created. The persistent Docker volume keeps data when the container stops. Avoid `docker compose down -v` unless you want to remove the database data.

Check the container with:

```bash
docker ps
docker exec -it mrp-intermediate-db psql -U mrp -d mrp
```

Inside `psql`, enter `\dt` to list tables and `\q` to exit.

## Run the application

Run from IntelliJ's Maven tool window (**Plugins → exec → exec:java**) or from a terminal where Maven is available:

```bash
mvn clean test
mvn exec:java
```

The server listens at **http://localhost:8080/api** by default. Leave it running while using Postman. If port 8080 is occupied, configure the `PORT` environment variable and update the Postman base URL.

### Optional environment variables

The application uses these defaults but can read alternatives from the environment:

| Variable | Default |
|---|---|
| `PORT` | `8080` |
| `DB_URL` | `jdbc:postgresql://localhost:5433/mrp` |
| `DB_USER` | `mrp` |
| `DB_PASSWORD` | `mrp` |

For example, in Git Bash:

```bash
export DB_URL="jdbc:postgresql://localhost:5433/mrp"
mvn exec:java
```

## Intermediate API endpoints

All endpoints are under `/api`.

| Method | Endpoint | Description | Bearer token? |
|---|---|---|---|
| `POST` | `/users/register` | Register a new user | No |
| `POST` | `/users/login` | Log in and obtain token | No |
| `GET` | `/media` | List media entries; optional filters | Yes |
| `GET` | `/media/{mediaId}` | Get one media entry | Yes |
| `POST` | `/media` | Create a media entry | Yes |
| `PUT` | `/media/{mediaId}` | Update an entry (creator only) | Yes |
| `DELETE` | `/media/{mediaId}` | Delete an entry (creator only) | Yes |

Register or log in:

```http
POST http://localhost:8080/api/users/register
Content-Type: application/json

{"username":"student1","password":"pass123"}
```

Log in using the same JSON at `/api/users/login`. The response includes a `token`, `userId`, and `username`. Use the returned token for subsequent requests:

```http
Authorization: Bearer student1-mrpToken
```

Create a media entry:

```http
POST http://localhost:8080/api/media
Authorization: Bearer student1-mrpToken
Content-Type: application/json

{
  "title": "Inception",
  "description": "Science-fiction thriller",
  "mediaType": "movie",
  "releaseYear": 2010,
  "genres": ["sci-fi", "thriller"],
  "ageRestriction": 12
}
```

Media type values are `movie`, `series`, or `game`. The application uses standard success and error codes, including `200`, `201`, `204`, `400`, `401`, `403`, `404`, and `409` as applicable.

## Testing

The project contains **20 JUnit tests** under `src/test/java`:

| Class | Count |
|---|---:|
| `AuthServiceTest` | 7 |
| `MediaServiceTest` | 9 |
| `MediaFilterTest` | 4 |

Run them using IntelliJ's **Maven → Lifecycle → test** or `mvn clean test`. The latest shared Maven output (26 October 2026) showed **20 tests passed, 0 failures, 0 errors, 0 skipped**. The unit tests use in-memory repositories and do not need Docker.

For HTTP and database integration checks, import `MRP_Intermediate_Postman_Collection.json` in Postman. Run the collection **in its existing order**: it registers users, logs in, creates and modifies media, tests access restrictions, and deletes the media entry. The collection stores tokens and media IDs as variables.

## Project structure

```text
src/main/java/at/technikum/application/
├── Main.java                  # Starts the server
├── server/                    # HTTP server, request and response handling
├── routing/                   # Route matching and controller helpers
├── common/                    # Exceptions and database connection factory
└── mrp/
    ├── MediaRatingApplication.java   # Wires services and routes
    ├── auth/                  # Registration, login and tokens
    ├── user/                  # User model and repository
    └── media/                 # Media model, controller, service, repository
src/test/java/                 # JUnit tests and in-memory test repositories
sql/init.sql                   # Database schema
compose.yaml                   # Docker PostgreSQL setup
MRP_Intermediate_Postman_Collection.json
MRP_Intermediate_Protocol.md
```

## Notes

- Tokens are stored in server memory, so logging in again is necessary after the Java server restarts.
- The token format follows the assignment example. This is a classroom implementation, **not** a production-grade authentication system.
- The SQL schema includes some tables intended for later features because media queries currently reference their aggregate data.
- See `MRP_Intermediate_Protocol.md` for architecture, implementation decisions, testing, and development notes.

