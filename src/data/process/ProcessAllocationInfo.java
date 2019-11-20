package data.process;

public class ProcessAllocationInfo {

    private final int numberOfPagesForProcess;
    private final int unfilledPageSlotsSize;

    public ProcessAllocationInfo(int numberOfPagesForProcess, int filledSlotsOnLastPage) {
        this.numberOfPagesForProcess = numberOfPagesForProcess;
        this.unfilledPageSlotsSize = filledSlotsOnLastPage;
    }

    public int getNumberOfPagesForProcess() {
        return numberOfPagesForProcess;
    }

    public int getUnfilledPageSlotsSize() {
        return unfilledPageSlotsSize;
    }
}
