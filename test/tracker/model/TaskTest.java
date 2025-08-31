package tracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


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
        assertEquals(TaskStatus.NEW, task.getStatus(),
                "`getStatus()` should return the NEW status for a new task");
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
    void shouldNotBeEqual() {
        assertFalse(task.equals(null), "Task should not be equal to `null`.");
        assertFalse(task.equals("string"), "Task should not be equal to other class.");
    }

    @Test
    void twoTasksWithDifferentIdsShouldHaveDifferentHashCodes() {
        final Task anotherTask = new Task(summary, description);
        task.setTaskId(1);
        anotherTask.setTaskId(2);
        assertNotEquals(task.hashCode(), anotherTask.hashCode(),
                "Two tasks with different ids should have different hash codes even if they have the same "
                + "summary and description");
        anotherTask.setSummary("Different summary");
        anotherTask.setDescription("Different description");
        assertNotEquals(task.hashCode(), anotherTask.hashCode(),
                "Two tasks with different ids should have different hash codes");
    }

    @Test
    void shouldSetAndReturnStartTimeAndDuration() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(21);
        task.setStartTimeAndDuration(startTime, duration);
        assertEquals(startTime, task.getStartTime(), "Task should return start time.");
        assertEquals(duration, task.getDuration(), "Task should return duration.");

        startTime = LocalDateTime.now().plusMinutes(2);
        duration = duration.plusMinutes(2);
        task.setStartTimeAndDuration(startTime, duration);
        assertEquals(startTime, task.getStartTime(), "`setStartTimeAndDuration()` should change start time '"
                + "and `getStartTime()` should return that new start time.");
        assertEquals(duration, task.getDuration(), "`setStartTimeAndDuration()` should change duration and '"
                + "`getStartTime()` should return that new duration.");
    }

    @Test
    void shouldNotReturnEndTime() {
        assertNull(task.getEndTime(), "`getEndTime()` should return `null` when there is neither start time, "
                + "nor duration.");
        task.setStartTimeAndDuration(null, Duration.ofMinutes(21));
        assertNull(task.getEndTime(), "`getEndTime()` should return `null` when there is no start time.");
        task.setStartTimeAndDuration(LocalDateTime.now(), null);
        assertNull(task.getEndTime(), "`getEndTime()` should return `null` when there is no duration.");
    }

    @Test
    void shouldReturnEndTime() {
        final LocalDateTime startTime = LocalDateTime.now();
        final Duration duration = Duration.ofMinutes(21);
        task.setStartTimeAndDuration(startTime, duration);
        assertEquals(startTime.plus(duration), task.getEndTime(), "`getEndTime()` should return end time which "
                + "should equals start time plus duration.");
    }

    @Test
    void shouldCompareWithTask() {
        final Task task2 = new Task(summary, description);
        final LocalDateTime firstStartTime = LocalDateTime.now();
        final Duration duration = Duration.ofMinutes(21);
        task.setStartTimeAndDuration(firstStartTime, duration);
        task2.setStartTimeAndDuration(firstStartTime, duration);
        assertEquals(0, task.compareTo(task2), "Task should be equal in `compareTo()` with other task "
                + "with same start time.");
        task2.setStartTimeAndDuration(firstStartTime.plusMinutes(22), duration);
        assertTrue(task.compareTo(task2) < 0, "compareTo()` should return less than 0 when other "
                + "task's start time is after of current task's start time.");
    }
}
