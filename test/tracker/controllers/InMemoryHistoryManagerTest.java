package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Task;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void beforeEach() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void shouldReturnInitialEmptyArrayListForNewHistoryManager() {
        final ArrayList<Task> expectedList = new ArrayList<>(0);
        assertEquals(expectedList, historyManager.getHistory(),
                "`getHistory()` should return empty `ArrayList` for a new history manager");
    }

    @Test
    void shouldAddTasksToHistory() {
        Task task = new Task("task", "desc task");
        task.setTaskId(1);
        historyManager.add(task);
        task = new Task("task", "desc task");
        task.setTaskId(2);
        historyManager.add(task);
        final ArrayList<Task> expectedList = new ArrayList<>(2);
        task = new Task("task", "desc task");
        task.setTaskId(1);
        expectedList.add(task);
        task = new Task("task", "desc task");
        task.setTaskId(2);
        expectedList.add(task);
        assertEquals(expectedList, historyManager.getHistory(), "`add()` should add tasks to history");
    }

    @Test
    void shouldRemoveFirstAddedToHistoryWhenExceed10Size() {
        Task task = new Task("task", "desc task");
        task.setTaskId(11);
        historyManager.add(task);
        for (int i = 1; i <= 10; i++) {
            task = new Task("task", "desc task");
            task.setTaskId(i);
            historyManager.add(task);
        }
        final ArrayList<Task> expectedList = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            task = new Task("task", "desc task");
            task.setTaskId(i);
            expectedList.add(task);
        }
        assertEquals(expectedList, historyManager.getHistory(), "`getHistory()` should return only 10 last added tasks");
    }
}