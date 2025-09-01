package tracker.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.exceptions.NotFoundException;
import tracker.exceptions.OverlapException;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private HttpServer server;

    public static void main(String[] args) {
        final HttpTaskServer taskServer = new HttpTaskServer();
        taskServer.start();
//        taskServer.stop();
    }

    public void start() {
        final TaskManager taskManager = Managers.getDefault();



        final Epic epic = new Epic("task 1", "desc 1");
        final int epicId = taskManager.addEpic(epic);
        final Subtask subtask = new Subtask("subsub", "des");
        final LocalDateTime start = LocalDateTime.of(2025, 8, 31, 10, 0);
        final Duration duration = Duration.ofMinutes(30);
        subtask.setStartTimeAndDuration(start, duration);
        subtask.setEpicId(epicId);
        try {
            taskManager.addSubtask(subtask);
        } catch (OverlapException | NotFoundException e) {
            throw new RuntimeException(e);
        }



        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (IOException e) {
            System.out.println("Server start error");
            return;
        }
        final HttpHandler taskHandler = new TaskHandler(taskManager);
        final HttpHandler subtaskHandler = new SubtaskHandler(taskManager);
        final HttpHandler epicHandler = new EpicHandler(taskManager);
        server.createContext("/tasks", taskHandler);
        server.createContext("/subtasks", subtaskHandler);
        server.createContext("/epics", epicHandler);
        server.createContext("/history");
        server.createContext("/prioritized");
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(2);
        }
    }
}
