package misc;

import data.process.ProcessCreationInfo;
import error.InvalidCommandException;

import java.util.Scanner;

public class InterfaceManager {

    private Scanner s;
    private ActionController controller;

    public InterfaceManager() {
        s = new Scanner(System.in);
        controller = new ActionController(this);
    }

    public void init() {
        run();
    }

    private void run() {
        showWelcome();
        createConfigurationSettings();
        interactWithUser();
    }

    private void interactWithUser() {
        while (true) {
            showActionsBoard();

            try {
                UserEvent event = getUserEvent();

                if (event == UserEvent.QUIT) {
                    showGoodbye();
                    break;
                } else if (event == UserEvent.UNINITIALIZED_EVENT) {
                    showUninitializedChoice();
                    break;
                }

                controller.handleUserEvent(event);
            } catch (InvalidCommandException ex) {
                print(ex.getMessage());
            }
        }
    }

    private void createConfigurationSettings() {
        print("Howdy! First, i'd like to know what are your desired specs for this run!");
        print("First, tell me, what's the memory size for this execution?");
        print("(please, input a number that's a power of 2, ex.: 1024)");
        int memorySize = s.nextInt();
        print("Great!");
        print("Now, what's the size of each page?");
        int pageSize = s.nextInt();
        print("I see... and now, what's the maximum size a process can be?");
        int maxProcessSize = s.nextInt();

        Configuration configuration = new Configuration(memorySize, pageSize, maxProcessSize);
        controller.storeConfiguration(configuration);

        print("Thank you! Now, enjoy the ride.");

        try {
            Thread.sleep(1500);
        } catch (Exception ex) {
            print(ex.getMessage());
        }
        clean();
    }

    private void showActionsBoard() {
        print("What would you like to do?");
        print("a) See all memory slots");
        print("b) Create a new process");
        print("c) See a page table for a process");
        print("d) Quit");
    }

    private void showWelcome() {
        print("Welcome, my guy. Please type what would you like to do");
    }

    private void showGoodbye() {
        print("Thanks for your time. Bbye");
    }

    private void showUninitializedChoice() {
        print("It seems your choice has not been captured. Please type it again");
    }

    private UserEvent getUserEvent() throws InvalidCommandException {
        char choice = s.next().charAt(0);

        if (choice != 'a' &&
            choice != 'b' &&
            choice != 'c' &&
            choice != 'd') {
            throw new InvalidCommandException();
        }

        return mapChoiceToEvent(choice);
    }

    private UserEvent mapChoiceToEvent(char choice) {
        UserEvent event = UserEvent.UNINITIALIZED_EVENT;
        switch (choice) {
            case 'a':
                event = UserEvent.VIEW_MEMORY;
                break;
            case 'b':
                event = UserEvent.CREATE_PROCESS;
                break;
            case 'c':
                event = UserEvent.VIEW_PROCESS_MEMORY;
                break;
            case 'd':
                event = UserEvent.QUIT;
                break;
        }
        return event;
    }

    public ProcessCreationInfo getInfoForProcessCreation() {
        int processId = -1;
        int processSize;

        print("Ok! Let's create a new process shall we?");
        print("Tell me, would you like to create this process id, or prefer to leave it to the system auto-generate it?");
        print("(y/n)");
        char choice = s.next().charAt(0);

        if (choice == 'y') {
            print("Ok! Tell me then, what's the id for it?");
            processId = s.nextInt();
        } else {
            print("No problem! The system will find an id for it.");
        }

        print("Ok... and, what's the size of this process?");
        processSize = s.nextInt();

        return new ProcessCreationInfo(processId, processSize);
    }

    public Integer getInfoForViewingProcessMemory() {
        print("Alright, let's take a look at some process's memory");
        print("What's the id of the process?");
        return s.nextInt();
    }

    public void print(String message) {
        System.out.println(message);
    }

    private void clean() {
        print("\n\n\n\n\n\n\n\n\n\n");
        print("\n\n\n\n\n\n\n\n\n\n");
        print("\n\n\n\n\n\n\n\n\n\n");
        print("\n\n\n\n\n\n\n\n\n\n");
    }
}
