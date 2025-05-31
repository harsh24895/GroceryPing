package com.example.groceryping.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "store_locations")
public class StoreLocation {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String address;
    private float latitude;
    private float longitude;
    private boolean isActive;
    private float radius; // Radius in meters for geofencing

    // Constructor with all fields - used by Room
    public StoreLocation(String name, String address, float latitude, float longitude, float radius) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isActive = true;
        this.radius = radius;
    }

    // Simplified constructor - ignored by Room
    @Ignore
    public StoreLocation(String name, String address) {
        this.name = name;
        this.address = address;
        this.latitude = 0.0f;
        this.longitude = 0.0f;
        this.radius = 100.0f; // Default radius of 100 meters
        this.isActive = true;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public boolean isActive() {
        return isActive;
    }

    public float getRadius() {
        return radius;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
} 