package org.philipquan.connection.runnable;

import java.io.IOException;
import java.util.regex.Matcher;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.philipquan.RunConfig;
import org.philipquan.connection.ClientManager;
import org.philipquan.model.HttpGetLastAlbumId;
import org.philipquan.model.SynchronizedInteger;

public class GetLastAlbumIdRunnable implements Runnable {

    private final ClientManager clientManager;
    private final String hostUrl;
    private final SynchronizedInteger lastAlbumId;
    private final long delayInSeconds;

    public GetLastAlbumIdRunnable(ClientManager clientManager, String hostUrl, SynchronizedInteger lastAlbumId, long delayInSeconds) {
        this.clientManager = clientManager;
        this.hostUrl = hostUrl;
        this.lastAlbumId = lastAlbumId;
        this.delayInSeconds = delayInSeconds;
    }

    @Override
    public void run() {
        CloseableHttpClient client = this.clientManager.getClient();
        HttpGet request = new HttpGetLastAlbumId(this.hostUrl);
        while (!Thread.currentThread().isInterrupted()) {
            CloseableHttpResponse response;
            String responseBody = "";
            try {
                response = client.execute(request);
                responseBody = EntityUtils.toString(response.getEntity());
                EntityUtils.consume(response.getEntity());
                response.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Matcher match = RunConfig.ALBUMID_PATTERN.matcher(responseBody);
            if (!match.find()) {
                throw new RuntimeException(String.format("no match found in %s", responseBody));
            }
            this.lastAlbumId.setValue(Integer.parseInt(match.group(1)));

            try {
                Thread.sleep(this.delayInSeconds * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}