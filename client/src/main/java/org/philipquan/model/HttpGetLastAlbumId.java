package org.philipquan.model;

import org.apache.http.client.methods.HttpGet;
import org.philipquan.RunConfig;

public class HttpGetLastAlbumId extends HttpGet {

    public HttpGetLastAlbumId(String hostUrl) {
        super(String.format("%s/%s", hostUrl, RunConfig.LAST_ALBUM_ID_ENDPOINT));
    }
}