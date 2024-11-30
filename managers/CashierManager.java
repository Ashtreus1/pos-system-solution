package managers;

import filesetup.QueueingOrderRepository;
import objects.QueueingOrder;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import poskiosksystem.PosKioskSystem;

public class CashierManager {
    private final Scanner scan = new Scanner(System.in);

    public void start() {
        System.out.println("\n--------POS-MACHINE--------");

        while (true) {
            try {
                System.out.println("\n1. See Orders\n2. Exit");
                System.out.print("Selection: ");
                int choice = scan.nextInt();
                scan.nextLine();

                switch (choice) {
                    case 1:
                        getAllOrderList();
                        break;
                    case 2:
                        exitSystem();
                        break;
                    default:
                        System.out.println("Invalid Selection!");
                        break;
                }
            } catch (InputMismatchException ex) {
                System.err.println("Invalid Input!");
                scan.nextLine();
            }
        }
    }

    public void getAllOrderList() {
        System.out.println("\n-----------------------------");
        System.out.println("QueueingNo | Code");
        System.out.println("-----------------------------");

        Set<String> displayedCodes = new HashSet<>();
        QueueingOrderRepository repo = QueueingOrderRepository.getInstance();

        for (QueueingOrder order : repo.getAllQueueingOrder()) {
            if (displayedCodes.add(order.getCode())) {
                System.out.println(order.getQueueingNo() + " | " + order.getCode());
            }
        }

        if (displayedCodes.isEmpty()) {
            System.out.println("--- No orders yet ---");
        }
        System.out.println("-----------------------------");

        manageOrders();
    }

    private void manageOrders() {
        while (true) {
            System.out.println("1. Checkout\n2. Refresh\n3. Return to main menu");
            try {
                System.out.print("Selection: ");
                int choice = scan.nextInt();
                scan.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("Enter Order Code: ");
                        getOrderByCode(scan.next());
                        return;
                    case 2:
                        getAllOrderList();
                        break;
                    case 3:
                        start();
                        break;
                    default:
                        System.out.println("Invalid input. Please try again.");
                        break;
                }
            } catch (InputMismatchException ex) {
                System.err.println("Invalid Input!");
                scan.nextLine();
            }
        }
    }

    private void displayOrderDetails(List<QueueingOrder> orderList) {
        String queueingNo = String.valueOf(orderList.get(0).getQueueingNo());
        System.out.println("-----------------------------");
        System.out.println("QueueingNo: " + queueingNo);
        System.out.println("Code: " + orderList.get(0).getCode());
        System.out.println("-----------------------------");

        double total = 0.0;
        for (QueueingOrder order : orderList) {
            System.out.printf("%-15s x %-3d: $%-7.2f%n", order.getName(), order.getQty(), order.getPrice());
            total += order.getPrice() * order.getQty();
        }
        System.out.println("-----------------------------");
        System.out.printf("Total: $%.2f%n", total);
        System.out.println("-----------------------------");
    }

    public void orderApproval(List<QueueingOrder> orders) {
        System.out.println("Do you approve this order? (yes)");
        System.out.println("Any other input will cancel the order. To go back, type 'back'.");
        System.out.print("Input: ");
        scan.nextLine();

        String approval = scan.nextLine().trim().toLowerCase();

        switch (approval) {
            case "yes":
                approveOrder(orders);
                break;
            case "back":
                getAllOrderList();
                break;
            default:
                cancelOrder(orders);
                break;
        }
    }

    public void printReceipt(List<QueueingOrder> orders) {
        String code = orders.get(0).getCode();
        Path filePath = Paths.get("Storage\\CashierReceipts\\" + code + ".txt");

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE)) {
            writer.write("-----------------------------\n");
            writer.write("Order No: " + orders.get(0).getQueueingNo() + "\n");
            writer.write("-----------------------------\n");

            double total = 0.0;
            for (QueueingOrder order : orders) {
                writer.write(String.format("%-15s x %-3d: $%-7.2f%n", order.getName(), order.getQty(), order.getPrice()));
                total += order.getQty() * order.getPrice();
            }

            writer.write("-----------------------------\n");
            writer.write(String.format("Total: $%.2f%n", total));
            writer.write("-----------------------------\n");
            writer.write("Thank you for your purchase!\n");
            writer.write("-----------------------------\n");
            System.out.println("Receipt has been printed.");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }

        QueueingOrderRepository.getInstance().deleteQueueingOrder(code);
        deleteKioskReceipt(orders.get(0).getQueueingNo());
        orders.clear();
    }

    private synchronized void deleteKioskReceipt(int queueingNo) {
        Path filePath = Paths.get("Storage\\KioskReceipts\\" + queueingNo + ".txt");
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            System.err.println("Cannot find Kiosk Receipt");
        }
    }

    public void getOrderByCode(String orderCode) {
        List<QueueingOrder> orderList = QueueingOrderRepository.getInstance().getAllQueueingOrderByCode(orderCode);

        if (orderList.isEmpty()) {
            System.out.println("-----------------------------");
            System.out.println("Order with code not found.");
            System.out.println("-----------------------------");
            return;
        }

        displayOrderDetails(orderList);
        orderApproval(orderList);
    }

    private void approveOrder(List<QueueingOrder> orders) {
        System.out.println("Order approved. Processing payment...");
        printReceipt(orders);
        orders.forEach(order -> order.setStatus(true));
    }

    private void cancelOrder(List<QueueingOrder> orders) {
        System.out.println("Order not approved. Cancelling order...");
        QueueingOrderRepository repo = QueueingOrderRepository.getInstance();
        orders.forEach(order -> repo.deleteQueueingOrder(order.getCode()));
    }

    private void exitSystem() {
        System.out.println("Thank you for using the POS System. Goodbye!");
        PosKioskSystem main = new PosKioskSystem();
        main.startSystem();
    }
}
