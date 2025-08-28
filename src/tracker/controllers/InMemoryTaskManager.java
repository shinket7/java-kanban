package tracker.controllers;

import tracker.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private static int lastTaskId;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;
    private final Set<Task> prioritizedTasks;
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        lastTaskId = 0;
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        prioritizedTasks = new TreeSet<>();
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
        prioritizedTasks.removeIf(issue -> issue.getTaskType() == TaskType.SUBTASK);
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
        prioritizedTasks.removeIf(issue -> issue.getTaskType() == TaskType.SUBTASK);
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
        prioritizedTasks.removeIf(issue -> issue.getTaskType() == TaskType.TASK);
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
        if (taskOverlapsWithExisting(task)) {
            return -1;
        }
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
        if (taskOverlapsWithExisting(subtask)) {
            return -1;
        }
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

    public boolean taskOverlapsWithExisting(Task task) {
        final LocalDateTime endTime = task.getEndTime();
        if (endTime == null) {
            return false;
        }
        final LocalDateTime startTime = task.getStartTime();
        return !prioritizedTasks.stream().allMatch(t -> startTime.isAfter(t.getEndTime())
                || endTime.isBefore(t.getStartTime()));
    }

    @Override
    public void updateTask(Task task) {
        if (taskOverlapsWithExisting(task)) {
            return;
        }
        final int taskId = task.getTaskId();
        tasks.put(taskId, task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        final int epicId = epic.getTaskId();
        epics.put(epicId, epic);
        final TaskStatus computedEpicStatus = computeEpicStatus(epicId);
        epic.setStatus(computedEpicStatus);
        computeAndChangeEpicStartTimeAndDuration(epicId);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (taskOverlapsWithExisting(subtask)) {
            return;
        }
        final int epicId = subtask.getEpicId();
        final Epic epic = epics.get(epicId);
        final int subtaskId = subtask.getTaskId();
        subtasks.put(subtaskId, subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        if (epic == null) {
            return;
        }
        final ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }

        final TaskStatus computedEpicStatus = computeEpicStatus(epicId);
        epic.setStatus(computedEpicStatus);
        computeAndChangeEpicStartTimeAndDuration(epicId);
        epics.put(epicId, epic);
    }

    @Override
    public void deleteTaskById(int id) {
        final Task task = tasks.remove(id);
        prioritizedTasks.remove(task);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        final Epic epic = epics.remove(id);
        historyManager.remove(id);
        for (Integer subtaskId : epic.getSubtaskIds()) {
            final Subtask subtask = subtasks.remove(subtaskId);
            prioritizedTasks.remove(subtask);
            historyManager.remove(subtaskId);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        final Subtask subtask = subtasks.get(id);
        final int epicId = subtask.getEpicId();
        final Epic epic = epics.get(epicId);
        subtasks.remove(id);
        prioritizedTasks.remove(subtask);
        historyManager.remove(id);

        if (epic != null) {
            final ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
            subtaskIds.remove(Integer.valueOf(subtask.getTaskId()));

            final TaskStatus computedEpicStatus = computeEpicStatus(epicId);
            epic.setStatus(computedEpicStatus);
            computeAndChangeEpicStartTimeAndDuration(epicId);
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

    private void computeAndChangeEpicStartTimeAndDuration(int epicId) {
        final Epic epic = epics.get(epicId);
        final ArrayList<Integer> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds.isEmpty()) {
            epic.setStartTimeAndDuration(null, null);
            epic.setEndTime(null);
            return;
        }
        LocalDateTime firstTimeStart = null;
        LocalDateTime lastTimeStart = null;
        LocalDateTime lastTimeEnd = null;
        Duration totalDuration = Duration.ZERO;
        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            final LocalDateTime subtaskTimeStart = subtask.getStartTime();
            if (subtaskTimeStart == null) {
                continue;
            }
            if (firstTimeStart == null || firstTimeStart.isAfter(subtaskTimeStart)) {
                firstTimeStart = subtaskTimeStart;
            }
            if (lastTimeStart == null || lastTimeStart.isBefore(subtaskTimeStart)) {
                lastTimeStart = subtaskTimeStart;
                final Duration duration = Duration.between(firstTimeStart, lastTimeStart.plus(subtask.getDuration()));
                lastTimeEnd = lastTimeStart.plus(duration);
            }
            totalDuration = totalDuration.plus(subtask.getDuration());
        }
        if (firstTimeStart == null) {
            return;
        }
        epic.setStartTimeAndDuration(firstTimeStart, totalDuration);
        epic.setEndTime(lastTimeEnd);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public void replaceAllIssues(List<Task> issues) {
        clearTasks();
        clearEpics();
        prioritizedTasks.clear();
        lastTaskId = 0;
        for (Task issue : issues) {
            int issueId = issue.getTaskId();
            if (issueId > lastTaskId) {
                lastTaskId = issueId;
            }
            TaskType issueType = issue.getTaskType();
            if (issueType == TaskType.EPIC) {
                epics.put(issueId, (Epic) issue);
            } else if (issueType == TaskType.SUBTASK) {
                subtasks.put(issueId, (Subtask) issue);
            } else {
                tasks.put(issueId, issue);
            }
        }
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }
}
