import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import exceptions.ServerRequestException;
import httpclient.HttpClientPool;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;

class HttpClientPoolTest {

    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private CloseableHttpResponse response;

    @InjectMocks
    private HttpClientPool httpClientPool;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Inject the mock httpClient into a new HttpClientPool instance
        httpClientPool = HttpClientPool.getInstance();

        // Use reflection to set the httpClient mock in the singleton instance
        Field httpClientField = HttpClientPool.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(httpClientPool, httpClient);
    }

    @Test
    void testPost() throws IOException, ServerRequestException {
        String url = "http://example.com";
        String requestBody = "request";
        String responseBody = "response";

        HttpEntity entity = new StringEntity(responseBody);
        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(any(HttpPost.class))).thenReturn(response);

        // Ensure EntityUtils.toString() returns the response body
        String result = httpClientPool.post(url, requestBody);

        assertEquals(responseBody, result);
        verify(httpClient, times(1)).execute(any(HttpPost.class));
    }

    @Test
    void testPost_withException() throws IOException {
        String url = "http://example.com";
        String requestBody = "request";

        when(httpClient.execute(any(HttpPost.class))).thenThrow(new IOException());

        assertThrows(ServerRequestException.class, () -> httpClientPool.post(url, requestBody));
        verify(httpClient, times(1)).execute(any(HttpPost.class));
    }
}
