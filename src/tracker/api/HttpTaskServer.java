package tracker.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import tracker.controllers.Managers;
import tracker.controllers.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private HttpServer server;

    public static void main(String[] args) {
        final HttpTaskServer taskServer = new HttpTaskServer();
        taskServer.start(Managers.getDefault());
        taskServer.stop();
    }

    public void start(TaskManager taskManager) {
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (IOException e) {
            System.out.println("Server start error");
            return;
        }
        final HttpHandler taskHandler = new TaskHandler(taskManager);
        final HttpHandler subtaskHandler = new SubtaskHandler(taskManager);
        final HttpHandler epicHandler = new EpicHandler(taskManager);
        final HttpHandler historyHandler = new HistoryHandler(taskManager);
        final HttpHandler prioritizedHandler = new PrioritizedHandler(taskManager);
        final HttpHandler baseHttpHandler = new BaseHttpHandler(taskManager);
        server.createContext("/tasks", taskHandler);
        server.createContext("/subtasks", subtaskHandler);
        server.createContext("/epics", epicHandler);
        server.createContext("/history", historyHandler);
        server.createContext("/prioritized", prioritizedHandler);
        server.createContext("/", baseHttpHandler);
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}
