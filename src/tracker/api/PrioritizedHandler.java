package tracker.api;

import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {
    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void processGet(HttpExchange exchange, String[] pathArray) throws IOException {
        if (pathArray.length != 2 || !pathArray[1].equals("prioritized")) {
            sendNotFound(exchange);
            return;
        }

        handlePrioritized(exchange);
    }

    private void handlePrioritized(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskManager.getPrioritizedTasks()));
    }
}
