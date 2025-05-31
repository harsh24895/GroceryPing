package com.example.groceryping;

import android.app.Application;
import com.example.groceryping.data.GroceryDatabase;

public class GroceryApplication extends Application {
    private static GroceryDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = GroceryDatabase.getInstance(this);
    }

    public static GroceryDatabase getDatabase() {
        return database;
    }
} 