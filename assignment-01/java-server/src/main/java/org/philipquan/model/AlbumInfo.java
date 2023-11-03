package org.philipquan.model;

import java.util.Objects;

public class AlbumInfo {
    private final String artist;
    private final String title;
    private final String year;

    public AlbumInfo(String artist, String title, String year) {
        this.artist = artist;
        this.title = title;
        this.year = year;
    }

    public String getArtist() {
        return this.artist;
    }

    public String getTitle() {
        return this.title;
    }

    public String getYear() {
        return this.year;
    }

    @Override
    public String toString() {
        return "AlbumInfo{" +
          "artist='" + this.artist + '\'' +
          ", title='" + this.title + '\'' +
          ", year='" + this.year + '\'' +
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
        AlbumInfo albumInfo = (AlbumInfo) o;
        return Objects.equals(this.artist, albumInfo.artist) &&
          Objects.equals(this.title, albumInfo.title) &&
          Objects.equals(this.year, albumInfo.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.artist, this.title, this.year);
    }
}