package freshlink;

import java.time.LocalDate;
import java.util.Scanner;

/**
 * Main class for FreshLink console application.
 * Provides a text-based menu for interacting with the produce management system.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ProduceDAO dao = new ProduceDAO();

        int choice = -1;

        while (choice != 0) {
            System.out.println("\n========================================");
            System.out.println("              FRESHLINK                 ");
            System.out.println("       SMART PRODUCE MANAGEMENT         ");
            System.out.println("========================================");
            System.out.println("1. Add Produce");
            System.out.println("2. View All Produce");
            System.out.println("3. Search Produce");
            System.out.println("4. Update Quantity");
            System.out.println("5. Delete Produce");
            System.out.println("6. Track Expiry");
            System.out.println("7. Allocate Stock (FIFO)");
            System.out.println("8. View Stock Transactions");
            System.out.println("0. Exit");
            System.out.println("========================================");
            System.out.print("Enter your choice: ");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Invalid input! Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    // 1. Add Produce
                    System.out.print("Enter Produce Name: ");
                    String name = scanner.nextLine().trim();

                    System.out.print("Enter Quantity (kg): ");
                    double qty = Double.parseDouble(scanner.nextLine().trim());

                    System.out.print("Enter Harvest Date (YYYY-MM-DD): ");
                    LocalDate harvest = LocalDate.parse(scanner.nextLine().trim());

                    System.out.print("Enter Expiry Date (YYYY-MM-DD): ");
                    LocalDate expiry = LocalDate.parse(scanner.nextLine().trim());

                    System.out.print("Enter Price per kg: ");
                    double price = Double.parseDouble(scanner.nextLine().trim());

                    ProduceBatch newBatch = new ProduceBatch(name, qty, harvest, expiry, price);
                    dao.addProduce(newBatch);
                    break;

                case 2:
                    // 2. View All Produce
                    dao.viewAllProduce();
                    break;

                case 3:
                    // 3. Search Produce
                    System.out.print("Enter Batch ID to search: ");
                    int searchId = Integer.parseInt(scanner.nextLine().trim());
                    dao.searchProduce(searchId);
                    break;

                case 4:
                    // 4. Update Quantity
                    System.out.print("Enter Batch ID to update: ");
                    int updateId = Integer.parseInt(scanner.nextLine().trim());

                    System.out.print("Enter New Quantity (kg): ");
                    double newQty = Double.parseDouble(scanner.nextLine().trim());

                    dao.updateQuantity(updateId, newQty);
                    break;

                case 5:
                    // 5. Delete Produce
                    System.out.print("Enter Batch ID to delete: ");
                    int deleteId = Integer.parseInt(scanner.nextLine().trim());
                    dao.deleteProduce(deleteId);
                    break;

                case 6:
                    // 6. Track Expiry
                    dao.trackExpiry();
                    break;

                case 7:
                    // 7. Allocate Stock (FIFO)
                    System.out.print("Enter Produce Name to allocate: ");
                    String allocateName = scanner.nextLine().trim();

                    System.out.print("Enter Quantity required (kg): ");
                    double reqQty = Double.parseDouble(scanner.nextLine().trim());

                    dao.allocateStockFIFO(allocateName, reqQty);
                    break;

                case 8:
                    // 8. View Stock Transactions
                    dao.viewTransactions();
                    break;

                case 0:
                    // 0. Exit
                    System.out.println("Thank you for using FreshLink. Goodbye!");
                    break;

                default:
                    System.out.println("Invalid choice! Please choose an option between 0 and 8.");
                    break;
            }
        }

        scanner.close();
    }
}
