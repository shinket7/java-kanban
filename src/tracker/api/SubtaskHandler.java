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
import tracker.model.Subtask;
import tracker.model.TaskStatus;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    public SubtaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void processGet(HttpExchange exchange, String[] pathArray) throws IOException {
        if (pathArray.length > 3 || !pathArray[1].equals("subtasks")) {
            sendNotFound(exchange);
            return;
        }

        if (pathArray.length == 2) {
            handleGet(exchange);
            return;
        }

        final int taskId = parseTaskId(exchange, pathArray[2]);
        if (taskId != -1) {
            handleGetById(exchange, taskId);
        }
    }

    @Override
    protected void processPost(HttpExchange exchange, String[] pathArray) throws IOException {
        if (pathArray.length != 2 || !pathArray[1].equals("subtasks")) {
            sendNotFound(exchange);
            return;
        }

        handlePost(exchange);
    }

    @Override
    protected void processDelete(HttpExchange exchange, String[] pathArray) throws IOException {
        if (pathArray.length != 3 || !pathArray[1].equals("subtasks")) {
            sendNotFound(exchange);
            return;
        }

        final int taskId = parseTaskId(exchange, pathArray[2]);
        if (taskId != -1) {
            handleDelete(exchange, taskId);
        }
    }

    private void handleGetById(HttpExchange exchange, int subtaskId) throws IOException {
        final Subtask subtask;
        try {
            subtask = taskManager.getSubtaskById(subtaskId);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
            return;
        }
        sendText(exchange, gson.toJson(subtask));
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        final List<Subtask> subtasks = taskManager.getSubtasks();
        sendText(exchange, gson.toJson(subtasks));
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
        final JsonElement epicId = bodyObj.get("epicId");
        final JsonElement taskId = bodyObj.get("taskId");
        final JsonElement startTime = bodyObj.get("startTime");
        final JsonElement duration = bodyObj.get("duration");

        if (summary == null || description == null || status == null || epicId == null || !summary.isJsonPrimitive()
                || !description.isJsonPrimitive() || !status.isJsonPrimitive() || !epicId.isJsonPrimitive()) {
            sendBadRequest(exchange);
            return;
        }
        final JsonPrimitive summaryPrim = bodyObj.getAsJsonPrimitive("summary");
        final JsonPrimitive descriptionPrim = bodyObj.getAsJsonPrimitive("description");
        final JsonPrimitive statusPrim = bodyObj.getAsJsonPrimitive("status");
        final JsonPrimitive epicIdPrim = bodyObj.getAsJsonPrimitive("epicId");
        if (!summaryPrim.isString() || !descriptionPrim.isString() || !statusPrim.isString()
                || !epicIdPrim.isNumber()) {
            sendBadRequest(exchange);
            return;
        }
        final int epicIdInt = epicIdPrim.getAsInt();

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

        final Subtask subtask = new Subtask(summaryPrim.getAsString(), descriptionPrim.getAsString());
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
        subtask.setStatus(taskStatus);
        subtask.setEpicId(epicIdInt);

        if (startTimeStr != null && durationStr != null) {
            final LocalDateTime startDT = LocalDateTime.parse(startTimeStr);
            final Duration durationDur = Duration.parse(durationStr);
            subtask.setStartTimeAndDuration(startDT, durationDur);
        }

        if (taskIdInt == null || taskIdInt == -1) {
            try {
                taskManager.addSubtask(subtask);
            } catch (OverlapException e) {
                sendHasOverlaps(exchange);
                return;
            } catch (NotFoundException e) {
                sendNotFound(exchange);
                return;
            }
        } else {
            try {
                taskManager.getSubtaskById(taskIdInt);
            } catch (NotFoundException e) {
                sendNotFound(exchange);
                return;
            }
            subtask.setTaskId(taskIdInt);
            try {
                taskManager.updateSubtask(subtask);
            } catch (OverlapException e) {
                sendHasOverlaps(exchange);
                return;
            } catch (NotFoundException e) {
                sendNotFound(exchange);
                return;
            }
        }
        sendCreated(exchange);
    }

    private void handleDelete(HttpExchange exchange, int taskId) throws IOException {
        try {
            taskManager.getSubtaskById(taskId);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
            return;
        }
        taskManager.deleteSubtaskById(taskId);
        sendText(exchange, "");
    }
}
