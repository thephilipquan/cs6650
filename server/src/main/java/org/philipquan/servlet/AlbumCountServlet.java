package org.philipquan.servlet;

import static org.philipquan.servlet.utility.ServletResponseHelper.setResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.philipquan.dal.AlbumDao;

@WebServlet("/albums/count")
public class AlbumCountServlet extends HttpServlet {

	protected AlbumDao albumsDao;

	@Override
	public void init() throws ServletException {
		this.albumsDao = AlbumDao.getInstance();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	  throws ServletException, IOException {
		response.setContentType("application/json");
		Map<String, Object> messages = new HashMap<>();

		// No validation needed.

		final long lastAlbumId = this.albumsDao.getLastAlbumId();
		setResponse(messages, response, HttpServletResponse.SC_OK, String.format("albumId: %d", lastAlbumId));
	}
}
