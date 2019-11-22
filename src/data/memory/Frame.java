package data.memory;

public class Frame {

    /**
     * A byte array that emulates each memory slot
     * of the physical memory
     */
    byte[] occupiedMemorySlots;

    public Frame(int size) {
        occupiedMemorySlots = new byte[size];
        initializeSlots();
    }

    private void initializeSlots() {
        for (int i = 0; i < occupiedMemorySlots.length; i++) {
            occupiedMemorySlots[i] = -1;
        }
    }

    public void occupyMemorySlotsWithProcessId(int slots, int processId) {
        for (int i = 0; i < slots; i++) {
            occupiedMemorySlots[i] = (byte) processId;
        }
    }

    public void cleanMemorySlots() {
        for (int i = 0; i < occupiedMemorySlots.length; i++) {
            occupiedMemorySlots[i] = -1;
        }
    }

    public byte[] getSlots() {
        return occupiedMemorySlots;
    }

    public byte getByteAt(int slotIndex) {
        return occupiedMemorySlots[slotIndex];
    }
}
