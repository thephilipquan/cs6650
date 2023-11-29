package org.philipquan.connection;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.philipquan.Main;

public class ClientManager {
    public CloseableHttpClient getClient() {
        return HttpClientBuilder.create()
          .setRetryHandler(new StandardHttpRequestRetryHandler(Main.REQUEST_RETRY_COUNT, true))
          .build();
    }
}