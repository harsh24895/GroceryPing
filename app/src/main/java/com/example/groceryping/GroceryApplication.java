package com.example.groceryping;

import android.app.Application;
import android.util.Log;
import com.example.groceryping.data.GroceryDatabase;

public class GroceryApplication extends Application {
    private static final String TAG = "GroceryApplication";
    private static GroceryDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            initializeDatabase();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database", e);
        }
    }

    private void initializeDatabase() {
        try {
            database = GroceryDatabase.getInstance(this);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public static GroceryDatabase getDatabase() {
        if (database == null) {
            throw new IllegalStateException("Database not initialized. Make sure GroceryApplication.onCreate() is called.");
        }
        return database;
    }
} 