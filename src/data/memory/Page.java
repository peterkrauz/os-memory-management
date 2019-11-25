package data.memory;

public class Page  {

    /**
     * Field containing the id of the process
     * that currently occupies this page's memory
     */
    private int processId;

    public Page(int size) {
        this.processId = -1;
    }

}
