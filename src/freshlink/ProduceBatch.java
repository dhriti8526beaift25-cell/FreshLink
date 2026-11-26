package freshlink;

import java.time.LocalDate;

/**
 * Represents an individual produce batch in the inventory.
 */
public class ProduceBatch {
    private int batchId;
    private String produceName;
    private double quantity;
    private LocalDate harvestDate;
    private LocalDate expiryDate;
    private double pricePerKg;

    // Default constructor
    public ProduceBatch() {
    }

    // Constructor without batchId (used when inserting a new batch)
    public ProduceBatch(String produceName, double quantity, LocalDate harvestDate, LocalDate expiryDate, double pricePerKg) {
        this.produceName = produceName;
        this.quantity = quantity;
        this.harvestDate = harvestDate;
        this.expiryDate = expiryDate;
        this.pricePerKg = pricePerKg;
    }

    // Constructor with batchId (used when reading from database)
    public ProduceBatch(int batchId, String produceName, double quantity, LocalDate harvestDate, LocalDate expiryDate, double pricePerKg) {
        this.batchId = batchId;
        this.produceName = produceName;
        this.quantity = quantity;
        this.harvestDate = harvestDate;
        this.expiryDate = expiryDate;
        this.pricePerKg = pricePerKg;
    }

    // Getters and Setters
    public int getBatchId() {
        return batchId;
    }

    public void setBatchId(int batchId) {
        this.batchId = batchId;
    }

    public String getProduceName() {
        return produceName;
    }

    public void setProduceName(String produceName) {
        this.produceName = produceName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public LocalDate getHarvestDate() {
        return harvestDate;
    }

    public void setHarvestDate(LocalDate harvestDate) {
        this.harvestDate = harvestDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public double getPricePerKg() {
        return pricePerKg;
    }

    public void setPricePerKg(double pricePerKg) {
        this.pricePerKg = pricePerKg;
    }
}
