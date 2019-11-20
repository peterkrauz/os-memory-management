package manager;

public interface IMemoryManager {
    void showMemory();
    void createProcess(int processId, int size) throws Exception;
    void showPageTableForProcess(int processId);
}
