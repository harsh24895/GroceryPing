package com.example.groceryping.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface GroceryItemDao {
    @Query("SELECT * FROM grocery_items ORDER BY category, name")
    LiveData<List<GroceryItem>> getAllItems();

    @Query("SELECT * FROM grocery_items WHERE category = :category ORDER BY name")
    LiveData<List<GroceryItem>> getItemsByCategory(String category);

    @Query("SELECT DISTINCT category FROM grocery_items ORDER BY category")
    LiveData<List<String>> getAllCategories();

    @Query("SELECT * FROM grocery_items WHERE name LIKE :searchQuery OR category LIKE :searchQuery ORDER BY category, name")
    LiveData<List<GroceryItem>> searchItems(String searchQuery);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GroceryItem item);

    @Update
    void update(GroceryItem item);

    @Delete
    void delete(GroceryItem item);

    @Query("DELETE FROM grocery_items")
    void deleteAll();
} 