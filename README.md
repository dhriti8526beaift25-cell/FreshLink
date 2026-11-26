# FreshLink – Smart Produce Management System

**A Beginner-Friendly College Java & JDBC Project (Third Semester Level)**

---

## 📌 1. Project Overview

**FreshLink** is a simple, structured console-based Java application built using **Core Java, JDBC, and MySQL**.

It solves a real-world agricultural challenge: **produce spoilage and financial loss caused by poor stock rotation**.

### Key Capabilities:
- **Produce Batch Management**: Register, view, search, update, and delete batches of agricultural produce.
- **Expiry Tracking**: Categorise produce into **Expired**, **Expiring Soon (within 3 days)**, and **Fresh Stock** using `LocalDate`.
- **First-In, First-Out (FIFO) Stock Allocation**: Dispatches the oldest eligible produce first using standard SQL sorting and a simple loop, backed by a JDBC transaction (`commit` & `rollback`).
- **Stock Audit Ledger**: Automatically records every stock addition and deduction with timestamps in MySQL.

---

## 🏗️ 2. Technology Stack

- **Language**: Core Java (Java SE 8+)
- **Database**: MySQL Server 8.0+
- **Connectivity**: JDBC with `PreparedStatement`
- **Driver**: MySQL Connector/J (`mysql-connector-j.jar`)
- **IDE**: Eclipse IDE / Terminal

---

## 📁 3. Project Structure

```
FreshLink/
│
├── src/
│   └── freshlink/
│       ├── Main.java             # Console menu and user input handling (Scanner)
│       ├── ProduceBatch.java     # Model class with private fields, getters & setters
│       ├── ProduceDAO.java       # Database Access Object for all SQL operations
│       └── DBConnection.java     # Simple MySQL connection provider using DriverManager
│
├── database/
│   └── freshlink.sql             # SQL script for database creation and sample data
│
├── lib/
│   └── mysql-connector-j.jar     # MySQL JDBC Driver JAR
│
├── bin/                          # Compiled .class files
└── README.md                     # Project documentation and viva guide
```

---

## 🗄️ 4. Database Design (`freshlink_db`)

The database consists of **two tables**:

### Table 1: `produce_batches`
Stores information about each harvested produce batch.
```sql
CREATE TABLE IF NOT EXISTS produce_batches (
    batch_id INT PRIMARY KEY AUTO_INCREMENT,
    produce_name VARCHAR(100) NOT NULL,
    quantity DOUBLE NOT NULL,
    harvest_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    price_per_kg DOUBLE NOT NULL
);
```

### Table 2: `stock_transactions`
Records every stock movement (`ADDITION` or `DEDUCTION`).
```sql
CREATE TABLE IF NOT EXISTS stock_transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    batch_id INT,
    transaction_type VARCHAR(20) NOT NULL,
    quantity DOUBLE NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (batch_id) REFERENCES produce_batches(batch_id) ON DELETE CASCADE
);
```

---

## ⚙️ 5. Setup & Running Instructions

### Step 1: Database Setup
1. Open your terminal or MySQL command line:
   ```bash
   mysql -u root -p
   ```
2. Run the `freshlink.sql` script:
   ```sql
   source /path/to/FreshLink/database/freshlink.sql;
   ```
   *(Or in PowerShell)*:
   ```powershell
   Get-Content database/freshlink.sql | mysql -u root -p
   ```

### Step 2: Database Credentials
Open `src/freshlink/DBConnection.java` and update your password if needed:
```java
private static final String URL = "jdbc:mysql://localhost:3306/freshlink_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "your_mysql_password";
```

### Step 3: Run the Application
#### In Eclipse IDE:
1. Open Eclipse ➡️ **File** ➡️ **Import...** ➡️ **Existing Projects into Workspace**.
2. Select the `project_freshlink` folder and click **Finish**.
3. Right-click `Main.java` ➡️ **Run As** ➡️ **Java Application**.

#### From Terminal / Command Prompt:
```bash
# Compile
javac -d bin -cp "lib/mysql-connector-j.jar" src/freshlink/*.java

# Run
java -cp "bin;lib/mysql-connector-j.jar" freshlink.Main
```

---

## 💻 6. Console Menu

```
========================================
              FRESHLINK                 
       SMART PRODUCE MANAGEMENT         
========================================
1. Add Produce
2. View All Produce
3. Search Produce
4. Update Quantity
5. Delete Produce
6. Track Expiry
7. Allocate Stock (FIFO)
8. View Stock Transactions
0. Exit
========================================
Enter your choice:
```

---

## 📖 7. Explanation of Every Class and Method

### 1. `DBConnection.java`
- **Purpose**: Central place to configure and establish database connections.
- **Method**:
  - `getConnection()`: Uses `DriverManager.getConnection(URL, USER, PASSWORD)` to return an active `Connection` object to `freshlink_db`.

### 2. `ProduceBatch.java`
- **Purpose**: Model class representing an agricultural produce batch.
- **Attributes**: `batchId`, `produceName`, `quantity`, `harvestDate`, `expiryDate`, `pricePerKg`.
- **Constructors**:
  - `ProduceBatch()`: Default constructor.
  - `ProduceBatch(name, qty, harvest, expiry, price)`: Constructor for inserting a new batch.
  - `ProduceBatch(id, name, qty, harvest, expiry, price)`: Constructor when reading records from MySQL.
- **Getters & Setters**: Standard methods providing clean **encapsulation**.

### 3. `ProduceDAO.java`
Contains all business logic and JDBC database operations:
- `addProduce(ProduceBatch batch)`: Validates inputs, inserts a new record into `produce_batches` using `PreparedStatement`, retrieves the auto-generated `batch_id`, and logs an `ADDITION` in `stock_transactions`.
- `viewAllProduce()`: Executes `SELECT * FROM produce_batches ORDER BY batch_id ASC` and prints a neat tabular overview of all produce in stock.
- `searchProduce(int batchId)`: Executes `SELECT * FROM produce_batches WHERE batch_id = ?` and displays the details of the batch if found.
- `updateQuantity(int batchId, double newQty)`: Updates the quantity for a batch in MySQL and logs the difference as `ADDITION` or `DEDUCTION` in `stock_transactions`.
- `deleteProduce(int batchId)`: Deletes a batch by ID (`DELETE FROM produce_batches WHERE batch_id = ?`).
- `trackExpiry()`: Fetches all batches and checks their `expiryDate` against `LocalDate.now()` using simple `isBefore()` and `plusDays(3)` comparisons, displaying **Expired**, **Expiring Soon**, and **Fresh** batches.
- `allocateStockFIFO(String produceName, double requestedQty)`:
  - Queries active, fresh batches sorted by oldest harvest date first:
    `SELECT * FROM produce_batches WHERE LOWER(produce_name) = LOWER(?) AND quantity > 0 AND expiry_date >= CURDATE() ORDER BY harvest_date ASC, batch_id ASC;`
  - Validates if total available stock is enough.
  - Uses a **JDBC Transaction**:
    - `conn.setAutoCommit(false)`: Starts the transaction.
    - Loops through batches deducting the required quantity from the oldest batch first.
    - Updates MySQL quantities and inserts transaction records.
    - `conn.commit()`: Saves all changes permanently if everything succeeded.
    - `conn.rollback()`: Cancels any partial deductions if an error occurs midway.
- `viewTransactions()`: Queries and displays all records from `stock_transactions`.

### 4. `Main.java`
- **Purpose**: Interactive command-line user interface.
- **Loop**: `while (choice != 0)` continuously presents the menu.
- **Input**: Uses `Scanner.nextLine()` with `Integer.parseInt()` / `Double.parseDouble()` to avoid common scanner skipping issues.
- **Flow**: Directs user choice to the corresponding method in `ProduceDAO`.

---

## 🔄 8. FIFO Allocation Explained With an Example

Suppose your warehouse has:
- **Batch 101 (Tomatoes)**: 50.0 kg, Harvested on 19 Nov
- **Batch 102 (Tomatoes)**: 30.0 kg, Harvested on 21 Nov
- **Batch 103 (Tomatoes)**: 40.0 kg, Harvested on 24 Nov

If a customer orders **60.0 kg of Tomatoes**:
1. FreshLink checks `60.0 <= (50 + 30 + 40)` (Sufficient stock exists).
2. FreshLink starts the transaction (`conn.setAutoCommit(false)`).
3. **Step 1**: Takes the oldest batch (**Batch 101**):
   - Deducts all 50.0 kg. Batch 101 quantity becomes **0.0 kg**.
   - Remaining needed: `60.0 - 50.0 = 10.0 kg`.
   - Logs `DEDUCTION` of 50.0 kg for Batch 101.
4. **Step 2**: Takes the next oldest batch (**Batch 102**):
   - Deducts the remaining 10.0 kg. Batch 102 quantity becomes **20.0 kg**.
   - Remaining needed: `10.0 - 10.0 = 0.0 kg` (Finished!).
   - Logs `DEDUCTION` of 10.0 kg for Batch 102.
5. **Step 3**: FreshLink permanently saves all changes using `conn.commit()`.

If an unexpected error occurs during step 4, `conn.rollback()` immediately restores Batch 101 back to 50.0 kg, preventing data corruption.

---

## 🎯 9. Viva Questions & Quick Answers

1. **Why did you use JDBC PreparedStatement instead of Statement?**
   - `PreparedStatement` pre-compiles SQL queries, handles data types automatically, improves performance, and prevents SQL Injection attacks.

2. **Why do we use commit() and rollback()?**
   - When deducting stock across multiple batches, we want all batch updates to succeed together. If one fails, `rollback()` cancels all partial changes, keeping the database consistent.

3. **How does FIFO prevent food wastage?**
   - By querying batches ordered by `harvest_date ASC` and excluding expired stock, older batches are dispatched first before they spoil in the warehouse.

4. **What OOP principles are used in this project?**
   - **Encapsulation**: Private variables with public getters and setters in `ProduceBatch`.
   - **Modularity / Separation of Concerns**: `DBConnection` handles connectivity, `ProduceDAO` handles database operations, `ProduceBatch` holds data, and `Main` handles user interaction.
