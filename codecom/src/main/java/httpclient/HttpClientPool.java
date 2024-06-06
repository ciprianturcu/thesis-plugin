package httpclient;

import exceptions.ServerRequestException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.rmi.server.ServerCloneException;

public final class HttpClientPool {

    private static volatile HttpClientPool instance;
    private final PoolingHttpClientConnectionManager connectionManager;
    private final CloseableHttpClient httpClient;
    private static final int CONNECTION_TIMEOUT = 15000;
    private static final int SOCKET_TIMEOUT = 15000;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientPool.class);

    private HttpClientPool() {
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200); // Total connections in the pool
        connectionManager.setDefaultMaxPerRoute(100); // Max connections per route

        httpClient = createHttpClient();
    }

    public static HttpClientPool getInstance() {
        if (instance == null) {
            synchronized (HttpClientPool.class) {
                if (instance == null) {
                    instance = new HttpClientPool();
                }
            }
        }
        return instance;
    }

    private CloseableHttpClient createHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public String get(String url) throws ServerRequestException {
        HttpGet httpGet = new HttpGet(url);
        return executeRequest(httpGet);
    }

    public String post(String url, String code) throws ServerRequestException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", code);
        String json = jsonObject.toString();
        StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        return executeRequest(httpPost);
    }

    private String executeRequest(HttpRequestBase request) throws ServerRequestException {
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            String decodedUrl = URLDecoder.decode(request.getURI().toString(), StandardCharsets.UTF_8);
            LOGGER.error("Error during HTTP request for URL: {}; error: {} ", decodedUrl ,e.getMessage());
            throw new ServerRequestException("Request to server failed", e);
        } finally {
            request.releaseConnection();
        }
    }

    public void shutdown() {
        try {
            httpClient.close();
            connectionManager.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
