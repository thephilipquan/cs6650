package org.philipquan.model;

import java.util.Objects;

/**
 * A package for newly posted albums to return to the client.
 */
public class ImageMetaData {

	private final Long albumId;

	private final Long imageSize;

	/**
	 * @param albumId the generated album id
	 * @param imageSize the size of the album photo (in bytes) passed from the client
	 */
	public ImageMetaData(Long albumId, Long imageSize) {
		this.albumId = albumId;
		this.imageSize = imageSize;
	}

	public Long getAlbumId() {
		return this.albumId;
	}

	public Long getImageSize() {
		return this.imageSize;
	}

	@Override
	public String toString() {
		return "ImageMetaData{" +
		  "albumId='" + this.albumId + '\'' +
		  ", imageSize='" + this.imageSize + '\'' +
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
		ImageMetaData that = (ImageMetaData) o;
		return Objects.equals(this.albumId, that.albumId) &&
		  Objects.equals(this.imageSize, that.imageSize);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.albumId, this.imageSize);
	}
}
