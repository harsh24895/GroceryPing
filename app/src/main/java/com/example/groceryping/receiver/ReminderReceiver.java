package com.example.groceryping.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.groceryping.helper.NotificationHelper;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        android.util.Log.d("ReminderReceiver", "Received reminder broadcast");
        
        String itemName = intent.getStringExtra("item_name");
        String message = intent.getStringExtra("message");
        String reminderId = intent.getStringExtra("reminder_id");

        android.util.Log.d("ReminderReceiver", "Reminder ID: " + reminderId);
        android.util.Log.d("ReminderReceiver", "Item Name: " + itemName);
        android.util.Log.d("ReminderReceiver", "Message: " + message);

        if (itemName != null && message != null) {
            android.util.Log.d("ReminderReceiver", "Showing notification for: " + itemName);
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.showReminderNotification(itemName, message);
            android.util.Log.d("ReminderReceiver", "Notification shown successfully");
        } else {
            android.util.Log.e("ReminderReceiver", "Missing reminder data in intent");
        }
    }
} 