package tracker.controllers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManagersTest {

    @Test
    void shouldReturnInMemoryTaskManager() {
        final TaskManager taskManager = Managers.getDefault();
        final TaskManager expected = new InMemoryTaskManager(Managers.getDefaultHistory());
        assertEquals(expected.getClass(), taskManager.getClass(),
                "`getDefault()` should return `InMemoryTaskManager` object");
    }

    @Test
    void shouldReturnInMemoryHistoryManager() {
        final HistoryManager historyManager = Managers.getDefaultHistory();
        final HistoryManager expected = new InMemoryHistoryManager();
        assertEquals(expected.getClass(), historyManager.getClass(),
                "`getDefaultHistory()` should return `InMemoryHistoryManager` object");
    }
}
