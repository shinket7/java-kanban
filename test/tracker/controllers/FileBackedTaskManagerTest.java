package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.TaskStatus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

class FileBackedTaskManagerTest {

    private Task task1;
    private Task task2;
    private Subtask subtask1;
    private Subtask subtask2;
    private Epic epic1;
    private Epic epic2;
    private FileBackedTaskManager taskManager;
    File autosaveTempFile;
    List<String> expectedFileLines;

    @BeforeEach
    void beforeEach() {
        task1 = new Task("task1", "desc task1");
        task2 = new Task("task2", "desc task2");
        epic1 = new Epic("epic1", "desc epic1");
        epic2 = new Epic("epic2", "desc epic2");
        subtask1 = new Subtask("subtask1", "desc subtask1");
        subtask2 = new Subtask("subtask2", "desc subtask2");
        try {
            autosaveTempFile = File.createTempFile("autosaveTempFile", "csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        taskManager = new FileBackedTaskManager(new InMemoryHistoryManager(), autosaveTempFile);
        expectedFileLines = new ArrayList<>();
        expectedFileLines.add("id,type,name,status,description,epic");
    }

    @Test
    void shouldReturnEmptyArrayListOfTasksForNewTaskManager() {
        assertEquals(new ArrayList<>(), taskManager.getTasks(),
                "`getTasks()` should return initial empty `ArrayList` of tasks for a new task manager");
    }

    @Test
    void shouldReturnEmptyArrayListOfEpicsForNewTaskManager() {
        assertEquals(new ArrayList<>(), taskManager.getEpics(),
                "`getEpics()` should return initial empty `ArrayList` of epics for a new task manager");
    }

    @Test
    void shouldReturnEmptyArrayListOfSubtasksForNewTaskManager() {
        assertEquals(new ArrayList<>(), taskManager.getSubtasks(),
                "`getSubtasks()` should return initial empty `ArrayList` of subtasks for a new task manager");
    }

    @Test
    void shouldReturnEmptyArrayListOfTaskIdsForNewTaskManager() {
        assertEquals(new ArrayList<>(), taskManager.getTaskIds(),
                "`getTasks()` should return initial empty `ArrayList` of task ids for a new task manager");
    }

    @Test
    void shouldReturnEmptyArrayListOfEpicIdsForNewTaskManager() {
        assertEquals(new ArrayList<>(), taskManager.getEpicIds(),
                "`getEpics()` should return initial empty `ArrayList` of epic ids for a new task manager");
    }

    @Test
    void shouldReturnEmptyArrayListOfSubtaskIdsForNewTaskManager() {
        assertEquals(new ArrayList<>(), taskManager.getSubtaskIds(),
                "`getSubtasks()` should return initial empty `ArrayList` "
                        + "of subtask ids for a new task manager");
    }

    @Test
    void shouldAddTask() {
        final int task1Id = taskManager.addTask(task1);
        final int task2Id = taskManager.addTask(task2);
        final ArrayList<Task> expectedList = new ArrayList<>(2);
        expectedList.add(task1);
        expectedList.add(task2);
        assertEquals(expectedList, taskManager.getTasks(), "`addTask()` should add task");
        assertEquals(1, task1Id, "`addTask()` should return id of the added task");
        assertEquals(2, task2Id, "`addTask()` should return id of the added task");
    }

    @Test
    void shouldAddEpic() {
        final int epic1Id = taskManager.addEpic(epic1);
        final int epic2Id = taskManager.addEpic(epic2);
        final ArrayList<Epic> expectedList = new ArrayList<>(2);
        expectedList.add(epic1);
        expectedList.add(epic2);
        assertEquals(expectedList, taskManager.getEpics(), "`addEpic()` should add epic");
        assertEquals(1, epic1Id, "`addEpic()` should return id of the added epic");
        assertEquals(2, epic2Id, "`addEpic()` should return id of the added epic");
    }

    @Test
    void shouldAddSubtask() {
        final int epicId = taskManager.addEpic(epic1);
        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);

        final int subtask1Id = taskManager.addSubtask(subtask1);
        final int subtask2Id = taskManager.addSubtask(subtask2);
        final ArrayList<Subtask> expectedList = new ArrayList<>(2);
        expectedList.add(subtask1);
        expectedList.add(subtask2);
        assertEquals(expectedList, taskManager.getSubtasks(), "`addSubtask()` should add subtask");
        assertEquals(2, subtask1Id, "`addSubtask()` should return id of the added subtask");
        assertEquals(3, subtask2Id, "`addSubtask()` should return id of the added subtask");
    }

    @Test
    void shouldReturnMinusOneAndShouldNotAddSubtaskWithWrongEpicId() {
        final ArrayList<Subtask> expectedList = new ArrayList<>(0);
        final int subtask1Id = taskManager.addSubtask(subtask1);
        assertEquals(expectedList, taskManager.getSubtasks(),
                "`addSubtask()` shouldn't add subtask with initial value of the epic id field");
        assertEquals(-1, subtask1Id,
                "`addSubtask()` should return -1 with initial value of the epic id field of a subtask");

        final int epicId = taskManager.addEpic(epic1);
        subtask2.setEpicId(epicId + 1500);
        final int subtask2Id = taskManager.addSubtask(subtask2);
        assertEquals(expectedList, taskManager.getSubtasks(),
                "`addSubtask()` shouldn't add subtask with epic id of a nonexistent epic");
        assertEquals(-1, subtask2Id,
                "`addSubtask()` should return -1 with epic id of a nonexistent epic");
    }

    @Test
    void shouldReturnTaskIds() {
        final int task1Id = taskManager.addTask(task1);
        final int task2Id = taskManager.addTask(task2);
        final ArrayList<Integer> expectedList = new ArrayList<>(2);
        expectedList.add(task1Id);
        expectedList.add(task2Id);
        assertEquals(expectedList, taskManager.getTaskIds(),
                "`getTaskIds()` should return `ArrayList` of added tasks");
    }

    @Test
    void shouldReturnEpicIds() {
        final int epic1Id = taskManager.addEpic(epic1);
        final int epic2Id = taskManager.addEpic(epic2);
        final ArrayList<Integer> expectedList = new ArrayList<>(2);
        expectedList.add(epic1Id);
        expectedList.add(epic2Id);
        assertEquals(expectedList, taskManager.getEpicIds(),
                "`getEpicIds()` should return `ArrayList` of added epics");
    }

    @Test
    void shouldReturnSubtaskIds() {
        final int epicId = taskManager.addEpic(epic1);
        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);

        final int subtask1Id = taskManager.addSubtask(subtask1);
        final int subtask2Id = taskManager.addSubtask(subtask2);
        final ArrayList<Integer> expectedList = new ArrayList<>(2);
        expectedList.add(subtask1Id);
        expectedList.add(subtask2Id);
        assertEquals(expectedList, taskManager.getSubtaskIds(),
                "`getSubtaskIds()` should return `ArrayList` of added subtasks");
    }

    @Test
    void shouldUpdateTask() {
        final int taskId = taskManager.addTask(task1);
        task2.setTaskId(taskId);
        taskManager.updateTask(task2);
        Task taskAfterUpdate = taskManager.getTaskById(taskId);
        assertEquals(task2, taskAfterUpdate, "`updateTask()` should update task to the new one");
    }

    @Test
    void shouldUpdateEpic() {
        final int epicId = taskManager.addTask(epic1);
        epic2.setTaskId(epicId);
        taskManager.updateEpic(epic2);
        Epic epicAfterUpdate = taskManager.getEpicById(epicId);
        assertEquals(epic2, epicAfterUpdate, "`updateEpic()` should update epic to the new one");
    }

    @Test
    void shouldUpdateSubtask() {
        final int epicId = taskManager.addEpic(epic1);
        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);

        final int subtaskId = taskManager.addSubtask(subtask1);
        subtask2.setTaskId(subtaskId);
        taskManager.updateSubtask(subtask2);
        Subtask subtaskAfterUpdate = taskManager.getSubtaskById(subtaskId);
        assertEquals(subtask2, subtaskAfterUpdate, "`updateSubtask()` should update subtask to the new one");
    }

    @Test
    void shouldClearTask() {
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.clearTasks();
        final ArrayList<Task> expectedList = new ArrayList<>(0);
        assertEquals(expectedList, taskManager.getTasks(),
                "`clearTasks()` should clear tasks from the task manager memory");
    }

    @Test
    void shouldClearEpic() {
        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        taskManager.clearEpics();
        final ArrayList<Epic> expectedList = new ArrayList<>(0);
        assertEquals(expectedList, taskManager.getEpics(),
                "`clearEpics()` should clear epics from the task manager memory");
    }

    @Test
    void shouldClearSubtask() {
        final int epicId = taskManager.addEpic(epic1);
        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.clearSubtasks();
        final ArrayList<Subtask> expectedList = new ArrayList<>(0);
        assertEquals(expectedList, taskManager.getSubtasks(),
                "`clearSubtasks()` should clear subtasks from the task manager memory");
    }

    @Test
    void shouldDeleteTask() {
        final int taskId = taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.deleteTaskById(taskId);
        final ArrayList<Task> expectedList = new ArrayList<>(1);
        expectedList.add(task2);
        assertEquals(expectedList, taskManager.getTasks(),
                "`deleteTaskById()` should delete task from the task manager memory");
    }

    @Test
    void shouldDeleteEpic() {
        final int epicId = taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        taskManager.deleteEpicById(epicId);
        final ArrayList<Epic> expectedList = new ArrayList<>(1);
        expectedList.add(epic2);
        assertEquals(expectedList, taskManager.getEpics(),
                "`deleteEpicById()` should delete epic from the task manager memory");
    }

    @Test
    void shouldDeleteSubtask() {
        final int epicId = taskManager.addEpic(epic1);
        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);

        final int subtaskId = taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.deleteSubtaskById(subtaskId);
        final ArrayList<Subtask> expectedList = new ArrayList<>(1);
        expectedList.add(subtask2);
        assertEquals(expectedList, taskManager.getSubtasks(),
                "`deleteSubtaskById()` should delete subtask from the task manager memory");
    }

    @Test
    void shouldRemoveSubtasksWithEpicDeletion() {
        final int epic1Id = taskManager.addEpic(epic1);
        final int epic2Id = taskManager.addEpic(epic2);
        subtask1.setEpicId(epic1Id);
        subtask2.setEpicId(epic2Id);

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.deleteEpicById(epic1Id);
        final ArrayList<Subtask> expectedList = new ArrayList<>(1);
        expectedList.add(subtask2);
        assertEquals(expectedList, taskManager.getSubtasks(),
                "`deleteEpicById()` should remove subtasks of the deleted epic from task manager memory");
    }

    @Test
    void shouldReturnSubtaskIdsByEpicId() {
        final int epicId = taskManager.addEpic(epic1);
        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);

        final int subtask1Id = taskManager.addSubtask(subtask1);
        final int subtask2Id = taskManager.addSubtask(subtask2);
        final ArrayList<Integer> expectedList = new ArrayList<>(2);
        expectedList.add(subtask1Id);
        expectedList.add(subtask2Id);
        assertEquals(expectedList, taskManager.getEpicSubtaskIdsByEpicId(epicId),
                "`getEpicSubtaskIdsByEpicId()` should return `ArrayList` of subtask ids of epic "
                        + "which id is given");
    }

    @Test
    void shouldIncrementInnerCommonIssueIdsCounterByOneWithAnyNewIssue() {
        final int epicId = taskManager.addEpic(epic1);
        subtask1.setEpicId(epicId);
        final int task1Id = taskManager.addTask(task1);
        final int subtaskId = taskManager.addSubtask(subtask1);
        final int task2Id = taskManager.addTask(task2);
        final ArrayList<Integer> expectedList = new ArrayList<>(4);
        final ArrayList<Integer> actualList = new ArrayList<>(4);
        expectedList.add(1);
        expectedList.add(2);
        expectedList.add(3);
        expectedList.add(4);
        actualList.add(epicId);
        actualList.add(task1Id);
        actualList.add(subtaskId);
        actualList.add(task2Id);
        assertEquals(expectedList, actualList,
                "`addTask()`, `addEpic()`, `addSubtask()` should increment inner common issue counter by one");
    }

    @Test
    void shouldComputeEpicStatusAccordingToItsSubtaskStatuses() {
        epic1.setStatus(TaskStatus.DONE);
        final int epicId = taskManager.addEpic(epic1);
        assertEquals(TaskStatus.NEW, epic1.getStatus(),
                "Task manager should compute epic status by its subtask statuses and should change "
                        + "epic status according to that");

        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);
        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        final int subtask1Id = taskManager.addSubtask(subtask1);
        final int subtask2Id = taskManager.addSubtask(subtask2);
        final ArrayList<Integer> subtaskIds = new ArrayList<>(2);
        subtaskIds.add(subtask1Id);
        subtaskIds.add(subtask2Id);
        epic1.setSubtaskIds(subtaskIds);
        taskManager.updateEpic(epic1);
        assertEquals(TaskStatus.DONE, epic1.getStatus(),
                "Task manager should set epic status to DONE when all its subtasks are in DONE status");

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateEpic(epic1);
        assertEquals(TaskStatus.IN_PROGRESS, epic1.getStatus(),
                "Task manager should set epic status to IN_PROGRESS when at least of its subtasks "
                        + "not in NEW or DONE status");

        subtask1.setStatus(TaskStatus.NEW);
        taskManager.updateEpic(epic1);
        assertEquals(TaskStatus.IN_PROGRESS, epic1.getStatus(),
                "Task manager should set epic status to IN_PROGRESS when at least of its subtasks "
                        + "not in NEW or DONE status");

        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateEpic(epic1);
        assertEquals(TaskStatus.IN_PROGRESS, epic1.getStatus(),
                "Task manager should set epic status to IN_PROGRESS when at least of its subtasks "
                        + "not in NEW or DONE status");

        subtask2.setStatus(TaskStatus.NEW);
        taskManager.updateEpic(epic1);
        assertEquals(TaskStatus.NEW, epic1.getStatus(), "Task manager should set epic status to NEW when "
                + "all its subtasks are in NEW status");
    }

    @Test
    void shouldReturnEmptyArrayListOfHistoryForNewTaskManager() {
        final ArrayList<Task> expectedList = new ArrayList<>(0);
        assertEquals(expectedList, taskManager.getHistory(),
                "`getHistory()` should return empty `ArrayList` for a new task manager");
    }

    @Test
    void allGetIssuesMethodsShouldAddIssuesToHistory() {
        final int epicId = taskManager.addEpic(epic1);
        subtask1.setEpicId(epicId);
        final int subtaskId = taskManager.addSubtask(subtask1);
        final int taskId = taskManager.addTask(task1);
        taskManager.getTaskById(taskId);
        taskManager.getSubtaskById(subtaskId);
        taskManager.getEpicById(epicId);
        final ArrayList<Task> expectedList = new ArrayList<>(3);
        expectedList.add(task1);
        expectedList.add(subtask1);
        expectedList.add(epic1);
        assertEquals(expectedList, taskManager.getHistory(),
                "All get issue methods should add issues to history which `getHistory()` should return");
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

    List<Task> prepareHistoryList() {
        final List<Task> list = new ArrayList<>(6);
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
        expectedFileLines.add("1,TASK,task1,NEW,desc task1,");
        assertEquals(expectedFileLines, autosaveFileLines, "`addTask()` should add task to autosave file");
    }

    @Test
    void shouldAddEpicToAutosaveFile() {
        taskManager.addEpic(epic1);
        final List<String> autosaveFileLines = readAutosaveFile();
        expectedFileLines.add("1,EPIC,epic1,NEW,desc epic1,");
        assertEquals(expectedFileLines, autosaveFileLines, "`addEpic()` should add epic to autosave file");
    }

    @Test
    void shouldAddSubtaskToAutosaveFile() {
        final int epic1Id = taskManager.addEpic(epic1);
        subtask1.setEpicId(epic1Id);
        taskManager.addSubtask(subtask1);
        final List<String> autosaveFileLines = readAutosaveFile();
        expectedFileLines.add("1,EPIC,epic1,NEW,desc epic1,");
        expectedFileLines.add("2,SUBTASK,subtask1,NEW,desc subtask1,1");
        assertEquals(expectedFileLines, autosaveFileLines,
                "`addSubtask()` should add subtask to autosave file");
    }

    @Test
    void shouldUpdateTaskInAutosaveFile() {
        final int task1Id = taskManager.addTask(task1);
        task2.setTaskId(task1Id);
        taskManager.updateTask(task2);
        final List<String> autosaveFileLines = readAutosaveFile();
        expectedFileLines.add("1,TASK,task2,NEW,desc task2,");
        assertEquals(expectedFileLines, autosaveFileLines, "`updateTask() should update task in autosave file");
    }

    @Test
    void shouldUpdateEpicInAutosaveFile() {
        final int epic1Id = taskManager.addEpic(epic1);
        epic2.setTaskId(epic1Id);
        taskManager.updateEpic(epic2);
        final List<String> autosaveFileLines = readAutosaveFile();
        expectedFileLines.add("1,EPIC,epic2,NEW,desc epic2,");
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
        expectedFileLines.add("1,EPIC,epic1,NEW,desc epic1,");
        expectedFileLines.add("2,SUBTASK,subtask2,NEW,desc subtask2,1");
        assertEquals(expectedFileLines, autosaveFileLines,
                "`updateSubtask() should update subtask in autosave file");
    }

    List<String> prepareAllIssuesLines() {
        final List<String> list = new ArrayList<>(6);
        list.add("1,TASK,task1,NEW,desc task1,");
        list.add("2,TASK,task2,NEW,desc task2,");
        list.add("3,EPIC,epic1,NEW,desc epic1,");
        list.add("4,EPIC,epic2,NEW,desc epic2,");
        list.add("5,SUBTASK,subtask1,NEW,desc subtask1,3");
        list.add("6,SUBTASK,subtask2,NEW,desc subtask2,4");
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

    @Test
    void shouldLoadFromFile() {
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
        taskManager.loadFromFileToCurrentManager(tempFile);
        assertEquals(List.of(task1), taskManager.getTasks(),
                "`loadFromFileToCurrentManager()` should replace tasks by those which are in the file");
        assertEquals(List.of(epic1), taskManager.getEpics(),
                "`loadFromFileToCurrentManager()` should replace epics by those which are in the file");
        assertEquals(List.of(subtask1), taskManager.getSubtasks(),
                "`loadFromFileToCurrentManager()` should replace subtasks by those which are in the file");
    }
}