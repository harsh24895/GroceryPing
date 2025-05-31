package com.example.groceryping.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.groceryping.MainActivity;
import com.example.groceryping.R;
import com.example.groceryping.data.GroceryDatabase;
import com.example.groceryping.data.StoreLocation;
import com.example.groceryping.receiver.GeofenceBroadcastReceiver;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class LocationService extends Service {
    private static final String TAG = "LocationService";
    private static final String CHANNEL_ID = "location_service_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final String WAKE_LOCK_TAG = "GroceryPing:LocationService";

    private GeofencingClient geofencingClient;
    private List<Geofence> geofenceList;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate called");
        try {
            geofencingClient = LocationServices.getGeofencingClient(this);
            createNotificationChannel();
            startForeground(NOTIFICATION_ID, createNotification());
            acquireWakeLock();
            Log.d(TAG, "Service setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
        }
    }

    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            WAKE_LOCK_TAG
        );
        wakeLock.acquire(10*60*1000L /*10 minutes*/);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");
        try {
            setupGeofences();
            return START_STICKY;
        } catch (Exception e) {
            Log.e(TAG, "Error in onStartCommand: " + e.getMessage(), e);
            return START_NOT_STICKY;
        }
    }

    private void setupGeofences() {
        Log.d(TAG, "Setting up geofences");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted");
            return;
        }

        try {
            // Get active stores from database
            GroceryDatabase.getInstance(this).storeLocationDao().getActiveStores().observeForever(stores -> {
                Log.d(TAG, "Active stores received: " + (stores != null ? stores.size() : 0));
                if (stores == null || stores.isEmpty()) {
                    Log.d(TAG, "No active stores found");
                    return;
                }

                try {
                    // Remove existing geofences first
                    geofencingClient.removeGeofences(getGeofencePendingIntent())
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Existing geofences removed");
                            addNewGeofences(stores);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to remove existing geofences", e);
                            // Try to add new geofences anyway
                            addNewGeofences(stores);
                        });
                } catch (Exception e) {
                    Log.e(TAG, "Error setting up geofences: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in setupGeofences: " + e.getMessage(), e);
        }
    }

    private void addNewGeofences(List<StoreLocation> stores) {
        try {
            if (stores == null || stores.isEmpty()) {
                Log.d(TAG, "No stores to add geofences for");
                return;
            }

            geofenceList = new ArrayList<>();
            for (StoreLocation store : stores) {
                if (store == null) {
                    Log.e(TAG, "Null store found in list");
                    continue;
                }

                Log.d(TAG, "Creating geofence for store: " + store.getName() + 
                    " at location: " + store.getLatitude() + ", " + store.getLongitude() +
                    " with radius: " + store.getRadius() + "m");
                
                try {
                    Geofence geofence = new Geofence.Builder()
                        .setRequestId(String.valueOf(store.getId()))
                        .setCircularRegion(
                            store.getLatitude(),
                            store.getLongitude(),
                            store.getRadius()
                        )
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .setLoiteringDelay(30000) // 30 seconds
                        .build();
                    
                    geofenceList.add(geofence);
                } catch (Exception e) {
                    Log.e(TAG, "Error creating geofence for store: " + store.getName(), e);
                }
            }

            if (geofenceList.isEmpty()) {
                Log.e(TAG, "No valid geofences created");
                return;
            }

            GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build();

            geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Geofences added successfully. Total geofences: " + geofenceList.size());
                    for (Geofence geofence : geofenceList) {
                        Log.d(TAG, "Active geofence: " + geofence.getRequestId());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add geofences. Error code: " + e.getMessage(), e);
                    if (e instanceof com.google.android.gms.common.api.ApiException) {
                        com.google.android.gms.common.api.ApiException apiException = 
                            (com.google.android.gms.common.api.ApiException) e;
                        Log.e(TAG, "API Exception status code: " + apiException.getStatusCode());
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Error adding new geofences: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        Log.d(TAG, "Creating notification channel");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_LOW
                );
                channel.setDescription("Background location service for store geofencing");
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channel: " + e.getMessage(), e);
            }
        }
    }

    private android.app.Notification createNotification() {
        Log.d(TAG, "Creating notification");
        try {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            );

            return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GroceryPing Location Service")
                .setContentText("Monitoring nearby stores")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
        } catch (Exception e) {
            Log.e(TAG, "Error creating notification: " + e.getMessage(), e);
            return null;
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        try {
            Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
            return PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        } catch (Exception e) {
            Log.e(TAG, "Error getting geofence pending intent: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        try {
            if (geofencingClient != null) {
                geofencingClient.removeGeofences(getGeofencePendingIntent())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Geofences removed successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to remove geofences", e));
            }
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage(), e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
} 