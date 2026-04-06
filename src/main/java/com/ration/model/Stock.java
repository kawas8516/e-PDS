package com.ration.model;


/**
 * Model class representing a commodity in the warehouse.
 */
public class Stock {
    private int itemId;
    private String itemName;
    private double currentQuantity;
    private double unitPrice;
    private double thresholdLimit; // Alert when stock falls below this

    public Stock(int itemId, String itemName, double currentQuantity, double unitPrice, double thresholdLimit) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.currentQuantity = currentQuantity;
        this.unitPrice = unitPrice;
        this.thresholdLimit = thresholdLimit;
    }

    // Getters and Setters
    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public double getCurrentQuantity() { return currentQuantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getThresholdLimit() { return thresholdLimit; }
    
    public void setCurrentQuantity(double currentQuantity) { this.currentQuantity = currentQuantity; }
}