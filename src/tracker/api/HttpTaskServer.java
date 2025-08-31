package tracker.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.model.Task;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private HttpServer server;

    public static void main(String[] args) {
        final HttpTaskServer taskServer = new HttpTaskServer();
        taskServer.start();
        taskServer.stop();
    }

    public void start() {
        final TaskManager taskManager = Managers.getDefault();

        taskManager.addTask(new Task("task 1", "desc 1"));

        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (IOException e) {
            System.out.println("Server start error");
            return;
        }
        final HttpHandler taskHandler = new TaskHandler(taskManager);
        server.createContext("/tasks", taskHandler);
        server.createContext("/subtasks");
        server.createContext("/epics");
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
