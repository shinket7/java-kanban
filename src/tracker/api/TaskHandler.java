package tracker.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.controllers.TaskManager;
import tracker.exceptions.NotFoundException;
import tracker.exceptions.OverlapException;
import tracker.model.Task;
import tracker.model.TaskStatus;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
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
            if (!methodName.equals("GET") && !methodName.equals("DELETE")) {
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
            if (methodName.equals("DELETE")) {
                handleDelete(exchange, taskId);
                return;
            }
            handleGetById(exchange, taskId);
            return;
        }

        if (methodName.equals("GET")) {
            handleGet(exchange);
            return;
        }
        if (methodName.equals("POST")) {
            handlePost(exchange);
            return;
        }
        sendNotAllowed(exchange);
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

    private void handleGet(HttpExchange exchange) throws IOException {
        final List<Task> tasks = taskManager.getTasks();
        sendText(exchange, gson.toJson(tasks));
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
        final JsonElement status = bodyObj.get("status");
        final JsonElement taskId = bodyObj.get("taskId");
        final JsonElement startTime = bodyObj.get("startTime");
        final JsonElement duration = bodyObj.get("duration");

        if (summary == null || description == null || status == null || !summary.isJsonPrimitive()
                || !description.isJsonPrimitive() || !status.isJsonPrimitive()) {
            sendBadRequest(exchange);
            return;
        }
        final JsonPrimitive summaryPrim = bodyObj.getAsJsonPrimitive("summary");
        final JsonPrimitive descriptionPrim = bodyObj.getAsJsonPrimitive("description");
        final JsonPrimitive statusPrim = bodyObj.getAsJsonPrimitive("status");
        if (!summaryPrim.isString() || !descriptionPrim.isString() || !statusPrim.isString()) {
            sendBadRequest(exchange);
            return;
        }

        Integer taskIdInt = null;
        String startTimeStr = null;
        String durationStr = null;
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

        if (startTime != null && !startTime.isJsonNull()) {
            if (!startTime.isJsonPrimitive()) {
                sendBadRequest(exchange);
                return;
            }
            final JsonPrimitive startTimePrim = startTime.getAsJsonPrimitive();
            if (!startTimePrim.isString()) {
                sendBadRequest(exchange);
                return;
            }
            startTimeStr = startTimePrim.getAsString();
        }
        if (duration != null && !duration.isJsonNull()) {
            if (!duration.isJsonPrimitive()) {
                sendBadRequest(exchange);
                return;
            }
            final JsonPrimitive durationPrim = duration.getAsJsonPrimitive();
            if (!durationPrim.isString()) {
                sendBadRequest(exchange);
                return;
            }
            durationStr = durationPrim.getAsString();
        }

        final Task task = new Task(summaryPrim.getAsString(), descriptionPrim.getAsString());
        final TaskStatus taskStatus;
        switch (statusPrim.getAsString()) {
            case "DONE":
                taskStatus = TaskStatus.DONE;
                break;
            case "IN_PROGRESS":
                taskStatus = TaskStatus.IN_PROGRESS;
                break;
            default:
                taskStatus = TaskStatus.NEW;
        }
        task.setStatus(taskStatus);

        if (startTimeStr != null && durationStr != null) {
            final LocalDateTime startDT = LocalDateTime.parse(startTimeStr);
            final Duration durationDur = Duration.parse(durationStr);
            task.setStartTimeAndDuration(startDT, durationDur);
        }

        if (taskIdInt == null || taskIdInt == -1) {
            try {
                taskManager.addTask(task);
            } catch (OverlapException e) {
                sendHasOverlaps(exchange);
                return;
            }
        } else {
            try {
                taskManager.getTaskById(taskIdInt);
            } catch (NotFoundException e) {
                sendNotFound(exchange);
                return;
            }
            task.setTaskId(taskIdInt);
            try {
                taskManager.updateTask(task);
            } catch (OverlapException e) {
                sendHasOverlaps(exchange);
                return;
            }
        }
        sendCreated(exchange);
    }

    private void handleDelete(HttpExchange exchange, int taskId) throws IOException {
        try {
            taskManager.getTaskById(taskId);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
            return;
        }
        taskManager.deleteTaskById(taskId);
        sendText(exchange, "");
    }
}
