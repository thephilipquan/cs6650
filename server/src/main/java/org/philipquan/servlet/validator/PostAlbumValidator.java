package org.philipquan.servlet.validator;

import static org.philipquan.servlet.utility.ServletResponseHelper.isNumeric;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Extends {@link RequestValidator). Validates the {@link javax.servlet.http.HttpServlet#doPost} method.
 *
 * <br>
 *
 * Maps the following keys.
 *
 * <br>
 *
 * <ul>
 * <li>{@value #MAP_IMAGE_PART_KEY}</li>
 * <li>{@value #MAP_ARTIST_KEY}</li>
 * <li>{@value #MAP_TITLE_KEY}</li>
 * <li>{@value #MAP_YEAR_KEY}</li>
 * </ul>
 */
public class PostAlbumValidator extends RequestValidator {

	public final static String MAP_IMAGE_PART_KEY = "image_part";
	public final static String MAP_ARTIST_KEY = "artist";
	public final static String MAP_TITLE_KEY = "title";
	public final static String MAP_YEAR_KEY = "year";

	private final static String IMAGE_KEY = "image";
	private final static String ALBUM_KEY = "profile";
	private final static String ARTIST_KEY = "artist";
	private final static String TITLE_KEY = "title";
	private final static String YEAR_KEY = "year";

	@Override
	public Boolean validate(HttpServletRequest request) throws IOException, ServletException {
		Part imagePart = request.getPart(IMAGE_KEY);
		Part profilePart = request.getPart(ALBUM_KEY);
		if (imagePart == null || profilePart == null) {
			this.status = HttpServletResponse.SC_BAD_REQUEST;
			this.errorMessage = String.format("invalid request. Form must contain %s and %s.", IMAGE_KEY, ALBUM_KEY);
			return false;
		}

		Map<String, String> profile = partToMap(request.getPart(ALBUM_KEY));
		if (!profile.keySet().containsAll(Arrays.asList(ARTIST_KEY, TITLE_KEY, YEAR_KEY)) || !isNumeric(profile.get(YEAR_KEY))) {
			this.status = HttpServletResponse.SC_BAD_REQUEST;
			this.errorMessage = String.format("invalid request. Form must contain %s, %s, %s. Year must be numeric.", ARTIST_KEY, TITLE_KEY, YEAR_KEY);
			return false;
		}

		this.map.put(ARTIST_KEY, profile.get(ARTIST_KEY));
		this.map.put(TITLE_KEY, profile.get(TITLE_KEY));
		this.map.put(YEAR_KEY, Integer.parseInt(profile.get(YEAR_KEY)));
		this.map.put("image_part", imagePart);
		return true;
	}

	/**
	 * Converts a {@link Part} value into a map.
	 *
	 * @param part from {@link HttpServletRequest#getPart}
	 * @return a map of key values pairs.
	 * @throws IOException from {@link Part#getInputStream}
	 */
	private Map<String, String> partToMap(Part part) throws IOException {
		String json = IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8);
		return new Gson().fromJson(json, new TypeToken<Map<String, String>>() {}.getType());
	}
}