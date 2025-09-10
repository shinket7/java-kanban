package tracker.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.adapters.DurationTypeAdapter;
import tracker.adapters.LocalDateTimeTypeAdapter;
import tracker.controllers.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHttpHandler implements HttpHandler {
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

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final String methodName = exchange.getRequestMethod();
        final String[] pathArray = exchange.getRequestURI().getPath().split("/");
        switch (methodName) {
            case "GET":
                processGet(exchange, pathArray);
                break;
            case "POST":
                processPost(exchange, pathArray);
                break;
            case "DELETE":
                processDelete(exchange, pathArray);
                break;
            default:
                sendNotAllowed(exchange);
        }
    }

    protected void processGet(HttpExchange exchange, String[] pathArray) throws IOException {
        sendNotAllowed(exchange);
    }

    protected void processPost(HttpExchange exchange, String[] pathArray) throws IOException {
        sendNotAllowed(exchange);
    }

    protected void processDelete(HttpExchange exchange, String[] pathArray) throws IOException {
        sendNotAllowed(exchange);
    }

    protected int parseTaskId(HttpExchange exchange, String taskIdString) throws IOException {
        try {
            return Integer.parseInt(taskIdString);
        } catch (NumberFormatException e) {
            sendBadRequest(exchange);
        }
        return -1;
    }

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(200, resp.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(resp);
        }
    }

    protected void sendCreated(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(201, 0);
        exchange.close();
    }

    protected void sendBadRequest(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(400, 0);
        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, 0);
        exchange.close();
    }

    protected void sendNotAllowed(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(405, 0);
        exchange.close();
    }

    protected void sendHasOverlaps(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(406, 0);
        exchange.close();
    }
}
