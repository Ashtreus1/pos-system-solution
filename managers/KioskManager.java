package managers;

import filesetup.IFoodItemRepository;
import filesetup.OrderRepository;
import filesetup.QueueingOrderRepository;
import objects.FoodItem;
import objects.Order;
import objects.QueueingOrder;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import poskiosksystem.PosKioskSystem;

public class KioskManager {
    private final IFoodItemRepository itemRepo;
    private int ITEM_INDEX = 0;

    public KioskManager(IFoodItemRepository itemRepo) {
        this.itemRepo = itemRepo;
    }

    public void start() {
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.println("\nWelcome to the Kiosk System!");
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Choose Meal");
            System.out.println("2. View Cart");
            System.out.println("3. Exit");
            System.out.print("\nSelection: ");
            int choice = getIntInput(scan);

            switch (choice) {
                case 1:
                    chooseCategory();
                    break;
                case 2:
                    viewCart();
                    break;
                case 3:
                    exitSystem();
                    return;
                default:
                    System.out.println("Invalid selection. Please choose a number between 1 and 3.");
                    break;
            }
        }
    }

    private void chooseCategory() {
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Category Menu ---");
            System.out.println("1. Dessert");
            System.out.println("2. Chicken");
            System.out.println("3. Drinks");
            System.out.println("4. Return to Main Menu");
            System.out.print("\nSelection: ");
            int choice = getIntInput(scan);

            if (choice == 4) return;
            if (choice < 1 || choice > 3) {
                System.out.println("Invalid selection. Please choose a number between 1 and 4.");
                continue;
            }
            chooseItem(choice);
        }
    }

    private void chooseItem(int categoryId) {
        Scanner scan = new Scanner(System.in);
        List<FoodItem> items = itemRepo.getFoodItemsByCategory(categoryId);
        displayItems(items);

        System.out.println("\nIf you want to go back type 0.");
        System.out.print("Selection: ");
        int itemId = getIntInput(scan);
        if (itemId == 0) return;

        Optional<FoodItem> itemOpt = items.stream().filter(item -> item.getId() == itemId).findFirst();
        if (!itemOpt.isPresent()) {
            System.err.println("Invalid item ID. Please select from the given list.");
            return;
        }

        System.out.print("Enter the quantity: ");
        int qty = getIntInput(scan);

        if (qty < 1) {
            System.out.println("Quantity must be 1 or more. Please enter a valid quantity.");
            return;
        }

        ITEM_INDEX++;
        Order newOrder = new Order(ITEM_INDEX, itemOpt.get().getName(), qty, itemOpt.get().getPrice());
        OrderRepository.getInstance().addOrder(newOrder);
        System.out.println("Added to cart");
    }

    private void displayItems(List<FoodItem> items) {
        System.out.println("\n----- Menu -----\nID   Name   Price");
        for (FoodItem item : items) {
            System.out.println(item.getId() + "   " + item.getName() + "   $" + item.getPrice());
        }
    }

    private void viewCart() {
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.println("\n----- Cart Menu -----");
            List<Order> orders = OrderRepository.getInstance().getAllOrders();
            if (orders.isEmpty()) {
                System.out.println("----There's no order yet----");
                return;
            }

            displayOrders(orders);
            System.out.println("----------------------------");
            System.out.println("\n1. Proceed to Payment\n2. Delete Order\n3. Back to Main Menu");
            System.out.print("\nSelection: ");
            int choice = getIntInput(scan);

            switch (choice) {
                case 1:
                    proceedToPayment(orders);
                    break;
                case 2:
                    deleteOrder(scan);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid selection. Please choose a number between 1 and 3.");
                    break;
            }
        }
    }

    private void displayOrders(List<Order> orders) {
        double total = 0.0;
        System.out.println("\n----- Your Cart -----");
        System.out.println("ID : ORDER NAME : QTY : PRICE");
        for (Order order : orders) {
            System.out.printf("%-3d: %-10s x %-3d: $%-7.2f%n", order.getId(), order.getName(), order.getQty(), order.getPrice());
            total += order.getQty() * order.getPrice();
        }
        System.out.println("----------------------");
        System.out.printf("Total: $%.2f%n", total);
    }

    private void proceedToPayment(List<Order> orders) {
        int queueingNo = generateQueueingNo();
        String orderCode = generateRandomCode();
        Path fileRoot = Paths.get("Storage\\KioskReceipts");
        Path filePath = Paths.get(fileRoot.toAbsolutePath() + "\\" + queueingNo + ".txt");

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE)) {
            writer.write("-----------------------------\n");
            writer.write("Order No: " + queueingNo + "\n");
            writer.write("Order Code: " + orderCode + "\n");
            writer.write("-----------------------------\n");

            double total = 0.0;
            for (Order order : orders) {
                writer.write(String.format("%-15s x %-3d: $%-7.2f%n", order.getName(), order.getQty(), order.getPrice()));
                QueueingOrder newOrder = new QueueingOrder(queueingNo, orderCode, order.getName(), order.getQty(), order.getPrice(), false);
                QueueingOrderRepository.getInstance().saveQueueingOrder(newOrder);
                total += order.getQty() * order.getPrice();
            }

            writer.write("-----------------------------\n");
            writer.write(String.format("Total: $%.2f%n", total));
            writer.write("-----------------------------\n");
            System.out.println("Receipt has been printed.");
            writer.flush();

            OrderRepository.getInstance().clearOrder();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    private void deleteOrder(Scanner scan) {
        System.out.print("Select order by Id to delete: ");
        int orderId = getIntInput(scan);
        System.err.println(OrderRepository.getInstance().deleteOrderById(orderId));
    }

    private void exitSystem() {
        System.out.println("Thank you for using the Kiosk System. Goodbye!");
        PosKioskSystem main = new PosKioskSystem();
        main.startSystem();
    }

    private String generateRandomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder(6);
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }

    private static int getIntInput(Scanner scan) {
        while (!scan.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scan.next();
        }
        return scan.nextInt();
    }

    private static int generateQueueingNo() {
        return new Random().nextInt(999);
    }
}
