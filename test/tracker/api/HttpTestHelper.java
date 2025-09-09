package tracker.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpTestHelper {
    public static HttpResponse<String> sendRequest(Method method, String path, String body) {
        final HttpClient client = HttpClient.newHttpClient();
        final URI uri = URI.create("http://localhost:8080" + path);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri);
        switch (method) {
            case POST:
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
                break;
            case DELETE:
                requestBuilder.DELETE();
                break;
            default:
                requestBuilder.GET();
        }
        final HttpRequest request = requestBuilder.build();
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
