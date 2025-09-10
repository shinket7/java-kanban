package tracker.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import tracker.adapters.DurationTypeAdapter;
import tracker.adapters.LocalDateTimeTypeAdapter;
import tracker.controllers.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
    }

    public void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(200, resp.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(resp);
        }
    }

    public void sendCreated(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(201, 0);
        exchange.close();
    }

    public void sendBadRequest(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(400, 0);
        exchange.close();
    }

    public void sendNotFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, 0);
        exchange.close();
    }

    public void sendNotAllowed(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(405, 0);
        exchange.close();
    }

    public void sendHasOverlaps(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(406, 0);
        exchange.close();
    }
}
