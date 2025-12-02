import java.io.*;
import java.util.*;

public class MemoryAllocationLab {

    static class MemoryBlock {
        int start;
        int size;
        String processName;  // null if free

        public MemoryBlock(int start, int size, String processName) {
            this.start = start;
            this.size = size;
            this.processName = processName;
        }

        public boolean isFree() {
            return processName == null;
        }

        public int getEnd() {
            return start + size - 1;
        }
    }

    static int totalMemory;
    static ArrayList<MemoryBlock> memory = new ArrayList<>();
    static int successfulAllocations = 0;
    static int failedAllocations = 0;

    /**
     * TODO 1, 2: Process memory requests from file
     * <p>
     * This method reads the input file and processes each REQUEST and RELEASE.
     * <p>
     * TODO 1: Read and parse the file
     *   - Open the file using BufferedReader
     *   - Read the first line to get total memory size
     *   - Initialize the memory list with one large free block
     *   - Read each subsequent line and parse it
     *   - Call appropriate method based on REQUEST or RELEASE
     * <p>
     * TODO 2: Implement allocation and deallocation
     *   - For REQUEST: implement First-Fit algorithm
     *     * Search memory list for first free block >= requested size
     *     * If found: split the block if necessary and mark as allocated
     *     * If not found: increment failedAllocations
     *   - For RELEASE: find the process's block and mark it as free
     *   - Optionally: merge adjacent free blocks (bonus)
     */
    // ==========================
    // TODO 1: Read + Process File
    // ==========================
    public static void processRequests(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            totalMemory = Integer.parseInt(br.readLine().trim());
            System.out.println("========================================");
            System.out.println("Memory Allocation Simulator (First-Fit)");
            System.out.println("========================================\n");

            System.out.println("Reading from: " + filename);
            System.out.println("Total Memory: " + totalMemory + " KB");
            System.out.println("----------------------------------------\n");
            System.out.println("Processing requests...\n");

            // Initialize memory with single free block
            memory.clear();
            memory.add(new MemoryBlock(0, totalMemory, null));

            // Read, parse, process each request line
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(" ");

                if (parts[0].equals("REQUEST")) {
                    String process = parts[1];
                    int size = Integer.parseInt(parts[2]);
                    allocate(process, size);
                }
                else if (parts[0].equals("RELEASE")) {
                    String process = parts[1];
                    deallocate(process);
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    // ================================
    // TODO 2A: First-Fit Allocation
    // ================================
    private static void allocate(String process, int size) {

        for (int i = 0; i < memory.size(); i++) {
            MemoryBlock block = memory.get(i);

            if (block.isFree() && block.size >= size) {

                // Success - allocate block
                int remaining = block.size - size;
                block.processName = process;
                block.size = size;

                // If leftover space, create new free block
                if (remaining > 0) {
                    MemoryBlock leftover = new MemoryBlock(block.start + size, remaining, null);
                    memory.add(i + 1, leftover);
                }

                successfulAllocations++;
                System.out.println("REQUEST " + process + " " + size + " KB → SUCCESS");
                return;
            }
        }

        // No suitable block found
        failedAllocations++;
        System.out.println("REQUEST " + process + " " + size + " KB → FAILED (Insufficient Memory)");
    }

    // ================================
    // Deallocate Process Memory
    // ================================
    private static void deallocate(String process) {

        for (MemoryBlock block : memory) {
            if (!block.isFree() && block.processName.equals(process)) {
                block.processName = null;
                System.out.println("RELEASE " + process + " → SUCCESS");
                mergeAdjacentBlocks();
                return;
            }
        }

        System.out.println("RELEASE " + process + " → FAILED (Process Not Found)");
    }

    // ====================================
    // Optional: Merge Adjacent Free Blocks
    // ====================================
    private static void mergeAdjacentBlocks() {
        for (int i = 0; i < memory.size() - 1; i++) {
            MemoryBlock a = memory.get(i);
            MemoryBlock b = memory.get(i + 1);

            if (a.isFree() && b.isFree()) {
                a.size += b.size;
                memory.remove(i + 1);
                i--;
            }
        }
    }

    // ==========================
    // Display Final Statistics
    // ==========================
    public static void displayStatistics() {
        System.out.println("\n========================================");
        System.out.println("Final Memory State");
        System.out.println("========================================");

        for (int i = 0; i < memory.size(); i++) {
            MemoryBlock b = memory.get(i);
            int end = b.start + b.size - 1;

            if (b.isFree()) {
                System.out.printf("Block %d: [%d-%d]  FREE (%d KB)\n",
                        i + 1, b.start, end, b.size);
            } else {
                System.out.printf("Block %d: [%d-%d]  %s (%d KB) - ALLOCATED\n",
                        i + 1, b.start, end, b.processName, b.size);
            }
        }

        // Stats
        int totalMem = 0, freeMem = 0, processes = 0, largestFree = 0;

        for (MemoryBlock b : memory) {
            totalMem += b.size;
            if (b.isFree()) {
                freeMem += b.size;
                if (b.size > largestFree) largestFree = b.size;
            } else {
                processes++;
            }
        }

        double extFrag = 0;
        if (freeMem > 0) {
            extFrag = (double)(freeMem - largestFree) / totalMem * 100.0;
        }

        System.out.println("\n========================================");
        System.out.println("Memory Statistics");
        System.out.println("========================================");
        System.out.println("Total Memory:           " + totalMem + " KB");
        System.out.println("Allocated Memory:       " + (totalMem - freeMem) + " KB");
        System.out.println("Free Memory:            " + freeMem + " KB");
        System.out.println("Number of Processes:    " + processes);
        System.out.println("Number of Free Blocks:  " + (memory.size() - processes));
        System.out.println("Largest Free Block:     " + largestFree + " KB");
        System.out.printf("External Fragmentation: %.2f%%\n", extFrag);
        System.out.println("\nSuccessful Allocations: " + successfulAllocations);
        System.out.println("Failed Allocations:     " + failedAllocations);
        System.out.println("========================================");
    }

    // ==========================
    // Main Driver
    // ==========================
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java MemoryAllocationLab <filename>");
            return;
        }

        processRequests(args[0]);
        displayStatistics();
    }
}