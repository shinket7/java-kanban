package tracker.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.exceptions.NotFoundException;
import tracker.exceptions.OverlapException;
import tracker.model.Epic;
import tracker.model.Subtask;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tracker.api.HttpTestHelper.sendRequest;

public class SubtaskHandlerTest {
    private Subtask subtask1;
    private Subtask subtask2;
    private TaskManager taskManager;
    private Gson gson;
    private HttpTaskServer server;

    @BeforeEach
    void beforeEach() {
        subtask1 = new Subtask("subtask1", "desc subtask1");
        subtask2 = new Subtask("subtask2", "desc subtask2");
        final Epic epic = new Epic("epic", "epic desc");
        taskManager = Managers.getDefault();

        final int epicId = taskManager.addEpic(epic);
        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);

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
    void shouldGetAllTasks() throws OverlapException, NotFoundException {
        final int subtask1Id = taskManager.addSubtask(subtask1);
        final int subtask2Id = taskManager.addSubtask(subtask2);
        subtask1.setTaskId(subtask1Id);
        subtask2.setTaskId(subtask2Id);
        final String expectedJson = gson.toJson(List.of(subtask1, subtask2));
        final HttpResponse<String> response = sendRequest(Method.GET, "/subtasks", "");
        assertEquals(200, response.statusCode(), "GET /subtasks should return code 200");
        assertEquals(expectedJson, response.body(), "GET /subtasks should return all subtasks in json");
    }

    @Test
    void shouldGetTaskById() throws OverlapException, NotFoundException {
        final int subtask1Id = taskManager.addSubtask(subtask1);
        subtask1.setTaskId(subtask1Id);
        final String expectedJson = gson.toJson(subtask1);
        final HttpResponse<String> response = sendRequest(Method.GET, "/subtasks/" + subtask1Id, "");
        assertEquals(200, response.statusCode(), "GET /subtasks/{id} should return code 200");
        assertEquals(expectedJson, response.body(),
                "GET /subtasks/{id} should return subtask in json by its id");
    }

    @Test
    void shouldAddSubtask() {
        final HttpResponse<String> response = sendRequest(Method.POST, "/subtasks", gson.toJson(subtask1));
        assertEquals(201, response.statusCode(), "POST /subtasks should return 201");
        assertEquals(1, taskManager.getSubtasks().size(),
                "POST /subtasks should add new subtask if without id");
    }

    @Test
    void shouldUpdateSubtask() throws OverlapException, NotFoundException {
        final int subtask1Id = taskManager.addSubtask(subtask1);
        subtask1.setTaskId(subtask1Id);
        final HttpResponse<String> response = sendRequest(Method.POST, "/subtasks", gson.toJson(subtask1));
        assertEquals(201, response.statusCode(), "POST /subtasks should return 201");
        assertEquals(1, taskManager.getSubtasks().size(),
                "POST /subtasks should not add new subtask if with id");
    }

    @Test
    void shouldDeleteSubtask() throws OverlapException, NotFoundException {
        final int subtask1Id = taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        final HttpResponse<String> response = sendRequest(Method.DELETE, "/subtasks/" + subtask1Id, "");
        assertEquals(200, response.statusCode(), "DELETE /subtasks/{id} should return 200");
        assertEquals(List.of(subtask2), taskManager.getSubtasks(),
                "DELETE /subtasks/{id} should delete subtask by its id");
    }
}
