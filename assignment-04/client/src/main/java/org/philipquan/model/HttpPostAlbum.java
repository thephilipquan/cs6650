package org.philipquan.model;

import static org.philipquan.RunConfig.ALBUM_ENDPOINT;

import java.net.URI;
import org.apache.http.client.methods.HttpPost;

public class HttpPostAlbum extends HttpPost {

    public HttpPostAlbum(String hostUrl) {
        super(URI.create(String.format("%s/%s",
          hostUrl,
          ALBUM_ENDPOINT
        )));
    }
}