package com.example.groceryping.data;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {GroceryItem.class, StoreLocation.class, Reminder.class}, version = 2, exportSchema = false)
@TypeConverters({})
public abstract class GroceryDatabase extends RoomDatabase {
    private static final String TAG = "GroceryDatabase";
    private static volatile GroceryDatabase instance;

    public abstract GroceryItemDao groceryItemDao();
    public abstract StoreLocationDao storeLocationDao();
    public abstract ReminderDao reminderDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            try {
                // Add any necessary migrations here
                database.execSQL("ALTER TABLE grocery_items ADD COLUMN last_updated INTEGER DEFAULT 0");
            } catch (Exception e) {
                Log.e(TAG, "Error during migration", e);
            }
        }
    };

    public static synchronized GroceryDatabase getInstance(Context context) {
        if (instance == null) {
            try {
                instance = Room.databaseBuilder(context.getApplicationContext(),
                        GroceryDatabase.class, "grocery_database")
                        .addMigrations(MIGRATION_1_2)
                        .fallbackToDestructiveMigration()
                        .addCallback(new RoomDatabase.Callback() {
                            @Override
                            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                super.onCreate(db);
                                try {
                                    // Enable foreign keys
                                    db.execSQL("PRAGMA foreign_keys = ON;");
                                } catch (Exception e) {
                                    Log.e(TAG, "Error enabling foreign keys", e);
                                }
                            }

                            @Override
                            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                super.onOpen(db);
                                try {
                                    // Enable foreign keys
                                    db.execSQL("PRAGMA foreign_keys = ON;");
                                } catch (Exception e) {
                                    Log.e(TAG, "Error enabling foreign keys", e);
                                }
                            }
                        })
                        .build();
            } catch (Exception e) {
                Log.e(TAG, "Error creating database instance", e);
                throw new RuntimeException("Failed to create database instance", e);
            }
        }
        return instance;
    }
} 