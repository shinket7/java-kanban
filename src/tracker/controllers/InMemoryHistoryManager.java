package tracker.controllers;

import tracker.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    class SinglyLinkedTaskList {

        private TaskNode first;
        private TaskNode last;
        private int size;

        public SinglyLinkedTaskList() {
            first = null;
            last = null;
            size = 0;
        }

        public int size() {
            return size;
        }

        public void linkLast(TaskNode node) {
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

        public void removeNode(TaskNode node) {
            final TaskNode prev = node.getPrev();
            final TaskNode next = node.getNext();
            if (prev != null) prev.setNext(next);
            if (next != null) next.setPrev(prev);
            size--;
        }
    }

    private final SinglyLinkedTaskList history;
    private final Map<Integer, TaskNode> historyMap;

    public InMemoryHistoryManager() {
        history = new SinglyLinkedTaskList();
        historyMap = new HashMap<>();
    }


    @Override
    public void add(Task task) {
        if (task == null) return;
        final TaskNode node = new TaskNode(task);
        final int taskId = task.getTaskId();
        if (historyMap.containsKey(taskId)) history.removeNode(node);
        historyMap.put(taskId, node);
        history.linkLast(node);
    }

    @Override
    public void remove(int id) {
        final TaskNode node = historyMap.get(id);
        historyMap.remove(id);
        history.removeNode(node);
    }

    @Override
    public List<Task> getHistory() {
        return history.getTasks();
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