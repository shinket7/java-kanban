package tracker.controllers;

import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.TaskType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileBackedTaskManager extends InMemoryTaskManager {

    public FileBackedTaskManager(HistoryManager historyManager) {
        super(historyManager);
    }

//    public void test() {
//        try {
//            Files.createFile(Paths.get("ASD.txt"));
//        } catch (IOException e) {
//            System.out.println("wrong....");
//        }
//    }

    public String toString(Task task) {
        final TaskType taskType = task.getTaskType();
        String epicIdText = "";
        if (taskType == TaskType.SUBTASK) {
                final Subtask subtask = (Subtask) task;
                epicIdText += subtask.getEpicId();
        }
        return String.format("%d,%s,%s,%s,%s,%s", task.getTaskId(), task.getTaskType(), task.getSummary(),
                task.getStatus(), task.getDescription(), epicIdText);
    }
}
