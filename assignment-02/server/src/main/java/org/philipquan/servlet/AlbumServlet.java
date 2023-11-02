package org.philipquan.servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
@MultipartConfig
public class AlbumServlet extends HttpServlet {

    private AlbumDao albumService;

    @Override
    public void init() throws ServletException {
        this.albumService = AlbumDao.getInstance();
    }

    /**
     * Get album by key. Returns {@link HttpServletResponse#SC_BAD_REQUEST} if albumId is missing or the value is not numeric.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
        response.setContentType("application/json");
        Map<String, Object> messages = new HashMap<>();

        final String albumIdKey = "albumId";
        String trial = request.getParameter(albumIdKey);
        if (trial == null || trial.trim().isEmpty() || !isNumeric(trial)) {
            setResponse(messages, response, HttpServletResponse.SC_BAD_REQUEST, "invalid request: missing parameter \"" + albumIdKey + "\"");
            return;
        }
        final Long albumId = Long.parseLong(trial);
        Album album = albumService.getAlbumById(albumId);
        if (album == null) {
            setResponse(messages, response, HttpServletResponse.SC_NOT_FOUND, albumIdKey + ": " + albumId + " not found");
        } else {
            setResponse(messages, response, HttpServletResponse.SC_OK, album);
        }
    }

    /**
     * Returns the new key and size of an image in bytes.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
        response.setContentType("application/json");
        Map<String, Object> messages = new HashMap<>();
        final String imageKey = Album.IMAGE_KEY.toLowerCase();
        final String profileKey = "profile";
        List<String> required = null;

        required = Arrays.asList(imageKey, profileKey);
        if (!streamContainsAllList(request.getParts().stream().map(Part::getName), required)) {
            setResponse(messages, response, HttpServletResponse.SC_BAD_REQUEST, "invalid request. Form must contain " + required + ".");
            return;
        }

        required = Arrays.asList(Album.ARTIST_KEY.toLowerCase(), Album.TITLE_KEY.toLowerCase(), Album.YEAR_KEY.toLowerCase());
        Map<String, String> profile = partToMap(request.getPart(profileKey));
        if (!streamContainsAllList(profile.keySet().stream(), required)) {
            setResponse(messages, response, HttpServletResponse.SC_BAD_REQUEST, "invalid request. Form must contain " + required + ".");
            return;
        }

        String image = IOUtils.toString(request.getPart(imageKey).getInputStream(), StandardCharsets.UTF_8);
        Album requestedAlbum = new Album(
          -1L,
          profile.get(Album.ARTIST_KEY.toLowerCase()),
          profile.get(Album.TITLE_KEY.toLowerCase()),
          Integer.parseInt(profile.get(Album.YEAR_KEY.toLowerCase())),
          image
        );

        Album createdAlbum = albumService.createAlbum(requestedAlbum);
        setResponse(
          messages,
          response,
          HttpServletResponse.SC_CREATED,
          new ImageMetaData(createdAlbum.getId(), request.getPart(imageKey).getSize())
        );
    }

    private void setResponse(Map<String, Object> messages, HttpServletResponse response, int statusCode, Object responseObject)
      throws IOException {
        messages.put("status", statusCode);
        messages.put("message", responseObject);
        response.setStatus(statusCode);
        response.getWriter().write(new Gson().toJson(messages));
    }

    /**
     * @param value the value to check if it is wholly a number.
     * @return true if the string is a number.
     */
    private boolean isNumeric(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns true if the stream contains all values within {@code list}.
     *
     * @param stream a {@link Stream<String>} of Strings.
     * @param list the list of wanted values within the stream.
     * @return {@code true} if the stream contains only all values within {@code list}.
     */
    private boolean streamContainsAllList(Stream<String> stream, List<String> list) {
        List<String> result = stream.filter(list::contains).collect(Collectors.toList());
        return result.size() == list.size(); // Bug if repetitive keys but its okay for hw.
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
        return new Gson().fromJson(
          json,
          new TypeToken<Map<String, String>>() {}.getType()
        );
    }

}