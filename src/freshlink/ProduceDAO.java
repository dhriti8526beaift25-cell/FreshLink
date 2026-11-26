package freshlink;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Data Access Object (DAO) for handling all database operations in FreshLink.
 * Uses simple JDBC code with PreparedStatement and ResultSet.
 */
public class ProduceDAO {

    // -------------------------------------------------------------
    // 1. ADD PRODUCE BATCH
    // -------------------------------------------------------------
    public void addProduce(ProduceBatch batch) {
        // Simple input validation
        if (batch.getProduceName().trim().isEmpty()) {
            System.out.println("Error: Produce name cannot be empty.");
            return;
        }
        if (batch.getQuantity() <= 0) {
            System.out.println("Error: Quantity must be greater than zero.");
            return;
        }
        if (batch.getPricePerKg() < 0) {
            System.out.println("Error: Price cannot be negative.");
            return;
        }
        if (batch.getExpiryDate().isBefore(batch.getHarvestDate())) {
            System.out.println("Error: Expiry date cannot be earlier than harvest date.");
            return;
        }

        String insertProduceSQL = "INSERT INTO produce_batches (produce_name, quantity, harvest_date, expiry_date, price_per_kg) VALUES (?, ?, ?, ?, ?)";
        String insertTxSQL = "INSERT INTO stock_transactions (batch_id, transaction_type, quantity) VALUES (?, 'ADDITION', ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertProduceSQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, batch.getProduceName());
            pstmt.setDouble(2, batch.getQuantity());
            pstmt.setDate(3, Date.valueOf(batch.getHarvestDate()));
            pstmt.setDate(4, Date.valueOf(batch.getExpiryDate()));
            pstmt.setDouble(5, batch.getPricePerKg());

            pstmt.executeUpdate();

            // Get generated batch ID
            int newBatchId = 0;
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newBatchId = generatedKeys.getInt(1);
                }
            }

            // Record initial addition in stock_transactions
            try (PreparedStatement txStmt = conn.prepareStatement(insertTxSQL)) {
                txStmt.setInt(1, newBatchId);
                txStmt.setDouble(2, batch.getQuantity());
                txStmt.executeUpdate();
            }

            System.out.println("Produce batch added successfully with Batch ID: " + newBatchId);

        } catch (SQLException e) {
            System.out.println("Database Error while adding produce: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------
    // 2. VIEW ALL PRODUCE
    // -------------------------------------------------------------
    public void viewAllProduce() {
        String sql = "SELECT * FROM produce_batches ORDER BY batch_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("--------------------------------------------------------------------------------------------------");
            System.out.printf("%-10s %-15s %-15s %-15s %-15s %-12s%n",
                    "Batch ID", "Produce Name", "Quantity (kg)", "Harvest Date", "Expiry Date", "Price/kg");
            System.out.println("--------------------------------------------------------------------------------------------------");

            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                System.out.printf("%-10d %-15s %-15.2f %-15s %-15s Rs. %-8.2f%n",
                        rs.getInt("batch_id"),
                        rs.getString("produce_name"),
                        rs.getDouble("quantity"),
                        rs.getDate("harvest_date").toString(),
                        rs.getDate("expiry_date").toString(),
                        rs.getDouble("price_per_kg"));
            }

            if (!hasRecords) {
                System.out.println("No produce batches found in the inventory.");
            }
            System.out.println("--------------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("Database Error while viewing produce: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------
    // 3. SEARCH PRODUCE BY ID
    // -------------------------------------------------------------
    public void searchProduce(int batchId) {
        String sql = "SELECT * FROM produce_batches WHERE batch_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, batchId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("--- Produce Batch Found ---");
                    System.out.println("Batch ID     : " + rs.getInt("batch_id"));
                    System.out.println("Produce Name : " + rs.getString("produce_name"));
                    System.out.println("Quantity     : " + rs.getDouble("quantity") + " kg");
                    System.out.println("Harvest Date : " + rs.getDate("harvest_date"));
                    System.out.println("Expiry Date  : " + rs.getDate("expiry_date"));
                    System.out.println("Price per kg : Rs. " + rs.getDouble("price_per_kg"));
                } else {
                    System.out.println("Produce batch with ID " + batchId + " not found.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Database Error while searching produce: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------
    // 4. UPDATE QUANTITY
    // -------------------------------------------------------------
    public void updateQuantity(int batchId, double newQuantity) {
        if (newQuantity < 0) {
            System.out.println("Error: Quantity cannot be negative.");
            return;
        }

        String checkSQL = "SELECT quantity FROM produce_batches WHERE batch_id = ?";
        String updateSQL = "UPDATE produce_batches SET quantity = ? WHERE batch_id = ?";
        String txSQL = "INSERT INTO stock_transactions (batch_id, transaction_type, quantity) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            double oldQuantity = -1;

            // Check if batch exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
                checkStmt.setInt(1, batchId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        oldQuantity = rs.getDouble("quantity");
                    } else {
                        System.out.println("Batch ID " + batchId + " not found.");
                        return;
                    }
                }
            }

            // Update batch quantity
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                updateStmt.setDouble(1, newQuantity);
                updateStmt.setInt(2, batchId);
                updateStmt.executeUpdate();
            }

            // Record transaction if quantity changed
            double diff = newQuantity - oldQuantity;
            if (diff != 0) {
                String txType = diff > 0 ? "ADDITION" : "DEDUCTION";
                try (PreparedStatement txStmt = conn.prepareStatement(txSQL)) {
                    txStmt.setInt(1, batchId);
                    txStmt.setString(2, txType);
                    txStmt.setDouble(3, Math.abs(diff));
                    txStmt.executeUpdate();
                }
            }

            System.out.println("Quantity for Batch " + batchId + " updated from " + oldQuantity + " kg to " + newQuantity + " kg.");

        } catch (SQLException e) {
            System.out.println("Database Error while updating quantity: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------
    // 5. DELETE PRODUCE
    // -------------------------------------------------------------
    public void deleteProduce(int batchId) {
        String sql = "DELETE FROM produce_batches WHERE batch_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, batchId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Produce batch " + batchId + " deleted successfully.");
            } else {
                System.out.println("Batch ID " + batchId + " not found.");
            }

        } catch (SQLException e) {
            System.out.println("Database Error while deleting produce: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------
    // 6. TRACK EXPIRY
    // -------------------------------------------------------------
    public void trackExpiry() {
        String sql = "SELECT * FROM produce_batches ORDER BY expiry_date ASC";

        ArrayList<ProduceBatch> expiredList = new ArrayList<>();
        ArrayList<ProduceBatch> expiringSoonList = new ArrayList<>();
        ArrayList<ProduceBatch> freshList = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ProduceBatch batch = new ProduceBatch(
                        rs.getInt("batch_id"),
                        rs.getString("produce_name"),
                        rs.getDouble("quantity"),
                        rs.getDate("harvest_date").toLocalDate(),
                        rs.getDate("expiry_date").toLocalDate(),
                        rs.getDouble("price_per_kg")
                );

                LocalDate expiry = batch.getExpiryDate();

                if (expiry.isBefore(today)) {
                    expiredList.add(batch);
                } else if (!expiry.isAfter(threeDaysLater)) {
                    expiringSoonList.add(batch);
                } else {
                    freshList.add(batch);
                }
            }

            // Display results simply
            System.out.println("\n=== 1. EXPIRED BATCHES (Do Not Consume/Sell) ===");
            if (expiredList.isEmpty()) {
                System.out.println("None");
            } else {
                for (ProduceBatch b : expiredList) {
                    System.out.println("Batch " + b.getBatchId() + " - " + b.getProduceName() + " (" + b.getQuantity() + " kg), Expired on: " + b.getExpiryDate());
                }
            }

            System.out.println("\n=== 2. EXPIRING SOON (Within Next 3 Days) ===");
            if (expiringSoonList.isEmpty()) {
                System.out.println("None");
            } else {
                for (ProduceBatch b : expiringSoonList) {
                    System.out.println("Batch " + b.getBatchId() + " - " + b.getProduceName() + " (" + b.getQuantity() + " kg), Expires on: " + b.getExpiryDate());
                }
            }

            System.out.println("\n=== 3. FRESH STOCK (Safe Storage) ===");
            if (freshList.isEmpty()) {
                System.out.println("None");
            } else {
                for (ProduceBatch b : freshList) {
                    System.out.println("Batch " + b.getBatchId() + " - " + b.getProduceName() + " (" + b.getQuantity() + " kg), Expires on: " + b.getExpiryDate());
                }
            }

        } catch (SQLException e) {
            System.out.println("Database Error while tracking expiry: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------
    // 7. ALLOCATE STOCK (FIFO)
    // -------------------------------------------------------------
    public void allocateStockFIFO(String produceName, double requestedQuantity) {
        if (requestedQuantity <= 0) {
            System.out.println("Error: Requested quantity must be greater than zero.");
            return;
        }

        // Simple query ordered by harvest_date ASC, batch_id ASC to pick oldest batches first
        String query = "SELECT * FROM produce_batches WHERE LOWER(produce_name) = LOWER(?) AND quantity > 0 AND expiry_date >= CURDATE() ORDER BY harvest_date ASC, batch_id ASC";

        ArrayList<ProduceBatch> eligibleBatches = new ArrayList<>();
        double totalAvailable = 0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, produceName.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ProduceBatch batch = new ProduceBatch(
                            rs.getInt("batch_id"),
                            rs.getString("produce_name"),
                            rs.getDouble("quantity"),
                            rs.getDate("harvest_date").toLocalDate(),
                            rs.getDate("expiry_date").toLocalDate(),
                            rs.getDouble("price_per_kg")
                    );
                    eligibleBatches.add(batch);
                    totalAvailable += batch.getQuantity();
                }
            }

        } catch (SQLException e) {
            System.out.println("Database Error reading batches: " + e.getMessage());
            return;
        }

        // Check if stock is sufficient
        if (eligibleBatches.isEmpty()) {
            System.out.println("No active, fresh stock found for " + produceName + ".");
            return;
        }

        if (requestedQuantity > totalAvailable) {
            System.out.println("Insufficient stock! Available: " + totalAvailable + " kg, Requested: " + requestedQuantity + " kg.");
            return;
        }

        // Perform deduction using a JDBC Transaction with commit and rollback
        String updateSQL = "UPDATE produce_batches SET quantity = ? WHERE batch_id = ?";
        String txSQL = "INSERT INTO stock_transactions (batch_id, transaction_type, quantity) VALUES (?, 'DEDUCTION', ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();

            // STEP 1: Disable auto-commit to start a database transaction.
            // All updates across multiple batches will be treated as one single atomic unit.
            conn.setAutoCommit(false);

            double remainingToDeduct = requestedQuantity;

            System.out.println("\n--- FIFO Deduction Breakdown ---");

            for (ProduceBatch batch : eligibleBatches) {
                if (remainingToDeduct <= 0) {
                    break;
                }

                double batchQty = batch.getQuantity();
                double deductAmount;

                if (batchQty <= remainingToDeduct) {
                    deductAmount = batchQty;
                } else {
                    deductAmount = remainingToDeduct;
                }

                double newQty = batchQty - deductAmount;

                // Update batch quantity in MySQL
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                    updateStmt.setDouble(1, newQty);
                    updateStmt.setInt(2, batch.getBatchId());
                    updateStmt.executeUpdate();
                }

                // Record deduction in stock_transactions table
                try (PreparedStatement txStmt = conn.prepareStatement(txSQL)) {
                    txStmt.setInt(1, batch.getBatchId());
                    txStmt.setDouble(2, deductAmount);
                    txStmt.executeUpdate();
                }

                System.out.println("Deducted " + deductAmount + " kg from Batch " + batch.getBatchId()
                        + " (Harvest: " + batch.getHarvestDate() + "). Remaining in batch: " + newQty + " kg.");

                remainingToDeduct -= deductAmount;
            }

            // STEP 2: Commit the transaction.
            // commit() permanently saves all changes to the database if everything succeeded.
            conn.commit();
            System.out.println("FIFO Stock Allocation completed successfully!");

        } catch (SQLException e) {
            System.out.println("Error during FIFO allocation: " + e.getMessage());
            // STEP 3: Rollback the transaction on failure.
            // rollback() undoes any partial deductions so the database remains in a consistent state.
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Transaction rolled back. No partial changes were saved.");
                } catch (SQLException ex) {
                    System.out.println("Error rolling back: " + ex.getMessage());
                }
            }
        } finally {
            // Restore default auto-commit and close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.out.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    // -------------------------------------------------------------
    // 8. VIEW STOCK TRANSACTIONS
    // -------------------------------------------------------------
    public void viewTransactions() {
        String sql = "SELECT * FROM stock_transactions ORDER BY transaction_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("----------------------------------------------------------------------------------");
            System.out.printf("%-15s %-12s %-18s %-15s %-20s%n",
                    "Transaction ID", "Batch ID", "Type", "Quantity (kg)", "Date & Time");
            System.out.println("----------------------------------------------------------------------------------");

            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                System.out.printf("%-15d %-12d %-18s %-15.2f %-20s%n",
                        rs.getInt("transaction_id"),
                        rs.getInt("batch_id"),
                        rs.getString("transaction_type"),
                        rs.getDouble("quantity"),
                        rs.getTimestamp("transaction_date").toString());
            }

            if (!hasRecords) {
                System.out.println("No stock transactions recorded yet.");
            }
            System.out.println("----------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("Database Error viewing transactions: " + e.getMessage());
        }
    }
}
