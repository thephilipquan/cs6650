package org.philipquan.connection;

import static org.philipquan.RunConfig.REQUEST_RETRY_COUNT;

import java.io.IOException;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.philipquan.RunConfig;
import org.philipquan.model.HttpGetAlbumId;

public class ClientManager {
    public CloseableHttpClient getClient() {
        return HttpClientBuilder.create()
          .setRetryHandler(new StandardHttpRequestRetryHandler(REQUEST_RETRY_COUNT, true))
          .build();
    }

    /**
     * @return {@code true} if a GET request to {@code this.hostUrl} returns a status code between
     * {@link HttpStatus#SC_OK} and {@link HttpStatus#SC_BAD_REQUEST}.
     */
    public Boolean hostUrlExists(String hostUrl) {
        HttpGet request = new HttpGet(String.format("%s/%s", hostUrl, RunConfig.ALBUM_ENDPOINT));
        int statusCode;
        try {
            statusCode = getClient().execute(request).getStatusLine().getStatusCode();
        } catch (IOException e) {
            return false;
        }
        return statusCode >= HttpStatus.SC_OK && statusCode <= HttpStatus.SC_BAD_REQUEST;
    }
}