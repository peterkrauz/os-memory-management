package data.memory;

public class Page  {


    private int id;
    /**
     * Field containing the frame that was "paired"
     * with this page.
     */
    private Frame frame;

    /**
     * Field containing the id of the process
     * that currently occupies this page's memory
     */
    private int processId;

    public Page(int id, int size) {
        this.id = id;
        this.processId = -1;
        frame = new Frame(size);
    }

    public void occupyMemorySlotsWithProcessId(int slots, int processId) {
        this.processId = processId;
        frame.occupyMemorySlotsWithProcessId(slots, processId);
    }

    public void cleanMemorySlots() {
        this.processId = -1;
        frame.cleanMemorySlots();
    }

    public int getProcessId() {
        return processId;
    }

    public int getId() {
        return id;
    }

    public Frame getFrame() {
        return frame;
    }

}
