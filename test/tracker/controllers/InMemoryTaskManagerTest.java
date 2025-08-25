package tracker.controllers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import tracker.model.Task;

import java.util.ArrayList;
import java.util.List;

class InMemoryTaskManagerTest extends TaskManagerTest {

    TaskManager getTaskManager() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    void prepareForHistoryClearTests() {
        final int task1Id = taskManager.addTask(task1);
        final int task2Id = taskManager.addTask(task2);
        final int epic1Id = taskManager.addEpic(epic1);
        final int epic2Id = taskManager.addEpic(epic2);
        subtask1.setEpicId(epic1Id);
        subtask2.setEpicId(epic2Id);
        final int subtask1Id = taskManager.addSubtask(subtask1);
        final int subtask2Id = taskManager.addSubtask(subtask2);
        taskManager.getTaskById(task1Id);
        taskManager.getTaskById(task2Id);
        taskManager.getEpicById(epic1Id);
        taskManager.getEpicById(epic2Id);
        taskManager.getSubtaskById(subtask1Id);
        taskManager.getSubtaskById(subtask2Id);
    }

    List<Task> prepareHistoryList() {
        final List<Task> list = new ArrayList<>(4);
        list.add(task1);
        list.add(task2);
        list.add(epic1);
        list.add(epic2);
        list.add(subtask1);
        list.add(subtask2);
        return list;
    }

    @Test
    void shouldRemoveTasksFromHistoryWhenClear() {
        prepareForHistoryClearTests();
        taskManager.clearTasks();
        final List<Task> expected = prepareHistoryList();
        expected.removeFirst();
        expected.removeFirst();
        assertEquals(expected, taskManager.getHistory(), "`clearTasks()` should remove all tasks from history "
                + "but leave all epics and subtasks intact");
    }

    @Test
    void shouldRemoveEpicsAndSubtasksFromHistoryWhenClear() {
        prepareForHistoryClearTests();
        taskManager.clearEpics();
        final List<Task> expected = prepareHistoryList();
        expected.remove(2);
        expected.remove(2);
        expected.remove(2);
        expected.remove(2);
        assertEquals(expected, taskManager.getHistory(), "`clearEpics()` should remove all epics and their "
                + "subtasks from history but leave all tasks intact");
    }

    @Test
    void shouldRemoveSubtasksFromHistoryWhenClear() {
        prepareForHistoryClearTests();
        taskManager.clearSubtasks();
        final List<Task> expected = prepareHistoryList();
        expected.remove(4);
        expected.remove(4);
        assertEquals(expected, taskManager.getHistory(), "`clearSubtasks()` should remove all subtasks "
                + "from history but leave all tasks and epics intact");
    }

    @Test
    void shouldRemoveTaskFromHistoryWhenDeleted() {
        prepareForHistoryClearTests();
        taskManager.deleteTaskById(task1.getTaskId());
        final List<Task> expected = prepareHistoryList();
        expected.removeFirst();
        assertEquals(expected, taskManager.getHistory(), "`deleteTaskById()` should remove only selected task");
    }

    @Test
    void shouldRemoveEpicAndItsSubtasksFromHistoryWhenDeleted() {
        prepareForHistoryClearTests();
        taskManager.deleteEpicById(epic1.getTaskId());
        final List<Task> expected = prepareHistoryList();
        expected.remove(2);
        expected.remove(3);
        assertEquals(expected, taskManager.getHistory(), "`deleteEpicById()` should remove only selected epic "
                + "and its subtasks");
    }

    @Test
    void shouldRemoveSubtaskFromHistoryWhenDeleted() {
        prepareForHistoryClearTests();
        taskManager.deleteSubtaskById(subtask1.getTaskId());
        final List<Task> expected = prepareHistoryList();
        expected.remove(4);
        assertEquals(expected, taskManager.getHistory(), "`deleteSubtaskById()` should remove only selected subtask");
    }
}