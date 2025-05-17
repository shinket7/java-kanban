package tracker.controllers;

import tracker.model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    class SinglyLinkedTaskList {

        private TaskNode first;
        private TaskNode last;
        private int size;

        public int size() {
            return size;
        }

        public SinglyLinkedTaskList() {
            first = null;
            last = null;
            size = 0;
        }

        public void linkLast(Task task) {
            final TaskNode node = new TaskNode(task);
            if (first == null) {
                first = node;
            } else {
                last.setNext(node);
                node.setPrev(last);
            }
            last = node;
            size++;
        }

        public List<Task> getTasks() {
            TaskNode node = first;
            final List<Task> list = new ArrayList<>(size());
            while (node != null) {
                final Task task = node.getTask();
                list.add(task);
                node = node.getNext();
            }
            return list;
        }
    }

    private final List<Task> history;

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

class TaskNode {

    private final Task task;
    private TaskNode next;
    private TaskNode prev;

    public TaskNode(Task task) {
        this.task = task;
        next = null;
        prev = null;
    }

    public TaskNode getNext() {
        return next;
    }

    public void setNext(TaskNode next) {
        this.next = next;
    }

    public TaskNode getPrev() {
        return prev;
    }

    public void setPrev(TaskNode prev) {
        this.prev = prev;
    }

    public Task getTask() {
        return task;
    }
}