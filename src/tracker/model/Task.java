package tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task implements Comparable<Task> {
    private String summary;
    private String description;
    private int taskId;
    private TaskStatus status;
    private LocalDateTime startTime;
    private Duration duration;
    protected TaskType taskType;

    public Task(String summary, String description) {
        this.summary = summary;
        this.description = description;
        this.taskId = -1;
        this.status = TaskStatus.NEW;
        startTime = null;
        duration = null;
        taskType = TaskType.TASK;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setStartTimeAndDuration(LocalDateTime startTime, Duration duration) {
        this.startTime = startTime;
        this.duration = duration;
    }

    public LocalDateTime getEndTime() {
        if (startTime != null && duration != null) {
            return startTime.plus(duration);
        }
        return null;
    }

    @Override
    public int compareTo(Task task) {
        return getStartTime().compareTo(task.getStartTime());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        final Task task = (Task) object;
        return taskId == task.taskId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(taskId);
    }

    @Override
    public String toString() {
        return "Task{"
                + "summary='" + summary + '\''
                + ", description='" + description + '\''
                + ", taskId=" + taskId
                + ", status=" + status
                + '}';
    }
}
