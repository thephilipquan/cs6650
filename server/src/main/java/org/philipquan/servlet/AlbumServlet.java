package org.philipquan.servlet;

import static org.philipquan.servlet.utility.ServletResponseHelper.setResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.philipquan.dal.AlbumDao;
import org.philipquan.model.Album;
import org.philipquan.model.ImageMetaData;
import org.philipquan.servlet.validator.GetAlbumValidator;
import org.philipquan.servlet.validator.PostAlbumValidator;
import org.philipquan.servlet.validator.RequestValidator;

@WebServlet("/albums")
@MultipartConfig(
  fileSizeThreshold=1024*1024*10,
  maxFileSize=1024*1024*50,
  maxRequestSize=1024*1024*100
)
public class AlbumServlet extends HttpServlet {

	private AlbumDao albumService;

	@Override
	public void init() {
		this.albumService = AlbumDao.getInstance();
	}

	/**
	 * GET endpoint for users to query an album's information give the album's id in the database.
	 * Writes all responses to {@link HttpServletResponse#getOutputStream}
	 *
	 * <br><br>
	 *
	 * If {@code albumId} exists, {@link HttpServletResponse#SC_OK} is returned.
	 * If {@code albumId} is missing or is not numeric, {@link HttpServletResponse#SC_BAD_REQUEST} is returned.
	 * @throws ServletException
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("application/json");
		Map<String, Object> messages = new HashMap<>();

		RequestValidator validator = new GetAlbumValidator();
		if (!validator.validate(request)) {
			setResponse(messages, response, validator.getStatus(), validator.getErrorMessage());
			return;
		}

		final long albumId = (long) validator.getParameter(GetAlbumValidator.MAP_ALBUM_ID_KEY);

		Album album = albumService.getAlbumById(albumId);
		if (album == null) {
			setResponse(messages, response, HttpServletResponse.SC_NOT_FOUND, String.format("albumId: %s not found", albumId));
		} else {
			setResponse(messages, response, HttpServletResponse.SC_OK, album);
		}
	}

	/**
	 * POST endpoint for users to submit an album by passing its image and information as a form.
	 * Writes the album's id in the database and the size of the passed image in bytes to
	 * {@link HttpServletResponse#getOutputStream}.
	 *
	 * <br><br>
	 *
	 * If the parts {@code [image, profile]}, are missing, {@link HttpServletResponse#SC_BAD_REQUEST} is returned.
	 *
	 * <br><br>
	 *
	 * If profile's json does not contain keys for {@code [artist, title, year]}
	 * {@link HttpServletResponse#SC_BAD_REQUEST} is returned.
	 *
	 * <br><br>
	 *
	 * Example curl
	 * <pre>
	 * curl http://localhost:8080/api/albums \
	 *	 --request "POST" \
	 *	 --header "multipart/form-data' \
	 *	 --form image=@path/to/image \
	 *	 --form profile='{"artist": "joe", "title": "joe's album", "year": 2023}'
	 * </pre>
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	  throws ServletException, IOException {
		response.setContentType("application/json");
		Map<String, Object> messages = new HashMap<>();

		RequestValidator validator = new PostAlbumValidator();
		if (!validator.validate(request)) {
			setResponse(messages, response, validator.getStatus(), validator.getErrorMessage());
			return;
		}

		final String artist = (String) validator.getParameter(PostAlbumValidator.MAP_ARTIST_KEY);
		final String title = (String) validator.getParameter(PostAlbumValidator.MAP_TITLE_KEY);
		final Integer year = (Integer) validator.getParameter(PostAlbumValidator.MAP_YEAR_KEY);
		final Part imagePart = (Part) validator.getParameter(PostAlbumValidator.MAP_IMAGE_PART_KEY);
		final String image = IOUtils.toString(
		  imagePart.getInputStream(),
		  StandardCharsets.UTF_8
		);

		Album requestedAlbum = new Album(-1L, artist, title, year, image);
		Album createdAlbum = albumService.createAlbum(requestedAlbum);
		setResponse(messages, response, HttpServletResponse.SC_CREATED,
		  new ImageMetaData(createdAlbum.getAlbumId(), imagePart.getSize())
		);
	}

}
