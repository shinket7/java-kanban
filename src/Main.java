import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.TaskStatus;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        final Tester tester = new Tester();
        tester.runTests();
    }
}

class Tester {

    final TaskManager manager = Managers.getDefault();

    public void runTests() {
        System.out.println("\nCreate first tasks");
        final Task task = new Task("Task summary", "Task description");
        manager.addTask(task);
        final Epic epic = new Epic("Epic summary", "Epic description");
        manager.addEpic(epic);
        final Subtask subtask = new Subtask("Subtask summary", "Subtask description");
        subtask.setEpicId(epic.getTaskId());
        manager.addSubtask(subtask);
        showLists();

        System.out.println("Create subtask without epic");
        final Subtask subtask2 = new Subtask("Subtask2 summary", "Subtask2 description");
        manager.addSubtask(subtask2);
        showLists();

        System.out.println("Create subtask2");
        subtask2.setEpicId(epic.getTaskId());
        manager.addSubtask(subtask2);
        showLists();

        System.out.println("Complete subtask");
        subtask.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask);
        showLists();

        System.out.println("Remove subtask2");
        manager.deleteSubtaskById(subtask2.getTaskId());
        showLists();

        System.out.println("Remove subtask");
        manager.deleteSubtaskById(subtask.getTaskId());
        showLists();

        System.out.println("Add two subtasks: IN_PROGRESS and DONE");
        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        manager.addSubtask(subtask);
        manager.addSubtask(subtask2);
        showLists();

        System.out.println("Remove epic");
        manager.deleteEpicById(epic.getTaskId());
        showLists();
    }

    private void showLists() {
        final ArrayList<Task> tasks = manager.getTasks();
        final ArrayList<Epic> epics = manager.getEpics();
        final ArrayList<Subtask> subtasks = manager.getSubtasks();

        System.out.println("\n_______lists_______");
        System.out.println("tasks: " + tasks);
        System.out.println("epics: " + epics);
        System.out.println("subtasks: " + subtasks);
        System.out.println("\n");
    }
}