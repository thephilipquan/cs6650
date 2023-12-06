package org.philipquan.connection;

import static org.philipquan.RunConfig.REQUEST_RETRY_COUNT;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;

public class ClientManager {
    public CloseableHttpClient getClient() {
        return HttpClientBuilder.create()
          .setRetryHandler(new StandardHttpRequestRetryHandler(REQUEST_RETRY_COUNT, true))
          .build();
    }
}