package tracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TaskTest {

    private final String summary = "Initial summary for test";
    private final String description = "Initial description for test";
    private Task task;

    @BeforeEach
    void beforeEach() {
        task = new Task(summary, description);
    }

    @Test
    void shouldReturnSummary() {
        assertEquals(summary, task.getSummary(), "`getSummary()` should return task summary");
    }

    @Test
    void shouldSetSummary() {
        final String newSummary = "New summary";
        task.setSummary(newSummary);
        assertEquals(newSummary, task.getSummary(), "`setSummary()` should set summary");
    }

    @Test
    void shouldReturnDescription() {
        assertEquals(description, task.getDescription(), "`getDescription` should return task description");
    }

    @Test
    void shouldSetDescription() {
        final String newDescription = "New description";
        task.setDescription(newDescription);
        assertEquals(newDescription, task.getDescription(), "`setDescription()` should set description");
    }

    @Test
    void shouldReturnInitialTaskIdForNewTask() {
        assertEquals(-1, task.getTaskId(),
                "`getTaskId()` should return initial task id -1 for a new task");
    }

    @Test
    void shouldSetId() {
        final int newTaskId = 34;
        task.setTaskId(newTaskId);
        assertEquals(newTaskId, task.getTaskId(), "`setTaskId()` should set taskId");
    }

    @Test
    void shouldReturnInitialStatusForNewTask() {
        assertEquals(TaskStatus.NEW, task.getStatus(), "`getStatus()` should return the NEW status for a new task");
    }

    @Test
    void shouldSetStatus() {
        TaskStatus newStatus = TaskStatus.DONE;
        task.setStatus(newStatus);
        assertEquals(newStatus, task.getStatus(), "`setStatus()` should set status");
    }

    @Test
    void twoTasksWithTheSameIdsShouldBeEqual() {
        final Task anotherTask = new Task("Another task", "Description");
        int newId = 35;
        task.setTaskId(newId);
        anotherTask.setTaskId(newId);
        assertEquals(task, anotherTask, "Two tasks with the same id should be equal");
    }

    @Test
    void twoTasksWithTheSameTextFieldsButWithDifferentIdsShouldNotBeEqual() {
        final Task anotherTask = new Task(summary, description);
        anotherTask.setTaskId(33);
        assertNotEquals(task, anotherTask,
                "Two tasks with the same text fields but with different ids should not be equal");
    }

    @Test
    void shouldReturnCorrectString() {
        final String expectedString = "Task{summary='" + summary + "'" + ", description='" + description + "', "
                + "taskId=-1, status=NEW}";
        assertEquals(expectedString, task.toString(), "`toString()` return wrong string");
    }

    @Test
    void twoTasksWithDifferentIdsShouldHaveDifferentHashCodes() {
        final Task anotherTask = new Task(summary, description);
        task.setTaskId(1);
        anotherTask.setTaskId(2);
        assertNotEquals(task.hashCode(), anotherTask.hashCode(),
                "Two tasks with different ids should have different hash codes even if they have the same "
                + "summary and description.");
        anotherTask.setSummary("Different summary");
        anotherTask.setDescription("Different description");
        assertNotEquals(task.hashCode(), anotherTask.hashCode(),
                "Two tasks with different ids should have different hash codes");
    }
}
