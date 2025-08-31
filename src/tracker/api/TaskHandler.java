package tracker.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.controllers.TaskManager;
import tracker.exceptions.NotFoundException;
import tracker.model.Task;

import java.io.IOException;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    public TaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final String[] pathArray = exchange.getRequestURI().getPath().split("/");
        if (pathArray.length > 3 || !pathArray[1].equals("tasks")) {
            sendNotFound(exchange);
            return;
        }
        final String methodName = exchange.getRequestMethod();
        if (pathArray.length == 3) {
            if (!methodName.equals("GET")) {
                sendNotAllowed(exchange);
                return;
            }
            final int taskId;
            try {
                taskId = Integer.parseInt(pathArray[2]);
            } catch (NumberFormatException e) {
                sendBadRequest(exchange);
                return;
            }
            handleGetById(exchange, taskId);
            return;
        }
        if (methodName.equals("GET")) {
            handleGet(exchange);
            return;
        }
        handlePost(exchange);
    }

    private void handleGetById(HttpExchange exchange, int taskId) throws IOException {
        final Task task;
        try {
            task = taskManager.getTaskById(taskId);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
            return;
        }
        sendText(exchange, gson.toJson(task));
    }

    private void handleGet(HttpExchange exchange) throws IOException  {
        final List<Task> tasks = taskManager.getTasks();
        sendText(exchange, gson.toJson(tasks));
    }

    private void handlePost(HttpExchange exchange) throws IOException  {

    }
}
