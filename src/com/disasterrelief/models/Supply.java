package com.disasterrelief.models;

import java.time.LocalDate;

public class Supply {

    private int supplyId;
    private String itemName;
    private int quantity;
    private String type;
    private LocalDate expiryDate;

    public Supply() {}

    public Supply(int supplyId, String itemName, int quantity,
                  String type, LocalDate expiryDate) {
        this.supplyId = supplyId;
        setItemName(itemName);
        setQuantity(quantity);
        setType(type);
        setExpiryDate(expiryDate);
    }

    public int getSupplyId() { return supplyId; }
    public void setSupplyId(int supplyId) { this.supplyId = supplyId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) {
        if (itemName == null || itemName.isBlank())
            throw new IllegalArgumentException("Item name cannot be empty");
        this.itemName = itemName.trim();
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        if (quantity < 0)
            throw new IllegalArgumentException("Quantity cannot be negative");
        this.quantity = quantity;
    }

    public String getType() { return type; }
    public void setType(String type) {
        if (type == null || type.isBlank())
            throw new IllegalArgumentException("Type cannot be empty");
        this.type = type.trim();
    }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public String toString() {
        return "Supply [supplyId=" + supplyId + ", itemName=" + itemName +
               ", quantity=" + quantity + ", expiryDate=" + expiryDate + "]";
    }
}
