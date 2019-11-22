package manager;

import data.memory.Page;
import data.process.Process;
import error.InsufficientMemoryException;
import error.ProcessTooLargeException;
import error.UnavailableProcessSpaceException;
import error.UnsupportedIdException;
import misc.ActionController;
import misc.Configuration;
import data.process.ProcessAllocationInfo;

import java.util.LinkedList;

public class MemoryManager implements IMemoryManager {

    private ActionController controller;
    private Configuration configuration;
    private StringBuilder builder;

    private byte[] rawMemory;
    private Page[] logicalMemory;
    private LinkedList<Page> availablePages;
    private Process[] runningProcesses;
    private int lastProcessId;

    public MemoryManager(ActionController controller) {
        this.controller = controller;
        availablePages = new LinkedList<>();
        builder = new StringBuilder();
    }

    @Override
    public void showMemory() {
        int lastPageNumber = 0;
        for (int i = 0; i < rawMemory.length; i++) {
            byte currentMemorySlot = rawMemory[i];
            int pageNumber = calculatePageNumberForMemoryIndex(i);

            if (lastPageNumber != pageNumber) {
                lastPageNumber = pageNumber;
                builder.append("\n");
            }

            builder.append("\t\t\t----\n");
            builder.append("Page " + pageNumber +": \t|");
            builder.append(currentMemorySlot);
            builder.append("|\n");

            if (i == configuration.memorySize() - 1) {
                builder.append("\t\t\t----");
            }
        }

        controller.print(builder.toString());
        clearStringBuilder();
    }

    @Override
    public void createProcess(int processId, int processSize) throws Exception {
        checkProcessFitsIntoMemory(processSize);
        checkHasMemoryForAllocatingProcess(processSize);
        int pid = checkProcessId(processId);

        allocateMemoryForProcess(pid, processSize);
    }

    /**
     *
     * This method checks if this process's size exceeds
     * the maximum size a process can be by comparing
     * if the size is lesser than or equal to the
     * current configurations's #maxProcessSize.
     *
     * @param size the size in pages
     *
     * @see Configuration#maxProcessSize
     *
     */
    private void checkProcessFitsIntoMemory(int size) throws ProcessTooLargeException {
        if (size > configuration.maxProcessSize()) {
            throw new ProcessTooLargeException();
        }
    }

    /**
     *
     * This method checks if there is a number of available pages
     * that satisfy amount of pages that will be needed for
     * allocating this process.
     *
     * @param size the size in pages
     *
     */
    private void checkHasMemoryForAllocatingProcess(int size) throws InsufficientMemoryException {
        int numberOfPagesForProcess = calculateProcessAllocationInfo(size).getNumberOfPagesForProcess();
        if (availablePages.size() < numberOfPagesForProcess) {
            throw new InsufficientMemoryException();
        }
    }

    /**
     *
     * This method takes care of incrementing the last process id or
     * checking if the process id given by the user can be used.
     *
     * @param processId the id of the process
     *
     */
    private int checkProcessId(int processId) throws UnsupportedIdException{
        if (processId == -1) {
            return ++lastProcessId;
        } else {
            if (processId < lastProcessId) {
                throw new UnsupportedIdException();
            }
            return processId;
        }
    }

    /**
     *
     * This method writes the processId into each "cell" of a page,
     * making it "own" the memory slot.
     *
     * @param processId the id of the process that is being allocated onto memory
     * @param processSize the size of the process, in bytes
     *
     */
    private void allocateMemoryForProcess(int processId, int processSize) throws UnavailableProcessSpaceException {
        ProcessAllocationInfo allocationInfo = calculateProcessAllocationInfo(processSize);

        int numberOfPages = allocationInfo.getNumberOfPagesForProcess();
        int unfilledPageSlotsSize = allocationInfo.getUnfilledPageSlotsSize();

        int slotsToFillOnLastPage = configuration.pageSize();
        Page[] allocatedPagesForProcess = new Page[numberOfPages];

        for (int i = 0; i < numberOfPages; i++) {
            Page currentPage = availablePages.getFirst();

            if (i == numberOfPages - 1) {
                slotsToFillOnLastPage = unfilledPageSlotsSize;
            } else {
                slotsToFillOnLastPage = configuration.pageSize();
            }

            currentPage.occupyMemorySlotsWithProcessId(slotsToFillOnLastPage, processId);
            allocatedPagesForProcess[i] = availablePages.removeFirst();
        }

        occupyMemorySlots(allocatedPagesForProcess, processId, slotsToFillOnLastPage);
        Process newProcess = new Process(processId, processSize, allocatedPagesForProcess);
        int newProcessIndex = calculateAvailableProcessIndex();
        runningProcesses[newProcessIndex] = newProcess;
    }

    /**
     *
     * This method forcefully writes the processId onto each occupied
     * raw memory slot. This must be done since Java does not support
     * pass by reference.
     *  @param allocatedPages the array of pages that were allocated beforehand
     * @param processId the id that will be written onto these slots
     * @param slotsToFillOnLastPage
     *
     */
    private void occupyMemorySlots(Page[] allocatedPages, int processId, int slotsToFillOnLastPage) {
        for (int i = 0; i < allocatedPages.length; i++) {
            int startingIndex = calculateMemoryIndexForPage(allocatedPages[i]) * configuration.pageSize();

            int displacement;
            if (i == allocatedPages.length - 1) {
                displacement = slotsToFillOnLastPage;
            } else {
                displacement = configuration.pageSize();
            }
            for (int d = 0; d < displacement; d++) {
                rawMemory[startingIndex + d] = (byte) processId;
            }
        }
    }

    @Override
    public void showPageTableForProcess(int processId) {
        for (int i = 0; i < runningProcesses.length; i++) {
            Process currentProcess = runningProcesses[i];
            try {
                if (currentProcess.getId() == processId) {
                    displayPageTableForProcess(currentProcess.getPageTable());
                    return;
                }
            } catch (NullPointerException ex) {
                controller.print("Oops! Apparently, there are no processes with id: " + processId);
            }
        }
    }

    /**
     *
     * This method displays the array of pages that were
     * allocated for this process.
     *
     * @param pageTable the array of pages to be shown
     *
     */
    private void displayPageTableForProcess(Page[] pageTable) {
        for (int i = 0; i < pageTable.length; i++) {
            Page currentPage = pageTable[i];
            byte[] pageSlots = currentPage.getFrame().getSlots();

            for (int slotIndex = 0; slotIndex < pageSlots.length; slotIndex++) {
                int indexToDisplay = slotIndex + (i * configuration.pageSize());

                builder
                        .append("\t\t\t\t\t-----\n")
                        .append("Slot number: " + indexToDisplay + "\t\t| ")
                        .append(currentPage.getFrame().getByteAt(slotIndex))
                        .append(" |\n");
            }
        }
        builder.append("\t\t\t\t\t-----");

        controller.print(builder.toString());
        clearStringBuilder();
    }

    /**
     *
     * This method stores the configurations for this runtime,
     * initializes memory slots and creates the array of processes
     * that can be created this runtime.
     *
     * @param configuration the object containing configuration fields
     *
     */
    public void storeConfiguration(Configuration configuration) {
        this.configuration = configuration;
        initializeMemories();
        runningProcesses = new Process[configuration.memorySize() / configuration.maxProcessSize()];
    }

    /**
     *
     * This method creates the array that emulates the physical memory
     * and initializes the memory pages pages.
     *
     */
    private void initializeMemories() {
        rawMemory = new byte[configuration.memorySize()];
        for (int i = 0; i < rawMemory.length; i++) {
            rawMemory[i] = -1;
        }

        int numberOfPages = configuration.memorySize() / configuration.pageSize();
        byte[][] pageToMemoryPointers = getMemoryMatrix();
        initializePages(numberOfPages, pageToMemoryPointers);
    }

    /**
     *
     * This method creates the logical memory array
     * and by doing so makes all frames have -1 as value
     *
     * @param numberOfPages the number of pages that exist during this runtime
     * @param pageToMemoryPointers
     *
     */
    private void initializePages(int numberOfPages, byte[][] pageToMemoryPointers) {
        logicalMemory = new Page[numberOfPages];
        for (int i = 0; i < logicalMemory.length; i++) {
            logicalMemory[i] = new Page(i, configuration.pageSize(), pageToMemoryPointers[i]);
            availablePages.addLast(logicalMemory[i]);
        }
    }

    /**
     *
     * This method calculates how many pages will be needed
     * for allocating a process based on that process's size
     *
     * @param processSize the size of the process, in bytes
     *
     */
    private ProcessAllocationInfo calculateProcessAllocationInfo(int processSize) {
        int numberOfPagesForProcess;
        int unfilledPageSlotsSize;

        unfilledPageSlotsSize = processSize % configuration.pageSize();

        if (unfilledPageSlotsSize == 0) {
            numberOfPagesForProcess = processSize / configuration.pageSize();
        } else {
            numberOfPagesForProcess = (processSize / configuration.pageSize()) + 1;
        }

        return new ProcessAllocationInfo(numberOfPagesForProcess, unfilledPageSlotsSize);
    }

    /**
     *
     * This method checks for the first available
     * index of the running process array.
     *
     */
    private int calculateAvailableProcessIndex() throws UnavailableProcessSpaceException{
        for (int i = 0; i < runningProcesses.length; i++) {
            if (runningProcesses[i] == null) {
                return i;
            }
        }
        throw new UnavailableProcessSpaceException();
    }

    /**
     *
     * This method clears all buffer from
     * the StringBuilder object used to compose
     * memory displays and etc.
     *
     */
    private void clearStringBuilder() {
        builder.setLength(0);
    }

    private int calculatePageNumberForMemoryIndex(int index) {
        return index / configuration.pageSize();
    }

    private int calculateMemoryIndexForPage(Page page) {
        return page.getId();
    }

    private int calculatePageIndexForMemoryIndex(int index) {
        return index % configuration.pageSize();
    }

    private byte[][] getMemoryMatrix() {
        int pageSize = configuration.pageSize();
        int numberOfPages = configuration.memorySize() / pageSize;
        byte[][] memoryMatrix = new byte[numberOfPages][pageSize];

        for (int i = 0; i < rawMemory.length; i++) {
            int pageIndex = calculatePageNumberForMemoryIndex(i);
            int displacement = calculatePageIndexForMemoryIndex(i);
            memoryMatrix[pageIndex][displacement] = rawMemory[i];
        }

        return memoryMatrix;
    }

    @Deprecated()
    private <T> void composeDisplayFor(T[] arrayToIterate) {
        for (int i = 0; i < arrayToIterate.length; i++) {
            T currentItem = arrayToIterate[i];

            builder.append("----").append("|");

            if (currentItem.getClass().isInstance(Page.class)) {
                builder
                        .append(((Page) currentItem).getProcessId());
            } else {
                builder
                        .append(currentItem);
            }

            builder.append("|");

            if (i == arrayToIterate.length - 1) {
                builder.append("----");
            }
        }
    }
}
