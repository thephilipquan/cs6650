package org.philipquan;

import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.philipquan.model.ErrorMessage;
import org.philipquan.model.ImageMetaData;

@WebServlet("/albums")
public class AlbumController extends HttpServlet {

    private AlbumService albumService;

    @Override
    public void init() throws ServletException {
        this.albumService = new AlbumService();
    }

    /**
     * Get album by key.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
        response.setContentType("application/json");

        final String albumId = request.getParameter("albumId");
        if (albumId == null || albumId.trim().isEmpty()) {
            setResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorMessage("invalid request"));
            return;
        }
        if (!this.albumService.albumKeyExists(albumId)) {
            setResponse(response, HttpServletResponse.SC_NOT_FOUND, new ErrorMessage("key not found"));
            return;
        }
        setResponse(response, HttpServletResponse.SC_OK, albumService.getAlbumById(albumId));
    }

    /**
     * Returns the new key and size of an image in bytes.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
//        StringBuilder requestBodyBuilder = new StringBuilder();
//        String line;
//        while ((line = request.getReader().readLine()) != null) {
//            requestBodyBuilder.append(line);
//        }
//        Map<String, String> requestBody = new Gson().fromJson(requestBodyBuilder.toString(), Map.class);
//
//        if (!requestBody.containsKey("image")) {
//            setResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorMessage("invalid request"));
//        }
//
//        if (!requestBody.containsKey("profile")) {
//            setResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorMessage("invalid request"));
//        }

        // Starting work for deserializing the request body. Stumped at
        // recursive deserialization though.
        // TODO deserialize "profile" into AlbumInfo.

        ImageMetaData imageMetaData = albumService.createAlbum();
        setResponse(response, HttpServletResponse.SC_CREATED, imageMetaData);
    }

    private void setResponse(HttpServletResponse response, int statusCode, Object responseObject)
      throws IOException {
        response.setStatus(statusCode);
        response.getWriter().write(new Gson().toJson(responseObject));
    }

}