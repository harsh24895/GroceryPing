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
public interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY timeInMillis ASC")
    LiveData<List<Reminder>> getAllReminders();

    @Query("SELECT * FROM reminders WHERE isActive = 1 ORDER BY timeInMillis ASC")
    LiveData<List<Reminder>> getActiveReminders();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("DELETE FROM reminders")
    void deleteAll();
} 