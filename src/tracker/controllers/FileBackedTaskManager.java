package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.TaskStatus;
import tracker.model.TaskType;

import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileBackedTaskManager extends InMemoryTaskManager {

    public FileBackedTaskManager(HistoryManager historyManager) {
        super(historyManager);
    }

//    public void test() {
//        try (FileWriter writer = new FileWriter("TaskManagerSave.txt", StandardCharsets.UTF_8)) {
//            writer.write("первая строчка\n");
//            writer.write("вторая строчка");
//        } catch (IOException e) {
//            System.out.println("wrong....");
//        }
//    }

    public static String toString(Task task) {
        final TaskType taskType = task.getTaskType();
        final String epicIdText;
        if (taskType == TaskType.SUBTASK) {
            final Subtask subtask = (Subtask) task;
            epicIdText = String.valueOf(subtask.getEpicId());
        } else {
            epicIdText = "";
        }
        return String.format("%d,%s,%s,%s,%s,%s", task.getTaskId(), task.getTaskType(), task.getSummary(),
                task.getStatus(), task.getDescription(), epicIdText);
    }

    public static Task fromString(String value) {
        String[] parts = value.split(",");
        final int taskId = Integer.parseInt(parts[0]);
        final String taskTypeText = parts[1];
        final String summary = parts[2];
        final String taskStatusText = parts[3];
        final String description = parts[4];

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
            final int epicId = Integer.parseInt(parts[5]);
            final Subtask subtask = new Subtask(summary, description);
            subtask.setEpicId(epicId);
            result = subtask;
        } else {
            result = new Task(summary, description);
        }
        result.setTaskId(taskId);
        result.setStatus(taskStatus);
        return result;
    }

    private void save() {
        final List<Task> issues = new ArrayList<>();
        issues.addAll(getTasks());
        issues.addAll(getEpics());
        issues.addAll(getSubtasks());
        try (FileWriter writer = new FileWriter("TaskManagerSave.csv", StandardCharsets.UTF_8)) {
            writer.write("id,type,name,status,description,epic");
            for (Task issue : issues) {
                writer.write("\n" + toString(issue));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("При сохранении задач в файл произошла ошибка");
        }
    }
}
