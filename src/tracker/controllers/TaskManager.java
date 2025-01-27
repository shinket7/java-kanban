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

    // ↓ For tests below ↓

    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    // ↑ For tests above ↑

    public TaskManager() {
        lastTaskId = 0;
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
    }

    public static int getNextTaskId() {
        return ++lastTaskId;
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
    }

    public void clearSubtasks() {
        subtasks.clear();
        ArrayList<Integer> epicIds = getEpicIds();
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

    public void addTask(Task task) {
        updateTask(task);
    }

    public void addEpic(Epic epic) {
        updateEpic(epic);
    }

    public void addSubtask(Subtask subtask) {
        updateSubtask(subtask);
    }

    public void updateTask(Task task) {
        tasks.put(task.getTaskId(), task);
    }

    public void updateEpic(Epic epic) {
        int epicId = epic.getTaskId();
        ArrayList<Integer> subtaskIds = epic.getSubtaskIds();

        TaskStatus computedEpicStatus = computeEpicStatusBySubtaskIds(epicId, subtaskIds);
        epic.setStatus(computedEpicStatus);
        epics.put(epicId, epic);
    }

    public void updateSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        Epic epic = getEpicById(epicId);
        int subtaskId = subtask.getTaskId();
        subtasks.put(subtaskId, subtask);

        if (epic != null) {
            ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
            if (!subtaskIds.contains(subtaskId)) {
                subtaskIds.add(subtaskId);
                epic.setSubtaskIds(subtaskIds);
            }

            TaskStatus computedEpicStatus = computeEpicStatusBySubtaskIds(epicId, subtaskIds);
            epic.setStatus(computedEpicStatus);
            epics.put(epicId, epic);
        }

    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    public void deleteEpicById(int id) {
        epics.remove(id);
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = getSubtaskById(id);
        int epicId = subtask.getEpicId();
        Epic epic = getEpicById(epicId);
        subtasks.remove(id);

        if (epic != null) {
            ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
            subtaskIds.remove(Integer.valueOf(subtask.getTaskId()));
            epic.setSubtaskIds(subtaskIds);

            TaskStatus computedEpicStatus = computeEpicStatusBySubtaskIds(epicId, subtaskIds);
            epic.setStatus(computedEpicStatus);
            epics.put(epicId, epic);
        }

    }

    public ArrayList<Integer> getEpicSubtaskIdsByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        return epic.getSubtaskIds();
    }

    TaskStatus computeEpicStatusBySubtaskIds(int epicId, ArrayList<Integer> subtaskIds) {
        if (epicId == -1 || subtaskIds.isEmpty()) {
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
