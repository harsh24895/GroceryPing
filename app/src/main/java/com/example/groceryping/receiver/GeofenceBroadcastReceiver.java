package com.example.groceryping.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.groceryping.MainActivity;
import com.example.groceryping.R;
import com.example.groceryping.data.GroceryDatabase;
import com.example.groceryping.data.StoreLocation;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceReceiver";
    private static final String CHANNEL_ID = "geofence_notification_channel";
    private static final int NOTIFICATION_ID = 1000;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Geofence broadcast received");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent == null) {
            Log.e(TAG, "GeofencingEvent is null");
            return;
        }

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "GeofencingEvent error: " + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

        if (triggeringGeofences == null) {
            Log.e(TAG, "No triggering geofences");
            return;
        }

        Log.d(TAG, "Geofence transition: " + geofenceTransition);
        Log.d(TAG, "Number of triggering geofences: " + triggeringGeofences.size());

        // Handle the transition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            for (Geofence geofence : triggeringGeofences) {
                String storeId = geofence.getRequestId();
                Log.d(TAG, "Entered geofence for store ID: " + storeId);
                
                // Get store details from database and show notification
                executorService.execute(() -> {
                    StoreLocation store = GroceryDatabase.getInstance(context)
                            .storeLocationDao()
                            .getStoreById(Long.parseLong(storeId));
                    
                    if (store != null) {
                        Log.d(TAG, "Found store: " + store.getName() + " at " + store.getAddress());
                        showNotification(context, store);
                    } else {
                        Log.e(TAG, "Store not found for ID: " + storeId);
                    }
                });
            }
        }
    }

    private void showNotification(Context context, StoreLocation store) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Nearby Store Alert")
            .setContentText("You're near " + store.getName())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) 
            context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Geofence Notifications";
            String description = "Notifications for store geofence events";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
} 