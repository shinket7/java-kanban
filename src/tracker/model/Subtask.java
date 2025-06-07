package tracker.model;

public class Subtask extends Task {

    private int epicId;

    public Subtask(String summary, String description) {
        super(summary, description);
        epicId = -1;
        taskType = TaskType.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{"
                + "summary='" + getSummary() + '\''
                + ", description='" + getDescription() + '\''
                + ", taskId=" + getTaskId()
                + ", status=" + getStatus()
                + ", epicId=" + epicId
                + '}';
    }
}
