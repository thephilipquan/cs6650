CREATE SCHEMA IF NOT EXISTS album_app;
SET search_path TO album_app;

DROP TABLE IF EXISTS reactions;
DROP TABLE IF EXISTS albums;

CREATE TABLE albums(
	albumId serial PRIMARY KEY,
	artist varchar(255) NOT NULL,
	title varchar(255) NOT NULL,
	year integer NOT NULL,
	image bytea NOT NULL
);

CREATE TABLE reactions(
	albumId serial PRIMARY KEY REFERENCES albums (albumId) ON DELETE CASCADE,
	likes integer DEFAULT 0 CHECK (likes > -1),
	dislikes integer DEFAULT 0 CHECK (dislikes > -1)
);
