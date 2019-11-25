package data.process;

import data.memory.Page;

public class Process {

    private int id;
    private int size;
    private Page[] pageTable;

    public Process(int id, int size) {
        this.id = id;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    public Page[] getPageTable() {
        return pageTable;
    }

    public void setPageTable(Page[] pageTable) {
        this.pageTable = pageTable;
    }
}
