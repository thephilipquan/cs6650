package org.philipquan.model;

import static org.philipquan.RunConfig.REACTION_ENDPOINT;

import java.net.URI;
import org.apache.http.client.methods.HttpPost;

public class HttpPostReaction extends HttpPost {

    public HttpPostReaction(String hostUrl, String action, Integer albumId) {
        this.setURI(URI.create(String.format("%s/%s/%s/%d",
          hostUrl,
          REACTION_ENDPOINT,
          action,
          albumId
        )));
    }
}