package data.process;

public class ProcessCreationInfo {
    private final int processId;
    private final int processSize;

    public ProcessCreationInfo(int processId, int processSize) {
        this.processId = processId;
        this.processSize = processSize;
    }

    public int first() {
        return processId;
    }

    public int second() {
        return processSize;
    }
}
