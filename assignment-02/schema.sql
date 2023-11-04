CREATE SCHEMA IF NOT EXISTS album_app;
SET search_path TO album_app;

DROP TABLE IF EXISTS albums;

CREATE TABLE albums(
	id serial PRIMARY KEY,
	artist varchar(255) NOT NULL,
	title varchar(255) NOT NULL,
	year integer NOT NULL,
	image bytea NOT NULL
);
