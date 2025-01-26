import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        Tester tester = new Tester();
        tester.runTests();
    }
}

class Tester {

    TaskManager manager = new TaskManager();

    void runTests() {
        showLists();

        System.out.println("create first tasks:");
        Task task = new Task("Task summary", "Task description");
        Epic epic = new Epic("Epic summary", "Epic description");
        Subtask subtask = new Subtask("Subtask summary", "Subtask description");

        System.out.println("first task: " + task);
        System.out.println("first epic: " + epic);
        System.out.println("first subtask: " + subtask);
        manager.addTask(task);
        manager.addEpic(epic);
        manager.addSubtask(subtask);
        showLists();

        System.out.println("update subtask:");
        subtask.setEpicId(2);
        manager.updateSubtask(subtask);
        showLists();

        System.out.println("Close subtask:");
        subtask.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask);
        showLists();

        System.out.println("create second subtask in progress:");
        Subtask subtask2 = new Subtask("Subtask 2 summary", "Subtask 2 description");
        subtask2.setEpicId(2);
        manager.addSubtask(subtask2);
        showLists();

        System.out.println("delete second subtask:");
        manager.deleteSubtaskById(4);
        showLists();

        System.out.println("delete first subtask:");
        manager.deleteSubtaskById(3);
        showLists();

        System.out.println("add two subtasks back:");
        manager.addSubtask(subtask);
        manager.addSubtask(subtask2);
        showLists();

        System.out.println("clear subtasks:");
        manager.clearSubtasks();
        showLists();
    }

    void showLists() {
        HashMap<Integer, Task> tasks = manager.getTasks();
        HashMap<Integer, Epic> epics = manager.getEpics();
        HashMap<Integer, Subtask> subtasks = manager.getSubtasks();

        System.out.println("\n_______lists_______");
        System.out.println("tasks: " + tasks);
        System.out.println("epics: " + epics);
        System.out.println("subtasks: " + subtasks);
        System.out.println("\n");
    }
}