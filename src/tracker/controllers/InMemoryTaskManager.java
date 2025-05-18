package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    private static int lastTaskId;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        lastTaskId = 0;
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        this.historyManager = historyManager;
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Integer> getTaskIds() {
        return new ArrayList<>(tasks.keySet());
    }

    @Override
    public ArrayList<Integer> getEpicIds() {
        return new ArrayList<>(epics.keySet());
    }

    @Override
    public ArrayList<Integer> getSubtaskIds() {
        return new ArrayList<>(subtasks.keySet());
    }

    @Override
    public void clearTasks() {
        final List<Integer> ids = getTaskIds();
        tasks.clear();
        for (int id : ids) {
            historyManager.remove(id);
        }
    }

    @Override
    public void clearEpics() {
        final List<Integer> epicIds = getEpicIds();
        final List<Integer> subtaskIds = getSubtaskIds();
        epics.clear();
        subtasks.clear();
        for (Integer epicId : epicIds) {
            historyManager.remove(epicId);
        }
        for (Integer subtaskId : subtaskIds) {
            historyManager.remove(subtaskId);
        }
    }

    @Override
    public void clearSubtasks() {
        final List<Integer> subtaskIds = getSubtaskIds();
        final List<Integer> epicIds = getEpicIds();
        subtasks.clear();
        for (Integer subtaskId : subtaskIds) {
            historyManager.remove(subtaskId);
        }
        for (Integer epicId : epicIds) {
            Epic epic = epics.get(epicId);
            epic.setStatus(TaskStatus.NEW);
            epic.setSubtaskIds(new ArrayList<>());
        }
    }

    @Override
    public Task getTaskById(int id) {
        final Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        final Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        final Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public int addTask(Task task) {
        final int id = ++lastTaskId;
        task.setTaskId(id);
        updateTask(task);
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        final int id = ++lastTaskId;
        epic.setTaskId(id);
        updateEpic(epic);
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        final int epicId = subtask.getEpicId();
        final Epic epic = epics.get(epicId);
        if (epic == null) {
            return -1;
        }

        final int id = ++lastTaskId;
        subtask.setTaskId(id);

        updateSubtask(subtask);
        return id;
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getTaskId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        final int epicId = epic.getTaskId();
        epics.put(epicId, epic);
        final TaskStatus computedEpicStatus = computeEpicStatus(epicId);
        epic.setStatus(computedEpicStatus);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        final int epicId = subtask.getEpicId();
        final Epic epic = epics.get(epicId);
        final int subtaskId = subtask.getTaskId();
        subtasks.put(subtaskId, subtask);

        if (epic != null) {
            final ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
            if (!subtaskIds.contains(subtaskId)) {
                subtaskIds.add(subtaskId);
            }

            final TaskStatus computedEpicStatus = computeEpicStatus(epicId);
            epic.setStatus(computedEpicStatus);
            epics.put(epicId, epic);
        }
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        final Epic epic = epics.remove(id);
        historyManager.remove(id);
        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        final Subtask subtask = subtasks.get(id);
        final int epicId = subtask.getEpicId();
        final Epic epic = epics.get(epicId);
        subtasks.remove(id);
        historyManager.remove(id);

        if (epic != null) {
            final ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
            subtaskIds.remove(Integer.valueOf(subtask.getTaskId()));

            final TaskStatus computedEpicStatus = computeEpicStatus(epicId);
            epic.setStatus(computedEpicStatus);
            epics.put(epicId, epic);
        }

    }

    @Override
    public ArrayList<Integer> getEpicSubtaskIdsByEpicId(int epicId) {
        final Epic epic = epics.get(epicId);
        return epic.getSubtaskIds();
    }

    private TaskStatus computeEpicStatus(int epicId) {
        final Epic epic = epics.get(epicId);
        final ArrayList<Integer> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds.isEmpty()) {
            return TaskStatus.NEW;
        }
        boolean hasNew = false;
        boolean hasInProgress = false;
        boolean hasDone = false;
        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) continue;

            int subtaskEpicId = subtask.getEpicId();
            if (epicId == subtaskEpicId) {
                TaskStatus subtaskStatus = subtask.getStatus();
                switch (subtaskStatus) {
                    case NEW:
                        hasNew = true;
                        break;
                    case IN_PROGRESS:
                        hasInProgress = true;
                        break;
                    case DONE:
                        hasDone = true;
                        break;
                }
            }
        }
        if (!hasNew && !hasInProgress && hasDone) {
            return TaskStatus.DONE;
        } else if (hasInProgress || hasDone) {
            return TaskStatus.IN_PROGRESS;
        } else {
            return TaskStatus.NEW;
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
