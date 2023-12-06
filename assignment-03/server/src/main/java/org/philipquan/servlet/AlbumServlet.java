package org.philipquan.servlet;

import static org.philipquan.servlet.utility.ServletResponseHelper.isNumeric;
import static org.philipquan.servlet.utility.ServletResponseHelper.setResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        Map<String, Object> messages = new HashMap<>();
        final String albumIdKey = "albumId";

        String albumId = request.getParameter(albumIdKey);
        if (albumId == null || albumId.trim().isEmpty() || !isNumeric(albumId)) {
            setResponse(messages, response, HttpServletResponse.SC_BAD_REQUEST,
              String.format("invalid request: missing parameter: %s", albumIdKey));
            return;
        }

        Album album = albumService.getAlbumById(Long.parseLong(albumId));
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
     *   --request "POST" \
     *   --header "multipart/form-data' \
     *   --form image=@path/to/image \
     *   --form profile='{"artist": "joe", "title": "joe's album", "year": 2023}'
     * </pre>
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
        response.setContentType("application/json");
        Map<String, Object> messages = new HashMap<>();
        final String imageKey = "image";
        final String albumKey = "profile";
        final String artistKey = "artist";
        final String titleKey = "title";
        final String yearKey = "year";

        Part imagePart = request.getPart(imageKey);
        Part profilePart = request.getPart(albumKey);
        if (imagePart == null || profilePart == null) {
            setResponse(messages, response, HttpServletResponse.SC_BAD_REQUEST,
              String.format("invalid request. Form must contain %s and %s.", imageKey, albumKey));
            return;
        }

        Map<String, String> profile = partToMap(request.getPart(albumKey));
        if (!profile.keySet().containsAll(Arrays.asList(artistKey, titleKey, yearKey)) || !isNumeric(profile.get(yearKey))) {
            setResponse(messages, response, HttpServletResponse.SC_BAD_REQUEST,
              String.format("invalid request. Form must contain %s, %s, %s. Year must be numeric.", artistKey, titleKey, yearKey));
            return;
        }

        Album requestedAlbum = new Album(-1L,
          profile.get(artistKey),
          profile.get(titleKey),
          Integer.parseInt(profile.get(yearKey)),
          IOUtils.toString(imagePart.getInputStream(), StandardCharsets.UTF_8)
        );

        Album createdAlbum = albumService.createAlbum(requestedAlbum);
        setResponse(messages, response, HttpServletResponse.SC_CREATED,
          new ImageMetaData(createdAlbum.getId(), imagePart.getSize())
        );
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