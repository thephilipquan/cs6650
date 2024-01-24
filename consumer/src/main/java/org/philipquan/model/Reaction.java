package org.philipquan.model;

import java.util.Objects;

import com.google.gson.Gson;

public class Reaction {

    private int albumId;
    private int likes;
    private int dislikes;

    /**
     * @param json the json representation of the object to deserialize from
     * @return the serialization of the passed json
     */
    public static Reaction fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Reaction.class);
    }

    /**
     * @param albumId the albumId to react to
     * @param likes the number of likes to react
     * @param dislikes the number of dislikes to react
     */
    public Reaction(int albumId, int likes, int dislikes) {
        this.albumId = albumId;
        this.likes = likes;
        this.dislikes = dislikes;
    }

    public int getAlbumId() {
        return this.albumId;
    }

    public int getLikes() {
        return this.likes;
    }

    public int getDislikes() {
        return this.dislikes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reaction reaction = (Reaction) o;
        return this.albumId == reaction.albumId &&
          this.likes == reaction.likes &&
          this.dislikes == reaction.dislikes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.albumId, this.likes, this.dislikes);
    }

    @Override
    public String toString() {
        return "Reaction{" +
          "albumId=" + this.albumId +
          ", likes=" + this.likes +
          ", dislikes=" + this.dislikes +
          '}';
    }
}
