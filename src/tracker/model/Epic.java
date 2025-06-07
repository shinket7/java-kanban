package tracker.model;

import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Integer> subtaskIds;

    public Epic(String summary, String description) {
        super(summary, description);
        subtaskIds = new ArrayList<>();
        taskType = TaskType.EPIC;
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(ArrayList<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
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
