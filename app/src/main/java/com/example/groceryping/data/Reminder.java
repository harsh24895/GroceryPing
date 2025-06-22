package com.example.groceryping.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.UUID;
import java.util.Calendar;

@Entity(tableName = "reminders")
public class Reminder {
    @PrimaryKey
    @NonNull
    private String id = java.util.UUID.randomUUID().toString();
    private String itemName;
    private String message;
    private long timeInMillis;
    private boolean isRepeating;
    private int repeatInterval; // 0: one-time, 1: daily, 7: weekly
    private boolean isActive;

    // Default constructor for Firebase
    public Reminder() {}

    public Reminder(String itemName, String message, long timeInMillis, boolean isRepeating, int repeatInterval) {
        this.id = java.util.UUID.randomUUID().toString();
        this.itemName = itemName;
        this.message = message;
        this.timeInMillis = timeInMillis;
        this.isRepeating = isRepeating;
        this.repeatInterval = repeatInterval;
        this.isActive = true;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
    }

    public int getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(int repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getFormattedTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    public String getFormattedDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return String.format("%d/%d/%d", 
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR));
    }
} 