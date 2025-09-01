package tracker.controllers;

import tracker.exceptions.NotFoundException;
import tracker.exceptions.OverlapException;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    ArrayList<Task> getTasks();

    ArrayList<Epic> getEpics();

    ArrayList<Subtask> getSubtasks();

    ArrayList<Integer> getTaskIds();

    ArrayList<Integer> getEpicIds();

    ArrayList<Integer> getSubtaskIds();

    void clearTasks();

    void clearEpics();

    void clearSubtasks();

    Task getTaskById(int id) throws NotFoundException;

    Epic getEpicById(int id) throws NotFoundException;

    Subtask getSubtaskById(int id) throws NotFoundException;

    int addTask(Task task) throws OverlapException;

    int addEpic(Epic epic);

    int addSubtask(Subtask subtask) throws OverlapException, NotFoundException;

    void updateTask(Task task) throws OverlapException;

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask) throws OverlapException, NotFoundException;

    void deleteTaskById(int id);

    void deleteEpicById(int id);

    void deleteSubtaskById(int id);

    List<Integer> getEpicSubtaskIdsByEpicId(int epicId) throws NotFoundException;

    List<Subtask> getEpicSubtasks(int epicId) throws NotFoundException;

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
