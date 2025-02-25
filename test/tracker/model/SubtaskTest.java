package tracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubtaskTest {

    private final String summary = "Initial summary for test";
    private final String description = "Initial description for test";
    private Subtask subtask;

    @BeforeEach
    void BeforeEach() {
        subtask = new Subtask(summary, description);
    }

    @Test
    void subTaskShouldExtendTask() {
        assertTrue(Task.class.isAssignableFrom(Subtask.class), "`Subtask` should extend `Task` class");
    }

    @Test
    void shouldReturnInitialEpicIdForNewSubtask() {
        assertEquals(-1, subtask.getEpicId(),
                "`getEpicId()` should return initial task id -1 for a new subtask");
    }

    @Test
    void shouldSetEpicId() {
        final int newEpicId = 38;
        subtask.setEpicId(newEpicId);
        assertEquals(newEpicId, subtask.getEpicId(), "`setEpicId()` should set epic id");
    }

    @Test
    void shouldReturnCorrectString() {
        final String expectedString = "Subtask{summary='" + summary + "'" + ", description='" + description + "', "
                + "taskId=-1, status=NEW, epicId=-1}";
        assertEquals(expectedString, subtask.toString(), "`toString()` return wrong string");
    }
}
