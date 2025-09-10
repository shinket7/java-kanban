package tracker.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.adapters.DurationTypeAdapter;
import tracker.adapters.LocalDateTimeTypeAdapter;
import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.exceptions.OverlapException;
import tracker.model.Task;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tracker.api.HttpTestHelper.sendRequest;

public class TaskHandlerTest {
    private Task task1;
    private Task task2;
    private TaskManager taskManager;
    private Gson gson;
    private HttpTaskServer server;

    @BeforeEach
    void beforeEach() {
        task1 = new Task("task1", "desc task1");
        task2 = new Task("task2", "desc task2");
        taskManager = Managers.getDefault();
        gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
        server = new HttpTaskServer();
        server.start(taskManager);
    }

    @AfterEach
    void afterEach() {
        server.stop();
    }

    @Test
    void shouldGetAllTasks() throws OverlapException {
        final int task1Id = taskManager.addTask(task1);
        final int task2Id = taskManager.addTask(task2);
        task1.setTaskId(task1Id);
        task2.setTaskId(task2Id);
        final String expectedJson = gson.toJson(List.of(task1, task2));
        final HttpResponse<String> response = sendRequest(Method.GET, "/tasks", "");
        assertEquals(200, response.statusCode(), "GET /tasks should return code 200");
        assertEquals(expectedJson, response.body(), "GET /tasks should return all tasks in json");
    }

    @Test
    void shouldGetTaskById() throws OverlapException {
        final int task1Id = taskManager.addTask(task1);
        task1.setTaskId(task1Id);
        final String expectedJson = gson.toJson(task1);
        final HttpResponse<String> response = sendRequest(Method.GET, "/tasks/" + task1Id, "");
        assertEquals(200, response.statusCode(), "GET /tasks/{id} should return code 200");
        assertEquals(expectedJson, response.body(), "GET /tasks/{id} should return task in json by its id");
    }

    @Test
    void shouldAddTask() {
        final HttpResponse<String> response = sendRequest(Method.POST, "/tasks", gson.toJson(task1));
        assertEquals(201, response.statusCode(), "POST /tasks should return 201");
        assertEquals(1, taskManager.getTasks().size(),
                "POST /tasks should add new task if without id");
    }

    @Test
    void shouldUpdateTask() throws OverlapException {
        final int task1Id = taskManager.addTask(task1);
        task1.setTaskId(task1Id);
        final HttpResponse<String> response = sendRequest(Method.POST, "/tasks", gson.toJson(task1));
        assertEquals(201, response.statusCode(), "POST /tasks should return 201");
        assertEquals(1, taskManager.getTasks().size(),
                "POST /tasks should not add new task if with id");
    }

    @Test
    void shouldDeleteTask() throws OverlapException {
        final int task1Id = taskManager.addTask(task1);
        taskManager.addTask(task2);
        final HttpResponse<String> response = sendRequest(Method.DELETE, "/tasks/" + task1Id, "");
        assertEquals(200, response.statusCode(), "DELETE /tasks/{id} should return 200");
        assertEquals(List.of(task2), taskManager.getTasks(), "DELETE /tasks/{id} should delete task by its id");
    }
}
