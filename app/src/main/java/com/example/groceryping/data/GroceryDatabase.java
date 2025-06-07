package com.example.groceryping.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {GroceryItem.class, StoreLocation.class, Reminder.class}, version = 2, exportSchema = false)
@TypeConverters({})
public abstract class
GroceryDatabase extends RoomDatabase {
    private static GroceryDatabase instance;

    public abstract GroceryItemDao groceryItemDao();
    public abstract StoreLocationDao storeLocationDao();
    public abstract ReminderDao reminderDao();

    public static synchronized GroceryDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    GroceryDatabase.class, "grocery_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
} 