package manager;

import data.memory.Frame;
import data.memory.Page;
import data.process.Process;
import error.InsufficientMemoryException;
import error.ProcessTooLargeException;
import error.UnavailableProcessSpaceException;
import error.UnsupportedIdException;
import misc.ActionController;
import misc.Configuration;
import data.process.ProcessAllocationInfo;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class MemoryManager implements IMemoryManager {

    private ActionController controller;
    private Configuration configuration;
    private StringBuilder builder;
    private int lastProcessId;
    // TODO("check if i can remove the field below heheheh")
    private Frame[] frames;
    private byte[] rawMemory;
    private Process[] runningProcesses;
    private LinkedList<Frame> availableFrames;
    private Map<Page, Frame> pageTable;

    public MemoryManager(ActionController controller) {
        this.controller = controller;
        availableFrames = new LinkedList<>();
        builder = new StringBuilder();
        pageTable = new LinkedHashMap<>();
    }

    @Override
    public void showMemory() {
        int lastPageNumber = 0;
        for (int i = 0; i < rawMemory.length; i++) {
            byte currentMemorySlot = rawMemory[i];
            int pageNumber = calculateFrameIndexForMemoryIndex(i);

            if (lastPageNumber != pageNumber) {
                lastPageNumber = pageNumber;
                builder.append("\n");
            }

            builder.append("\t\t\t----\n");
            builder.append("Frame ").append(pageNumber).append(": \t|");
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

        allocateProcessIntoMemory(pid, processSize);
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
        if (availableFrames.size() < numberOfPagesForProcess) {
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
    private void allocateProcessIntoMemory(int processId, int processSize) throws Exception {
        ProcessAllocationInfo allocationInfo = calculateProcessAllocationInfo(processSize);

        int numberOfPages = allocationInfo.getNumberOfPagesForProcess();
        int unfilledPageSlotsSize = allocationInfo.getUnfilledPageSlotsSize();

        Process newProcess = new Process(processId, processSize);
        int slotsToFill = configuration.pageSize();
        Page[] allocatedPagesForProcess = new Page[numberOfPages];

        for (int i = 0; i < numberOfPages; i++) {
            Frame currentFrame = availableFrames.getFirst();

            if (i == numberOfPages - 1) {
                slotsToFill = unfilledPageSlotsSize;
            }

            currentFrame.occupyMemorySlotsWithProcessId(slotsToFill, processId);
            // todo: remove process id from page
            Page pageForProcess = new Page(configuration.pageSize());
            Frame correspondingFrame = availableFrames.removeFirst();
            pageTable.put(pageForProcess, correspondingFrame);

            allocatedPagesForProcess[i] = pageForProcess;
        }

        newProcess.setPageTable(allocatedPagesForProcess);
        int newProcessIndex = calculateAvailableProcessIndex();
        runningProcesses[newProcessIndex] = newProcess;
        occupyMemorySlots(processId, slotsToFill);
    }

    /**
     *
     * This method forcefully writes the processId onto each occupied
     * raw memory slot. This must be done since Java does not support
     * pass by reference.
     * @param processId the id that will be written onto these slots
     *
     */
    private void occupyMemorySlots(int processId, int slotsToFillOnLastPage) throws UnsupportedIdException {
        Process process = getProcessById(processId);
        Page[] pagesForProcess = process.getPageTable();

        for (int i = 0; i < pagesForProcess.length; i++) {
            int startingIndex = getBaseStartingIndexForProcess(process, i);

            int displacement;
            if (i == pagesForProcess.length - 1) {
                displacement = slotsToFillOnLastPage;
            } else {
                displacement = configuration.pageSize();
            }

            for (int d = 0; d < displacement; d++) {
                rawMemory[startingIndex + d] = (byte) processId;
            }
        }
    }

    private int getBaseStartingIndexForProcess(Process process, int currentPageIndex) {
        int startingPosition = 0;

        for (Process currentProcess : runningProcesses) {
            if (currentProcess.getId() != process.getId()) {
                int numberOfPages = calculateProcessAllocationInfo(currentProcess.getSize()).getNumberOfPagesForProcess();
                startingPosition += numberOfPages * configuration.pageSize();
            } else {
                return startingPosition + (currentPageIndex * configuration.pageSize());
            }
        }

        return startingPosition;
    }

    @Override
    public void showPageTableForProcess(int processId) throws UnsupportedIdException {
        Process processToBeShown = getProcessById(processId);
        displayPageTableForProcess(processToBeShown.getPageTable());
    }

    private Process getProcessById(int processId) throws UnsupportedIdException {
        for (Process runningProcess : runningProcesses) {
            if (runningProcess.getId() == processId) {
                return runningProcess;
            }
        }
        throw new UnsupportedIdException();
    }

    /**
     *
     * This method displays the array of pages that were
     * allocated for this process.
     *
     * @param processPages the array of pages to be shown
     *
     */
    private void displayPageTableForProcess(Page[] processPages) {
        int lastPageNumber = 0;
        for (int i = 0; i < processPages.length; i++) {
            if (lastPageNumber != i) {
                lastPageNumber = i;
                builder.append("\n");
            }

            Page currentPage = processPages[i];
            Frame correspondingFrame = pageTable.get(currentPage);
            byte[] memorySlots = correspondingFrame.getSlots();
            int frameIndex = getMemoryFrameIndexForFrame(correspondingFrame);

            for (int memSlot = 0; memSlot < memorySlots.length; memSlot++) {

                builder.append("\t\t\t\t\t-----\n");
                builder.append("Page:").append(i).append(", Frame:").append(frameIndex).append(": \t| ");
                builder.append(memorySlots[memSlot]).append(" |\n");
            }
        }

        controller.print(builder.toString());
        clearStringBuilder();
    }

    private int getMemoryFrameIndexForFrame(Frame frame) {
        for (int i = 0; i < frames.length; i++) {
            if (frames[i].equals(frame)) {
                return i;
            }
        }
        return 0;
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

        int numberOfFrames = configuration.memorySize() / configuration.pageSize();
        initializeFrames(numberOfFrames);
    }

    /**
     *
     * This method creates the logical memory array
     * and by doing so makes all frames have -1 as value
     *
     * @param numberOfFrames the number of pages that exist during this runtime
     *
     */
    private void initializeFrames(int numberOfFrames) {
        frames = new Frame[numberOfFrames];
        for (int i = 0; i < numberOfFrames; i++) {
            frames[i] = new Frame(configuration.pageSize());
            availableFrames.addLast(frames[i]);
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

    private int calculateFrameIndexForMemoryIndex(int index) {
        return index / configuration.pageSize();
    }
}
