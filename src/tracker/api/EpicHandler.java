package tracker.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.exceptions.NotFoundException;
import tracker.model.Epic;
import tracker.model.Subtask;

import java.io.IOException;
import java.util.List;

public class EpicHandler extends BaseHttpHandler {
    public EpicHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void processGet(HttpExchange exchange, String[] pathArray) throws IOException {
        if (pathArray.length > 4 || !pathArray[1].equals("epics")
                || pathArray.length == 4 && !pathArray[3].equals("subtasks")) {
            sendNotFound(exchange);
            return;
        }

        if (pathArray.length == 2) {
            handleGet(exchange);
            return;
        }

        final int taskId = parseTaskId(exchange, pathArray[2]);
        if (taskId != -1) {
            if (pathArray.length == 4) {
                handleGetSubtasks(exchange, taskId);
                return;
            }
            handleGetById(exchange, taskId);
        }
    }

    @Override
    protected void processPost(HttpExchange exchange, String[] pathArray) throws IOException {
        if (pathArray.length != 2 || !pathArray[1].equals("epics")) {
            sendNotFound(exchange);
            return;
        }

        handlePost(exchange);
    }

    @Override
    protected void processDelete(HttpExchange exchange, String[] pathArray) throws IOException {
        if (pathArray.length != 3 || !pathArray[1].equals("epics")) {
            sendNotFound(exchange);
            return;
        }

        final int taskId = parseTaskId(exchange, pathArray[2]);
        if (taskId != -1) {
            handleDelete(exchange, taskId);
        }
    }

    private void handleGetById(HttpExchange exchange, int epicId) throws IOException {
        final Epic epic;
        try {
            epic = taskManager.getEpicById(epicId);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
            return;
        }
        sendText(exchange, gson.toJson(epic));
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        final List<Epic> epics = taskManager.getEpics();
        sendText(exchange, gson.toJson(epics));
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        final JsonElement body = JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes()));
        if (!body.isJsonObject()) {
            sendBadRequest(exchange);
            return;
        }
        final JsonObject bodyObj = body.getAsJsonObject();

        final JsonElement summary = bodyObj.get("summary");
        final JsonElement description = bodyObj.get("description");
        final JsonElement taskId = bodyObj.get("taskId");

        if (summary == null || description == null || !summary.isJsonPrimitive() || !description.isJsonPrimitive()) {
            sendBadRequest(exchange);
            return;
        }
        final JsonPrimitive summaryPrim = bodyObj.getAsJsonPrimitive("summary");
        final JsonPrimitive descriptionPrim = bodyObj.getAsJsonPrimitive("description");
        if (!summaryPrim.isString() || !descriptionPrim.isString()) {
            sendBadRequest(exchange);
            return;
        }

        Integer taskIdInt = null;
        if (taskId != null && !taskId.isJsonNull()) {
            if (!taskId.isJsonPrimitive()) {
                sendBadRequest(exchange);
                return;
            }
            final JsonPrimitive taskIdPrim = taskId.getAsJsonPrimitive();
            if (!taskIdPrim.isNumber()) {
                sendBadRequest(exchange);
                return;
            }
            taskIdInt = taskIdPrim.getAsInt();
        }

        final Epic epic = new Epic(summaryPrim.getAsString(), descriptionPrim.getAsString());

        if (taskIdInt == null || taskIdInt == -1) {
            taskManager.addEpic(epic);
        } else {
            try {
                taskManager.getEpicById(taskIdInt);
            } catch (NotFoundException e) {
                sendNotFound(exchange);
                return;
            }
            epic.setTaskId(taskIdInt);
            taskManager.updateEpic(epic);
        }
        sendCreated(exchange);
    }

    private void handleDelete(HttpExchange exchange, int epicId) throws IOException {
        try {
            taskManager.getEpicById(epicId);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
            return;
        }
        taskManager.deleteEpicById(epicId);
        sendText(exchange, "");
    }

    private void handleGetSubtasks(HttpExchange exchange, int epicId) throws IOException {
        final List<Subtask> subtasks;
        try {
            subtasks = taskManager.getEpicSubtasks(epicId);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
            return;
        }
        sendText(exchange, gson.toJson(subtasks));
    }
}
