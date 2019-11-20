package misc;

public class Configuration {
    private final int memorySize;
    private final int pageSize;
    private final int maxProcessSize;

    public Configuration(int memorySize, int pageSize, int maxProcessSize) {
        this.memorySize = memorySize;
        this.pageSize = pageSize;
        this.maxProcessSize = maxProcessSize;
    }

    public int memorySize() {
        return memorySize;
    }

    public int pageSize() {
        return pageSize;
    }

    public int maxProcessSize() {
        return maxProcessSize;
    }
}
