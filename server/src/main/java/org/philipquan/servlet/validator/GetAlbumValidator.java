package org.philipquan.servlet.validator;

import static org.philipquan.servlet.utility.ServletResponseHelper.isNumeric;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Extends {@link RequestValidator). Validates the {@link javax.servlet.http.HttpServlet#doGet} method.
 *
 * <br>
 *
 * Maps the following keys.
 *
 * <br>
 *
 * <ul>
 * <li>{@value #MAP_ALBUM_ID_KEY}</li>
 * </ul>
 */
public class GetAlbumValidator extends RequestValidator {

	public final static String MAP_ALBUM_ID_KEY = "album_id";
	private final static String ALBUM_ID_KEY = "albumId";

	@Override
	public Boolean validate(HttpServletRequest request) throws IOException, ServletException {
		String albumId = request.getParameter(ALBUM_ID_KEY);
		if (albumId == null || albumId.trim().isEmpty() || !isNumeric(albumId)) {
			this.status = HttpServletResponse.SC_BAD_REQUEST;
			this.errorMessage = String.format("invalid request: missing parameter: %s", ALBUM_ID_KEY);
			return false;
		}
		this.map.put(MAP_ALBUM_ID_KEY, Long.parseLong(albumId));
		return true;
	}

}
