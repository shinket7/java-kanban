package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {

    private static int lastTaskId;

    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;

    public TaskManager() {
        lastTaskId = 0;
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
    }

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public ArrayList<Integer> getTaskIds() {
        return new ArrayList<>(tasks.keySet());
    }

    public ArrayList<Integer> getEpicIds() {
        return new ArrayList<>(epics.keySet());
    }

    public ArrayList<Integer> getSubtaskIds() {
        return new ArrayList<>(subtasks.keySet());
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void clearSubtasks() {
        subtasks.clear();
        final ArrayList<Integer> epicIds = getEpicIds();
        for (Integer epicId : epicIds) {
            Epic epic = getEpicById(epicId);
            epic.setStatus(TaskStatus.NEW);
            epic.setSubtaskIds(new ArrayList<>());
        }
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public int addTask(Task task) {
        final int id = ++lastTaskId;
        task.setTaskId(id);
        updateTask(task);
        return id;
    }

    public int addEpic(Epic epic) {
        final int id = ++lastTaskId;
        epic.setTaskId(id);
        updateEpic(epic);
        return id;
    }

    public int addSubtask(Subtask subtask) {
        final int epicId = subtask.getEpicId();
        final Epic epic = getEpicById(epicId);
        if (epic == null) {
            return -1;
        }

        final int id = ++lastTaskId;
        subtask.setTaskId(id);

        updateSubtask(subtask);
        return id;
    }

    public void updateTask(Task task) {
        tasks.put(task.getTaskId(), task);
    }

    public void updateEpic(Epic epic) {
        final int epicId = epic.getTaskId();
        epics.put(epicId, epic);
        final TaskStatus computedEpicStatus = computeEpicStatus(epicId);
        epic.setStatus(computedEpicStatus);
    }

    public void updateSubtask(Subtask subtask) {
        final int epicId = subtask.getEpicId();
        final Epic epic = getEpicById(epicId);
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

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    public void deleteEpicById(int id) {
        final Epic epic = epics.remove(id);
        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
        }
    }

    public void deleteSubtaskById(int id) {
        final Subtask subtask = getSubtaskById(id);
        final int epicId = subtask.getEpicId();
        final Epic epic = getEpicById(epicId);
        subtasks.remove(id);

        if (epic != null) {
            final ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
            subtaskIds.remove(Integer.valueOf(subtask.getTaskId()));

            final TaskStatus computedEpicStatus = computeEpicStatus(epicId);
            epic.setStatus(computedEpicStatus);
            epics.put(epicId, epic);
        }

    }

    public ArrayList<Integer> getEpicSubtaskIdsByEpicId(int epicId) {
        final Epic epic = epics.get(epicId);
        return epic.getSubtaskIds();
    }

    private TaskStatus computeEpicStatus(int epicId) {
        final Epic epic = getEpicById(epicId);
        final ArrayList<Integer> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds.isEmpty()) {
            return TaskStatus.NEW;
        }
        boolean hasNew = false;
        boolean hasInProgress = false;
        boolean hasDone = false;
        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = getSubtaskById(subtaskId);
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
}
