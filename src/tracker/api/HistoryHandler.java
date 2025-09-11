package tracker.api;

import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void processGet(HttpExchange exchange, String[] pathArray) throws IOException {
        if (pathArray.length != 2 || !pathArray[1].equals("history")) {
            sendNotFound(exchange);
            return;
        }

        handleHistory(exchange);
    }

    private void handleHistory(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskManager.getHistory()));
    }
}
