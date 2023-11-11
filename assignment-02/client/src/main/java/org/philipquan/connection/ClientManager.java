package org.philipquan.connection;

import java.io.IOException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.philipquan.Main;

public class ClientManager {

    private CloseableHttpClient client;

    public ClientManager() {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(40);
        this.client = HttpClientBuilder.create()
          .setRetryHandler(new StandardHttpRequestRetryHandler(Main.REQUEST_RETRY_COUNT, true))
          .setConnectionManager(manager)
          .build();
    }

    public CloseableHttpClient getClient() {
        return this.client;
    }

    public void closeClient() {
        try {
            this.client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}