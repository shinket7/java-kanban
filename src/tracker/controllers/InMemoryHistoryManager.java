package tracker.controllers;

import tracker.model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final ArrayList<Task> history;

    public InMemoryHistoryManager() {
        history = new ArrayList<>(10);
    }

    @Override
    public void add(Task task) {
        if (task == null) return;
        if (history.size() == 10) history.removeFirst();
        history.add(task);
    }

    @Override
    public void remove(int id) {
        for (Task task : history) {
            if (id == task.getTaskId()) {
                history.remove(task);
                break;
            }
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
