package org.philipquan.servlet.validator;

import static org.philipquan.servlet.utility.ServletResponseHelper.isNumeric;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.philipquan.dal.AlbumDao;

public class PostReviewValidator extends RequestValidator {

    private static final int actionIndex = 1;
    private static final int albumIdIndex = 2;
    protected AlbumDao albumDao;

    public PostReviewValidator() {
        super();
        this.albumDao = AlbumDao.getInstance();
    }
    @Override
    public Boolean validate(HttpServletRequest request) {
        String[] variables = request.getPathInfo().split("/");
        if (variables.length != 3) {
            this.status = HttpServletResponse.SC_BAD_REQUEST;
            this.errorMessage = "invalid request: expected server context /reviews/{like/dislike}/{albumId}";
            return false;
        }

        final String action = variables[actionIndex];
        final String albumId = variables[albumIdIndex];
        this.map.put("action", action);
        this.map.put("albumId", albumId);

        boolean isValid = false;
        if (!(action.equals("like") || action.equals("dislike"))) {
            this.status = HttpServletResponse.SC_BAD_REQUEST;
            this.errorMessage = "invalid request: action can only be like or dislike";
        } else if (!isNumeric(albumId)) {
            this.status = HttpServletResponse.SC_BAD_REQUEST;
            this.errorMessage = "invalid request: albumId must be numeric";
        } else if (this.albumDao.getAlbumById(Long.parseLong(albumId)) == null) {
            this.status = HttpServletResponse.SC_NOT_FOUND;
            this.errorMessage = "invalid request: albumId does not exist";
        } else {
            isValid = true;
        }

        return isValid;
    }
}