CREATE TABLE IF NOT EXISTS users (
    id              SERIAL PRIMARY KEY,
    username        VARCHAR(80) NOT NULL UNIQUE,
    password_hash   TEXT NOT NULL,
    email           VARCHAR(255),
    favorite_genre  VARCHAR(100),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS media (
    id               SERIAL PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    description      TEXT,
    media_type       VARCHAR(20) NOT NULL CHECK (media_type IN ('movie', 'series', 'game')),
    release_year     INTEGER NOT NULL CHECK (release_year > 0),
    age_restriction  INTEGER NOT NULL CHECK (age_restriction BETWEEN 0 AND 21),
    creator_id       INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS media_genres (
    media_id INTEGER NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    genre    VARCHAR(100) NOT NULL,
    PRIMARY KEY (media_id, genre)
);

CREATE TABLE IF NOT EXISTS ratings (
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_id    INTEGER NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    stars       INTEGER NOT NULL CHECK (stars BETWEEN 1 AND 5),
    comment     TEXT,
    confirmed   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_rating_user_media UNIQUE (user_id, media_id)
);

CREATE TABLE IF NOT EXISTS rating_likes (
    user_id     INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating_id   INTEGER NOT NULL REFERENCES ratings(id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, rating_id)
);

CREATE TABLE IF NOT EXISTS favorites (
    user_id     INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_id    INTEGER NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, media_id)
);

CREATE INDEX IF NOT EXISTS idx_media_title ON media (lower(title));
CREATE INDEX IF NOT EXISTS idx_media_type ON media (media_type);
CREATE INDEX IF NOT EXISTS idx_media_release_year ON media (release_year);
CREATE INDEX IF NOT EXISTS idx_media_genres_genre ON media_genres (lower(genre));
CREATE INDEX IF NOT EXISTS idx_ratings_user ON ratings (user_id);
CREATE INDEX IF NOT EXISTS idx_ratings_media ON ratings (media_id);
CREATE INDEX IF NOT EXISTS idx_favorites_user ON favorites (user_id);
