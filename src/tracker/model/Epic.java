package tracker.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Integer> subtaskIds;
    private LocalDateTime endTime;

    public Epic(String summary, String description) {
        super(summary, description);
        subtaskIds = new ArrayList<>();
        taskType = TaskType.EPIC;
        endTime = null;
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(ArrayList<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "Epic{"
                + "summary='" + getSummary() + '\''
                + ", description='" + getDescription() + '\''
                + ", taskId=" + getTaskId()
                + ", status=" + getStatus()
                + ", subtaskIds=" + subtaskIds
                + '}';
    }
}
