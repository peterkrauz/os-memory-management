package data.process;

import data.memory.Page;

public class Process {

    private int id;
    private Page[] pageTable;
    private int size;

    public Process(int id, int size, Page[] allocatedPages) {
        this.id = id;
        this.size = size;
        pageTable = allocatedPages;
    }

    public int getId() {
        return id;
    }

    public Page[] getPageTable() {
        return pageTable;
    }

    public int getSize() {
        return size;
    }

}
