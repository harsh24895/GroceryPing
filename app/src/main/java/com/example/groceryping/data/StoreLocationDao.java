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
public interface StoreLocationDao {
    @Query("SELECT * FROM store_locations ORDER BY name ASC")
    LiveData<List<StoreLocation>> getAllStores();

    @Query("SELECT * FROM store_locations WHERE isActive = 1 ORDER BY name ASC")
    LiveData<List<StoreLocation>> getActiveStores();

    @Query("SELECT * FROM store_locations WHERE id = :id")
    StoreLocation getStoreById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StoreLocation store);

    @Update
    void update(StoreLocation store);

    @Delete
    void delete(StoreLocation store);

    @Query("DELETE FROM store_locations")
    void deleteAll();
} 