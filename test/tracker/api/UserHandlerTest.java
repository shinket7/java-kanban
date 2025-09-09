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
import tracker.model.Task;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tracker.api.HttpTestHelper.sendRequest;

public class UserHandlerTest {
    private Task task1;
    private Task task2;
    private Subtask subtask1;
    private Subtask subtask2;
    private Epic epic;
    private TaskManager taskManager;
    private Gson gson;
    private HttpTaskServer server;

    @BeforeEach
    void beforeEach() {
        task1 = new Task("task1", "desc task1");
        task2 = new Task("task2", "desc task2");
        subtask1 = new Subtask("subtask1", "desc subtask1");
        subtask2 = new Subtask("subtask2", "desc subtask2");
        epic = new Epic("epic", "desc epic");
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
    void shouldGetHistory() throws OverlapException, NotFoundException {
        final int task1Id = taskManager.addTask(task1);
        final int task2Id = taskManager.addTask(task2);
        final int subtask1Id = taskManager.addSubtask(subtask1);
        final int subtask2Id = taskManager.addSubtask(subtask2);
        final int epicId = taskManager.addEpic(epic);
        taskManager.getTaskById(task1Id);
        taskManager.getEpicById(epicId);
        taskManager.getSubtaskById(subtask2Id);
        taskManager.getTaskById(task2Id);
        taskManager.getSubtaskById(subtask1Id);
        final String expectedJson = gson.toJson(List.of(task1, epic, subtask2, task2, subtask1));
        final HttpResponse<String> response = sendRequest(Method.GET, "/history", "");
        assertEquals(200, response.statusCode(), "GET /history should return code 200");
        assertEquals(expectedJson, response.body(), "GET /history should return history in json");
    }

    @Test
    void shouldGetPrioritized() throws OverlapException, NotFoundException{
        final LocalDateTime startTime1 = LocalDateTime.of(2025, 1, 1, 10, 0);
        final LocalDateTime startTime2 = LocalDateTime.of(2025, 1, 1, 11, 0);
        final LocalDateTime startTime3 = LocalDateTime.of(2025, 1, 1, 12, 0);
        final Duration duration = Duration.ofMinutes(30);
        task1.setStartTimeAndDuration(startTime2, duration);
        subtask1.setStartTimeAndDuration(startTime3, duration);
        subtask2.setStartTimeAndDuration(startTime1, duration);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addEpic(epic);
        final String expectedJson = gson.toJson(List.of(subtask2, task1, subtask1));
        final HttpResponse<String> response = sendRequest(Method.GET, "/prioritized", "");
        assertEquals(200, response.statusCode(), "GET /prioritized should return code 200");
        assertEquals(expectedJson, response.body(), "GET /prioritized should return prioritized lists in json");
    }
}
