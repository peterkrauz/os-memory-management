package misc;

import data.process.ProcessCreationInfo;
import manager.MemoryManager;

public class ActionController {

    private InterfaceManager interfaceManager;
    private MemoryManager memoryManager;

    public ActionController(InterfaceManager interfaceManager) {
        this.interfaceManager = interfaceManager;
        memoryManager = new MemoryManager(this);
    }

    public void handleUserEvent(UserEvent event) {
        switch (event) {
            case VIEW_MEMORY:
                interfaceManager.print("Viewing physical memory...");
                memoryManager.showMemory();
                break;
            case CREATE_PROCESS:
                ProcessCreationInfo processInfo = interfaceManager.getInfoForProcessCreation();

                interfaceManager.print("Creating process...");
                try {
                    memoryManager.createProcess(processInfo.first(), processInfo.second());
                } catch (Exception ex) {
                    interfaceManager.print(ex.getMessage());
                }

                break;
            case VIEW_PROCESS_MEMORY:
                int processId = interfaceManager.getInfoForViewingProcessMemory();
                memoryManager.showPageTableForProcess(processId);
                break;
        }
    }

    public void storeConfiguration(Configuration configuration) {
        memoryManager.storeConfiguration(configuration);
    }

    public void print(String something) {
        interfaceManager.print(something);
    }
}
