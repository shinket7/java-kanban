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

public class EpicHandlerTest {
    private Epic epic1;
    private Epic epic2;
    private TaskManager taskManager;
    private Gson gson;
    private HttpTaskServer server;

    @BeforeEach
    void beforeEach() {
        epic1 = new Epic("epic1", "desc epic1");
        epic2 = new Epic("epic2", "desc epic2");
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
    void shouldGetAllEpics() {
        final int epic1Id = taskManager.addEpic(epic1);
        final int epic2Id = taskManager.addEpic(epic2);
        epic1.setTaskId(epic1Id);
        epic2.setTaskId(epic2Id);
        final String expectedJson = gson.toJson(List.of(epic1, epic2));
        final HttpResponse<String> response = sendRequest(Method.GET, "/epics", "");
        assertEquals(200, response.statusCode(), "GET /epics should return code 200");
        assertEquals(expectedJson, response.body(), "GET /epics should return all epics in json");
    }

    @Test
    void shouldGetEpicById() {
        final int epic1Id = taskManager.addEpic(epic1);
        epic1.setTaskId(epic1Id);
        final String expectedJson = gson.toJson(epic1);
        final HttpResponse<String> response = sendRequest(Method.GET, "/epics/" + epic1Id, "");
        assertEquals(200, response.statusCode(), "GET /epics/{id} should return code 200");
        assertEquals(expectedJson, response.body(), "GET /epics/{id} should return epic in json by its id");
    }

    @Test
    void shouldGetEpicSubtasks() throws OverlapException, NotFoundException {
        final int epic1Id = taskManager.addEpic(epic1);
        final int epic2Id = taskManager.addEpic(epic2);
        final Subtask subtask1 = new Subtask("subtask1", "subtask1 desc");
        final Subtask subtask2 = new Subtask("subtask2", "subtask2 desc");
        final Subtask subtask3 = new Subtask("subtask3", "subtask3 desc");
        subtask1.setEpicId(epic1Id);
        subtask2.setEpicId(epic2Id);
        subtask3.setEpicId(epic1Id);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        final String expectedJson = gson.toJson(List.of(subtask1, subtask3));
        final HttpResponse<String> response = sendRequest(Method.GET, "/epics/" + epic1Id + "/subtasks", "");
        assertEquals(200, response.statusCode(), "GET /epics/{id}/subtasks should return code 200");
        assertEquals(expectedJson, response.body(),
                "GET /epics/{id}/subtasks should return all epic subtasks in json");
    }

    @Test
    void shouldAddEpic() {
        final HttpResponse<String> response = sendRequest(Method.POST, "/epics", gson.toJson(epic1));
        assertEquals(201, response.statusCode(), "POST /epics should return 201");
        assertEquals(1, taskManager.getEpics().size(),
                "POST /epics should add new epic if without id");
    }

    @Test
    void shouldUpdateEpic() {
        final int epic1Id = taskManager.addEpic(epic1);
        epic1.setTaskId(epic1Id);
        final HttpResponse<String> response = sendRequest(Method.POST, "/epics", gson.toJson(epic1));
        assertEquals(201, response.statusCode(), "POST /epics should return 201");
        assertEquals(1, taskManager.getEpics().size(),
                "POST /epics should not add new epic if with id");
    }

    @Test
    void shouldDeleteEpic() {
        final int epic1Id = taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        final HttpResponse<String> response = sendRequest(Method.DELETE, "/epics/" + epic1Id, "");
        assertEquals(200, response.statusCode(), "DELETE /epics/{id} should return 200");
        assertEquals(List.of(epic2), taskManager.getEpics(), "DELETE /epics/{id} should delete epic by its id");
    }
}
