package com.example.groceryping.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.UUID;

@Entity(tableName = "grocery_items")
public class GroceryItem {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String location;
    private boolean isCompleted;
    private String category;
    private double price;
    private int quantity;

    // Constructor for Room
    public GroceryItem(@NonNull String id, String name, String location, boolean isCompleted, 
                      String category, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.isCompleted = isCompleted;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
    }

    // Constructor for creating new items
    @Ignore
    public GroceryItem(String name, String location, String category, double price, int quantity) {
        this(UUID.randomUUID().toString(), name, location, false, category, price, quantity);
    }

    // Getters
    @NonNull
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    // Setters for Room
    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public GroceryItem withCompleted(boolean completed) {
        return new GroceryItem(id, name, location, completed, category, price, quantity);
    }
} 