package tracker.controllers;

import tracker.model.Task;

public class TaskNode {

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