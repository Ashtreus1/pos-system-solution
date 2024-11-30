package poskiosksystem;

import filesetup.*;
import managers.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class PosKioskSystem {
    public static void main(String[] args) {
        PosKioskSystem main = new PosKioskSystem();
        main.startSystem();
    }
    
    public void startSystem() {
        try (Scanner scan = new Scanner(System.in)) {
            IFoodItemRepository itemRepo = new FoodItemRepository();
            KioskManager kioskSystem = new KioskManager(itemRepo);
            CashierManager cashierSystem = new CashierManager();
            MonitorManager monitorSystem = new MonitorManager();
            
            while (true) {
                try {
                    System.out.println("\nSelect a system!");
                    System.out.println("1. Kiosk System");
                    System.out.println("2. Cashier System");
                    System.out.println("3. Monitor System");
                    System.out.println("4. Exit");
                    System.out.print("\nSelection: ");
                    int choice = scan.nextInt();
                    scan.nextLine();

                    switch (choice) {
                        case 1:
                            kioskSystem.start();
                            break;
                        case 2:
                            cashierSystem.start();
                            break;
                        case 3:
                            monitorSystem.start();
                            break;
                        case 4:
                            System.out.println("System shutdown!");
                            System.exit(0);
                            break;
                        default:
                            System.out.println("Invalid selection. Please choose a number between 1 and 4.");
                            break;
                    }
                } catch (InputMismatchException ex) {
                    System.err.println("Invalid Input!");
                    scan.nextLine();
                }
            }
        }
    }
}

