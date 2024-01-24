package org.philipquan.model;

import java.util.Objects;

/**
 * POJO for a music album.
 */
public class Album {

	public static final String TABLE_NAME = "albums";
	public static final String ID_KEY = "albumId";
	public static final String ARTIST_KEY = "artist";
	public static final String TITLE_KEY = "title";
	public static final String YEAR_KEY = "year";
	public static final String IMAGE_KEY = "image";

	private final Long id;
	private final String artist;
	private final String title;
	private final Integer year;
	private final String image;

	public Album(Long id, String artist, String title, Integer year, String image) {
		this.id = id;
		this.artist = artist;
		this.title = title;
		this.year = year;
		this.image = image;
	}

	public Long getAlbumId() {
		return this.id;
	}

	public String getArtist() {
		return this.artist;
	}

	public String getTitle() {
		return this.title;
	}

	public Integer getYear() {
		return this.year;
	}

	public String getImage() {
		return this.image;
	}

	@Override
	public String toString() {
		return "AlbumInfo{" +
		  "id='" + this.id + '\'' +
		  ", artist='" + this.artist + '\'' +
		  ", title='" + this.title + '\'' +
		  ", year='" + this.year + '\'' +
		  ", image='" + this.image + '\'' +
		  '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Album albumInfo = (Album) o;
		return Objects.equals(this.id, albumInfo.id) &&
		  Objects.equals(this.artist, albumInfo.artist) &&
		  Objects.equals(this.title, albumInfo.title) &&
		  Objects.equals(this.year, albumInfo.year) &&
		  Objects.equals(this.image, albumInfo.image);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.artist, this.title, this.year, this.image);
	}
}
