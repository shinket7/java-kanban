package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import tracker.model.Task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

class FileBackedTaskManagerTest extends TaskManagerTest {

    private File autosaveTempFile;
    private List<String> expectedFileLines;

    TaskManager getTaskManager() {
        return new FileBackedTaskManager(new InMemoryHistoryManager(), autosaveTempFile);
    }

    @BeforeEach
    void beforeEach() {
        try {
            autosaveTempFile = File.createTempFile("autosaveTempFile", "csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        expectedFileLines = new ArrayList<>();
        expectedFileLines.add("id,type,name,status,description,startTime,duration,epic");
        super.beforeEach();
    }

    void addAllIssues() {
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        final int epic1Id = taskManager.addEpic(epic1);
        final int epic2Id = taskManager.addEpic(epic2);
        subtask1.setEpicId(epic1Id);
        subtask2.setEpicId(epic2Id);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
    }

    void prepareForHistoryClearTests() {
        addAllIssues();
        taskManager.getTaskById(task1.getTaskId());
        taskManager.getTaskById(task2.getTaskId());
        taskManager.getEpicById(epic1.getTaskId());
        taskManager.getEpicById(epic2.getTaskId());
        taskManager.getSubtaskById(subtask1.getTaskId());
        taskManager.getSubtaskById(subtask2.getTaskId());
    }

    @Test
    void shouldRemoveTasksFromHistoryWhenClear() {
        prepareForHistoryClearTests();
        taskManager.clearTasks();
        final List<Task> expected = prepareHistoryList();
        expected.removeFirst();
        expected.removeFirst();
        assertEquals(expected, taskManager.getHistory(),
                "`clearTasks()` should remove all tasks from history but leave all epics and subtasks intact");
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
        assertEquals(expected, taskManager.getHistory(),
                "`clearEpics()` should remove all epics and their subtasks from history but leave all "
                + "tasks intact");
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

    List<String> readAutosaveFile() {
        List<String> autosaveFileLines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(autosaveTempFile.toPath())) {
            while (reader.ready()) {
                autosaveFileLines.add(reader.readLine());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return autosaveFileLines;
    }

    @Test
    void shouldAddTaskToAutosaveFile() {
        taskManager.addTask(task1);
        final List<String> autosaveFileLines = readAutosaveFile();
        expectedFileLines.add("1,TASK,task1,NEW,desc task1,,,");
        assertEquals(expectedFileLines, autosaveFileLines, "`addTask()` should add task to autosave file");
    }

    @Test
    void shouldAddEpicToAutosaveFile() {
        taskManager.addEpic(epic1);
        final List<String> autosaveFileLines = readAutosaveFile();
        expectedFileLines.add("1,EPIC,epic1,NEW,desc epic1,,,");
        assertEquals(expectedFileLines, autosaveFileLines, "`addEpic()` should add epic to autosave file");
    }

    @Test
    void shouldAddSubtaskToAutosaveFile() {
        final int epic1Id = taskManager.addEpic(epic1);
        subtask1.setEpicId(epic1Id);
        taskManager.addSubtask(subtask1);
        final List<String> autosaveFileLines = readAutosaveFile();
        expectedFileLines.add("1,EPIC,epic1,NEW,desc epic1,,,");
        expectedFileLines.add("2,SUBTASK,subtask1,NEW,desc subtask1,,,1");
        assertEquals(expectedFileLines, autosaveFileLines,
                "`addSubtask()` should add subtask to autosave file");
    }

    @Test
    void shouldUpdateTaskInAutosaveFile() {
        final int task1Id = taskManager.addTask(task1);
        task2.setTaskId(task1Id);
        taskManager.updateTask(task2);
        final List<String> autosaveFileLines = readAutosaveFile();
        expectedFileLines.add("1,TASK,task2,NEW,desc task2,,,");
        assertEquals(expectedFileLines, autosaveFileLines, "`updateTask() should update task in autosave file");
    }

    @Test
    void shouldUpdateEpicInAutosaveFile() {
        final int epic1Id = taskManager.addEpic(epic1);
        epic2.setTaskId(epic1Id);
        taskManager.updateEpic(epic2);
        final List<String> autosaveFileLines = readAutosaveFile();
        expectedFileLines.add("1,EPIC,epic2,NEW,desc epic2,,,");
        assertEquals(expectedFileLines, autosaveFileLines, "`updateEpic() should update epic in autosave file");
    }

    @Test
    void shouldUpdateSubtaskInAutosaveFile() {
        final int epic1Id = taskManager.addEpic(epic1);
        subtask1.setEpicId(epic1Id);
        subtask2.setEpicId(epic1Id);
        final int subtask1Id = taskManager.addSubtask(subtask1);
        subtask2.setTaskId(subtask1Id);
        taskManager.updateSubtask(subtask2);
        final List<String> autosaveFileLines = readAutosaveFile();
        expectedFileLines.add("1,EPIC,epic1,NEW,desc epic1,,,");
        expectedFileLines.add("2,SUBTASK,subtask2,NEW,desc subtask2,,,1");
        assertEquals(expectedFileLines, autosaveFileLines,
                "`updateSubtask() should update subtask in autosave file");
    }

    List<String> prepareAllIssuesLines() {
        final List<String> list = new ArrayList<>(6);
        list.add("1,TASK,task1,NEW,desc task1,,,");
        list.add("2,TASK,task2,NEW,desc task2,,,");
        list.add("3,EPIC,epic1,NEW,desc epic1,,,");
        list.add("4,EPIC,epic2,NEW,desc epic2,,,");
        list.add("5,SUBTASK,subtask1,NEW,desc subtask1,,,3");
        list.add("6,SUBTASK,subtask2,NEW,desc subtask2,,,4");
        return list;
    }

    @Test
    void shouldClearTasksFromAutosaveFile() {
        addAllIssues();
        taskManager.clearTasks();
        final List<String> autosaveFileLines = readAutosaveFile();
        assertNotEquals(expectedFileLines, autosaveFileLines,
                "`clearTasks()` should clear only tasks from autosave file");
        expectedFileLines.addAll(prepareAllIssuesLines());
        expectedFileLines.remove(1);
        expectedFileLines.remove(1);
        assertEquals(expectedFileLines, autosaveFileLines,
                "`clearTasks()` should clear all tasks from autosave file");
    }

    @Test
    void shouldClearEpicsAndSubtasksFromAutosaveFile() {
        addAllIssues();
        taskManager.clearEpics();
        final List<String> autosaveFileLines = readAutosaveFile();
        assertNotEquals(expectedFileLines, autosaveFileLines,
                "`clearEpics()` should clear only epics and subtasks from autosave file");
        expectedFileLines.addAll(prepareAllIssuesLines());
        expectedFileLines.remove(3);
        expectedFileLines.remove(3);
        expectedFileLines.remove(3);
        expectedFileLines.remove(3);
        assertEquals(expectedFileLines, autosaveFileLines,
                "`clearEpics()` should clear all epics and subtasks from autosave file");
    }

    @Test
    void shouldClearSubtasksFromAutosaveFile() {
        addAllIssues();
        taskManager.clearSubtasks();
        final List<String> autosaveFileLines = readAutosaveFile();
        assertNotEquals(expectedFileLines, autosaveFileLines,
                "`clearEpics()` should clear only subtasks from autosave file");
        expectedFileLines.addAll(prepareAllIssuesLines());
        expectedFileLines.remove(5);
        expectedFileLines.remove(5);
        assertEquals(expectedFileLines, autosaveFileLines,
                "`clearEpics()` should clear all subtasks from autosave file");
    }

    @Test
    void shouldDeleteTaskFromAutosaveFile() {
        addAllIssues();
        taskManager.deleteTaskById(task1.getTaskId());
        final List<String> autosaveFileLines = readAutosaveFile();
        expectedFileLines.addAll(prepareAllIssuesLines());
        expectedFileLines.remove(1);
        assertEquals(expectedFileLines, autosaveFileLines,
                "`deleteTaskById()` should delete task from autosave file and only that one task");
    }

    @Test
    void shouldDeleteEpicFromAutosaveFile() {
        addAllIssues();
        taskManager.deleteEpicById(epic1.getTaskId());
        final List<String> autosaveFileLines = readAutosaveFile();
        expectedFileLines.addAll(prepareAllIssuesLines());
        expectedFileLines.remove(3);
        expectedFileLines.remove(4);
        assertEquals(expectedFileLines, autosaveFileLines,
                "`deleteEpicById()` should delete epic and its subtasks from autosave file and only that one "
                + "epic and its subtasks");
    }

    @Test
    void shouldDeleteSubtaskFromAutosaveFile() {
        addAllIssues();
        taskManager.deleteSubtaskById(subtask1.getTaskId());
        final List<String> autosaveFileLines = readAutosaveFile();
        expectedFileLines.addAll(prepareAllIssuesLines());
        expectedFileLines.remove(5);
        assertEquals(expectedFileLines, autosaveFileLines,
                "`deleteSubtaskById()` should delete subtask from autosave file and only that one subtask");
    }

    File createTempFileForLoad() {
        final File tempFile;
        try {
            tempFile = File.createTempFile("tempFile", "csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        addAllIssues();
        expectedFileLines.addAll(prepareAllIssuesLines());
        expectedFileLines.remove(2);
        expectedFileLines.remove(3);
        expectedFileLines.remove(4);
        try (BufferedWriter writer = Files.newBufferedWriter(tempFile.toPath())) {
            for (int i = 0; i < expectedFileLines.size(); i++) {
                if (i != 0) {
                    writer.write("\n");
                }
                writer.write(expectedFileLines.get(i));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tempFile;
    }

    @Test
    void shouldLoadFromFileToCurrentManager() {
        final File tempFile = createTempFileForLoad();
        FileBackedTaskManager fileBackedTaskManager = (FileBackedTaskManager) taskManager;
        fileBackedTaskManager.loadFromFileToCurrentManager(tempFile);
        assertEquals(List.of(task1), taskManager.getTasks(),
                "`loadFromFileToCurrentManager()` should replace tasks by those which are in the file");
        assertEquals(List.of(epic1), taskManager.getEpics(),
                "`loadFromFileToCurrentManager()` should replace epics by those which are in the file");
        assertEquals(List.of(subtask1), taskManager.getSubtasks(),
                "`loadFromFileToCurrentManager()` should replace subtasks by those which are in the file");
    }

    @Test
    void shouldLoadFromFile() {
        final File tempFile = createTempFileForLoad();
        FileBackedTaskManager taskManagerFromFile = FileBackedTaskManager.loadFromFile(tempFile);
        taskManagerFromFile.loadFromFileToCurrentManager(tempFile);
        assertEquals(List.of(task1), taskManagerFromFile.getTasks(),
                "`loadFromFileToCurrentManager()` should replace tasks by those which are in the file");
        assertEquals(List.of(epic1), taskManagerFromFile.getEpics(),
                "`loadFromFileToCurrentManager()` should replace epics by those which are in the file");
        assertEquals(List.of(subtask1), taskManagerFromFile.getSubtasks(),
                "`loadFromFileToCurrentManager()` should replace subtasks by those which are in the file");
    }
}