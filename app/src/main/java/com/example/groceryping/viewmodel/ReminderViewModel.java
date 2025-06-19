package com.example.groceryping.viewmodel;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.groceryping.data.GroceryDatabase;
import com.example.groceryping.data.Reminder;
import com.example.groceryping.data.ReminderDao;
import com.example.groceryping.receiver.ReminderReceiver;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderViewModel extends AndroidViewModel {
    private final LiveData<List<Reminder>> allReminders;
    private final AlarmManager alarmManager;
    private final ReminderDao reminderDao;
    private final ExecutorService executorService;

    public ReminderViewModel(Application application) {
        super(application);
        this.alarmManager = (AlarmManager) application.getSystemService(Context.ALARM_SERVICE);
        this.reminderDao = GroceryDatabase.getInstance(application).reminderDao();
        this.allReminders = reminderDao.getAllReminders();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Reminder>> getAllReminders() {
        return allReminders;
    }

    public void addReminder(Reminder reminder) {
        executorService.execute(() -> {
            Log.d("ReminderViewModel", "Adding reminder: " + reminder.getItemName());
            reminderDao.insert(reminder);
            scheduleReminder(reminder);
        });
    }

    public void updateReminder(Reminder reminder) {
        executorService.execute(() -> {
            Log.d("ReminderViewModel", "Updating reminder: " + reminder.getItemName());
            reminderDao.update(reminder);
            scheduleReminder(reminder);
        });
    }

    public void cancelReminder(Reminder reminder) {
        executorService.execute(() -> {
            Log.d("ReminderViewModel", "Canceling reminder: " + reminder.getItemName());
            
            // Cancel the alarm
            Intent intent = new Intent(getApplication(), ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplication(),
                reminder.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(pendingIntent);

            // Delete the reminder from the database
            reminderDao.delete(reminder);
            
            Log.d("ReminderViewModel", "Reminder deleted from database: " + reminder.getItemName());
        });
    }

    private void scheduleReminder(Reminder reminder) {
        Log.d("ReminderViewModel", "Scheduling reminder: " + reminder.getItemName() + 
            " for " + new java.util.Date(reminder.getTimeInMillis()));
        
        Intent intent = new Intent(getApplication(), ReminderReceiver.class);
        intent.putExtra("reminder_id", reminder.getId());
        intent.putExtra("item_name", reminder.getItemName());
        intent.putExtra("message", reminder.getMessage());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            reminder.getId().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (reminder.isRepeating()) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                reminder.getTimeInMillis(),
                reminder.getRepeatInterval() * 24 * 60 * 60 * 1000L,
                pendingIntent
            );
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                reminder.getTimeInMillis(),
                pendingIntent
            );
        }
        
        Log.d("ReminderViewModel", "Reminder scheduled successfully");
    }

    public void refreshData() {
        // Force a refresh by triggering the LiveData
        // This will cause the observers to be notified with the latest data
        executorService.execute(() -> {
            // The LiveData will automatically update when the database changes
            // This method can be used to trigger a manual refresh if needed
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
} 