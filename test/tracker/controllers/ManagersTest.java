package tracker.controllers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManagersTest {

    @Test
    void shouldReturnInMemoryTaskManager() {
        final TaskManager taskManager = Managers.getDefault();
        final TaskManager expected = new InMemoryTaskManager();
        assertEquals(expected.getClass(), taskManager.getClass(),
                "`getDefault()` should return `InMemoryTaskManager` object");
    }
}