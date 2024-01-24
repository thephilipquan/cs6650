package org.philipquan.model;

import org.apache.http.client.methods.HttpGet;
import org.philipquan.RunConfig;

public class HttpGetAlbumId extends HttpGet {

    public HttpGetAlbumId(String hostUrl, Integer albumId) {
        super(String.format("%s/%s?albumId=%d", hostUrl, RunConfig.ALBUM_ENDPOINT, albumId));
    }
}