package com.example.groceryping;

import android.app.Application;
import android.util.Log;
import com.example.groceryping.data.GroceryDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroceryApplication extends Application {
    private static final String TAG = "GroceryApplication";
    private static GroceryDatabase database;
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            // Initialize database in background
            executorService.execute(this::initializeDatabase);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database", e);
        }
    }

    private void initializeDatabase() {
        try {
            if (database == null) {
                synchronized (GroceryApplication.class) {
                    if (database == null) {
                        database = GroceryDatabase.getInstance(this);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize database", e);
        }
    }

    public static GroceryDatabase getDatabase() {
        if (database == null) {
            synchronized (GroceryApplication.class) {
                if (database == null) {
                    throw new IllegalStateException("Database not initialized. Make sure GroceryApplication.onCreate() is called.");
                }
            }
        }
        return database;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        executorService.shutdown();
    }
} 