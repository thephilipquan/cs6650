CREATE DATABASE IF NOT EXISTS AlbumApp;
USE AlbumApp;

DROP TABLE IF EXISTS Albums;

CREATE TABLE Albums(
	id INT AUTO_INCREMENT,
	artist VARCHAR(255) NOT NULL,
	title VARCHAR(255) NOT NULL,
	year INT NOT NULL,
	image BLOB NOT NULL,
	CONSTRAINT pk_Albums_id PRIMARY KEY (id)
);

# CREATE SCHEMA IF NOT EXISTS album_app;
# SET search_path TO album_app;

# DROP TABLE IF EXISTS albums;

# CREATE TABLE albums(
# 	id serial PRIMARY KEY,
# 	artist varchar(255) NOT NULL,
# 	title varchar(255) NOT NULL,
# 	year integer NOT NULL,
# 	image bytea NOT NULL
# );
