package tracker.controllers;

import tracker.exceptions.ManagerSaveException;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.TaskStatus;
import tracker.model.TaskType;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final DateTimeFormatter DATE_TIME_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final File autosaveFile;

    public FileBackedTaskManager(HistoryManager historyManager, File autosaveFile) {
        super(historyManager);
        this.autosaveFile = autosaveFile;
    }

    private static String toString(Task task) {
        final TaskType taskType = task.getTaskType();
        final String epicIdText;
        if (taskType == TaskType.SUBTASK) {
            final Subtask subtask = (Subtask) task;
            epicIdText = String.valueOf(subtask.getEpicId());
        } else {
            epicIdText = "";
        }
        final LocalDateTime startTime = task.getStartTime();
        final Duration duration = task.getDuration();
        final String startTimeText;
        final String durationText;
        if (startTime == null || duration == null) {
            startTimeText = "";
            durationText = "";
        } else {
            startTimeText = startTime.format(DATE_TIME_PATTERN);
            durationText = String.valueOf(duration.toMinutes());
        }
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s", task.getTaskId(), task.getTaskType(), task.getSummary(),
                task.getStatus(), task.getDescription(), startTimeText, durationText, epicIdText);
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",", 8);
        final int taskId = Integer.parseInt(parts[0]);
        final String taskTypeText = parts[1];
        final String summary = parts[2];
        final String taskStatusText = parts[3];
        final String description = parts[4];
        final String startTimeText = parts[5];
        final String durationText = parts[6];

        final TaskStatus taskStatus;
        if (taskStatusText.equals("IN_PROGRESS")) {
            taskStatus = TaskStatus.IN_PROGRESS;
        } else if (taskStatusText.equals("DONE")) {
            taskStatus = TaskStatus.DONE;
        } else {
            taskStatus = TaskStatus.NEW;
        }

        final Task result;
        if (taskTypeText.equals("EPIC")) {
            result = new Epic(summary, description);
        } else if (taskTypeText.equals("SUBTASK")) {
            final int epicId = Integer.parseInt(parts[7]);
            final Subtask subtask = new Subtask(summary, description);
            subtask.setEpicId(epicId);
            result = subtask;
        } else {
            result = new Task(summary, description);
        }
        result.setTaskId(taskId);
        result.setStatus(taskStatus);

        if (!startTimeText.isEmpty() && !durationText.isEmpty()) {
            final LocalDateTime startTime = LocalDateTime.parse(startTimeText, DATE_TIME_PATTERN);
            final Duration duration = Duration.ofMinutes(Long.parseLong(durationText));
            result.setStartTimeAndDuration(startTime, duration);
        }
        return result;
    }

    private void save() {
        final List<Task> issues = new ArrayList<>();
        issues.addAll(getTasks());
        issues.addAll(getEpics());
        issues.addAll(getSubtasks());
        try (BufferedWriter writer = Files.newBufferedWriter(autosaveFile.toPath(), StandardCharsets.UTF_8)) {
            writer.write("id,type,name,status,description,startTime,duration,epic");
            for (Task issue : issues) {
                writer.write("\n" + toString(issue));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("При сохранении задач в файл произошла ошибка");
        }
    }

    public void loadFromFileToCurrentManager(File file) {
        final String fileContent;
        try {
            fileContent = Files.readString(file.toPath());
        } catch (IOException e) {
            throw new ManagerSaveException("При загрузке задач из файла произошла ошибка");
        }
        String[] fileLines = fileContent.split("\\n", 8);
        final List<Task> issues = new ArrayList<>();
        for (String fileLine : fileLines) {
            if (fileLine.startsWith("id")) {
                continue;
            }
            Task task = fromString(fileLine);
            issues.add(task);
        }
        replaceAllIssues(issues);
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        final FileBackedTaskManager manager = new FileBackedTaskManager(Managers.getDefaultHistory(), file);
        manager.loadFromFileToCurrentManager(file);
        return manager;
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public int addTask(Task task) {
        final int taskId = super.addTask(task);
        save();
        return taskId;
    }

    @Override
    public int addEpic(Epic epic) {
        final int epicId = super.addEpic(epic);
        save();
        return epicId;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        final int subtaskId = super.addSubtask(subtask);
        save();
        return subtaskId;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void replaceAllIssues(List<Task> issues) {
        super.replaceAllIssues(issues);
        save();
    }
}
