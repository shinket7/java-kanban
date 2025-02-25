package tracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpicTest {

    private final String summary = "Initial summary for test";
    private final String description = "Initial description for test";
    private Epic epic;

    @BeforeEach
    void BeforeEach() {
        epic = new Epic(summary, description);
    }

    @Test
    void subTaskShouldExtendTask() {
        assertTrue(Task.class.isAssignableFrom(Epic.class), "`Epic` should extend `Task` class");
    }

    @Test
    void shouldReturnEmptyArrayListOfSubtaskIdsForNewEpic() {
        assertEquals(new ArrayList<>(), epic.getSubtaskIds(),
                "`getSubtaskIds()` should return initial empty `ArrayList` of subtask ids for a new epic");
    }

    @Test
    void shouldSetSubtaskIds() {
        ArrayList<Integer> newSubtaskIds = new ArrayList<>(2);
        newSubtaskIds.add(23);
        newSubtaskIds.add(21);
        epic.setSubtaskIds(newSubtaskIds);
        assertEquals(newSubtaskIds, epic.getSubtaskIds(), "`setSubtaskIds()` should set subtask ids");
    }

    @Test
    void shouldReturnCorrectString() {
        String expectedString = "Epic{summary='" + summary + "'" + ", description='" + description + "', "
                + "taskId=-1, status=NEW, subtaskIds=[]}";
        assertEquals(expectedString, epic.toString(), "`toString()` return wrong string");
    }
}