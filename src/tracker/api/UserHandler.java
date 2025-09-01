package tracker.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.controllers.TaskManager;

import java.io.IOException;

public class UserHandler extends BaseHttpHandler implements HttpHandler {
    public UserHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final String[] pathArray = exchange.getRequestURI().getPath().split("/");
        if (pathArray.length != 2 || !pathArray[1].equals("history") && !pathArray[1].equals("prioritized")) {
            sendNotFound(exchange);
            return;
        }
        final String methodName = exchange.getRequestMethod();
        if (!methodName.equals("GET")) {
            sendNotAllowed(exchange);
            return;
        }
        if (pathArray[1].equals("history")) {
            handleHistory(exchange);
            return;
        }
        handlePrioritized(exchange);
    }

    private void handleHistory(HttpExchange exchange) throws IOException {
       sendText(exchange, gson.toJson(taskManager.getHistory()));
    }

    private void handlePrioritized(HttpExchange exchange) throws IOException {
       sendText(exchange, gson.toJson(taskManager.getPrioritizedTasks()));
    }
}
