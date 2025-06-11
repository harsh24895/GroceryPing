package com.example.groceryping;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.groceryping.adapter.GroceryAdapter;
import com.example.groceryping.data.GroceryItem;
import com.example.groceryping.data.Reminder;
import com.example.groceryping.viewmodel.GroceryViewModel;
import com.example.groceryping.viewmodel.ReminderViewModel;
import com.example.groceryping.viewmodel.ReminderViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import android.view.ViewGroup;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.DatePicker;
import android.widget.TimePicker;
import java.util.Calendar;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import com.example.groceryping.adapter.GroceryAdapter.OnItemClickListener;
import com.example.groceryping.data.StoreLocation;
import com.example.groceryping.service.LocationService;
import android.content.Intent;
import android.widget.Switch;
import android.widget.Toast;
import android.os.Build;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.groceryping.data.GroceryDatabase;
import com.google.android.material.switchmaterial.SwitchMaterial;
import androidx.annotation.NonNull;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import com.example.groceryping.adapter.ReminderAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.util.Log;
import android.os.PowerManager;
import android.net.Uri;
import android.provider.Settings;
import com.example.groceryping.ads.AdManager;
import com.example.groceryping.data.GroceryItemDao;
import androidx.annotation.Nullable;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
    };
    private static final String TAG = "MainActivity";
    private GroceryViewModel groceryViewModel;
    private ReminderViewModel reminderViewModel;
    private GroceryAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyStateView;
    private Calendar selectedDateTime;
    private SwitchMaterial switchLocationService;
    private FloatingActionButton fabAddStore;
    private ExecutorService executorService;
    private RecyclerView reminderRecyclerView;
    private ReminderAdapter reminderAdapter;
    private View emptyRemindersView;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private GroceryItemDao groceryItemDao;
    private AdManager adManager;
    private int itemAddCount = 0;
    private static final int ITEMS_BEFORE_INTERSTITIAL = 3;
    private static final int BARCODE_SCAN_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "Layout inflated successfully");

            // Initialize ViewModels
            initializeViewModels();

            // Initialize RecyclerView
            initializeRecyclerView();

            // Initialize empty state view
            initializeEmptyStateView();

            // Setup Floating Action Buttons
            setupFloatingActionButtons();

            // Initialize selected date and time
            selectedDateTime = Calendar.getInstance();

            // Initialize location service switch
            initializeLocationService();

            // Observe grocery items
            observeGroceryItems();

            // Initialize executor service
            executorService = Executors.newSingleThreadExecutor();

            setupReminderRecyclerView();
            observeReminders();

            // Initialize AdManager
            initializeAdManager();

            Log.d(TAG, "Activity created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            showErrorAndFinish("Failed to initialize app: " + e.getMessage());
        }
    }

    private void initializeViewModels() {
        try {
            groceryViewModel = new ViewModelProvider(this).get(GroceryViewModel.class);
            reminderViewModel = new ViewModelProvider(this, 
                new ReminderViewModelFactory(getApplication())).get(ReminderViewModel.class);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ViewModels", e);
            throw new RuntimeException("Failed to initialize ViewModels", e);
        }
    }

    private void initializeRecyclerView() {
        try {
            recyclerView = findViewById(R.id.recyclerView);
            adapter = new GroceryAdapter(this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setHasFixedSize(true);
            recyclerView.setItemViewCacheSize(20);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing RecyclerView", e);
            throw new RuntimeException("Failed to initialize RecyclerView", e);
        }
    }

    private void initializeEmptyStateView() {
        try {
            emptyStateView = findViewById(R.id.emptyState);
            updateGroceryEmptyStateVisibility(true);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing empty state view", e);
            throw new RuntimeException("Failed to initialize empty state view", e);
        }
    }

    private void setupFloatingActionButtons() {
        try {
            FloatingActionButton fabAddItem = findViewById(R.id.fabAddItem);
            fabAddItem.setOnClickListener(view -> showAddItemDialog());

            FloatingActionButton fabAddReminder = findViewById(R.id.fabAddReminder);
            fabAddReminder.setOnClickListener(view -> showAddReminderDialog());

            fabAddStore = findViewById(R.id.fabAddStore);
            fabAddStore.setOnClickListener(v -> {
                Intent intent = new Intent(this, StoresActivity.class);
                startActivity(intent);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up FABs", e);
            throw new RuntimeException("Failed to setup FABs", e);
        }
    }

    private void initializeLocationService() {
        try {
            switchLocationService = findViewById(R.id.switchLocationService);
            switchLocationService.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    checkLocationPermissions();
                } else {
                    stopLocationService();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing location service", e);
            throw new RuntimeException("Failed to initialize location service", e);
        }
    }

    private void initializeAdManager() {
        try {
            adManager = AdManager.getInstance(this);
            adManager.loadInterstitialAd();
            adManager.loadRewardedAd();

            View adContainer = findViewById(R.id.adContainer);
            if (adContainer != null) {
                adManager.loadBannerAd((ViewGroup) adContainer);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing AdManager", e);
            // Don't throw here as ads are not critical
        }
    }

    private void observeGroceryItems() {
        try {
            groceryViewModel.getAllItems().observe(this, items -> {
                Log.d(TAG, "Grocery items updated. Count: " + items.size());
                adapter.setItems(items);
                updateGroceryEmptyStateVisibility(items.isEmpty());
            });
        } catch (Exception e) {
            Log.e(TAG, "Error observing grocery items", e);
            throw new RuntimeException("Failed to observe grocery items", e);
        }
    }

    private void showErrorAndFinish(String message) {
        new AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }

    private void setupReminderRecyclerView() {
        reminderRecyclerView = findViewById(R.id.reminderRecyclerView);
        emptyRemindersView = findViewById(R.id.emptyRemindersView);
        
        reminderAdapter = new ReminderAdapter(new ReminderAdapter.OnReminderActionListener() {
            @Override
            public void onEditReminder(Reminder reminder) {
                Log.d("MainActivity", "Edit reminder clicked: " + reminder.getItemName());
                showEditReminderDialog(reminder);
            }

            @Override
            public void onCancelReminder(Reminder reminder) {
                Log.d("MainActivity", "Cancel reminder clicked: " + reminder.getItemName());
                reminderViewModel.cancelReminder(reminder);
            }
        });
        
        reminderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reminderRecyclerView.setAdapter(reminderAdapter);
    }

    private void observeReminders() {
        reminderViewModel.getAllReminders().observe(this, reminders -> {
            Log.d("MainActivity", "Reminders updated. Count: " + reminders.size());
            reminderAdapter.setReminders(reminders);
            updateReminderEmptyStateVisibility(reminders.isEmpty());
        });
    }

    private void updateGroceryEmptyStateVisibility(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void updateReminderEmptyStateVisibility(boolean isEmpty) {
        if (isEmpty) {
            reminderRecyclerView.setVisibility(View.GONE);
            emptyRemindersView.setVisibility(View.VISIBLE);
        } else {
            reminderRecyclerView.setVisibility(View.VISIBLE);
            emptyRemindersView.setVisibility(View.GONE);
        }
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);
        builder.setTitle("Add Grocery Item");

        TextInputEditText nameInput = dialogView.findViewById(R.id.editTextName);
        AutoCompleteTextView storeInput = dialogView.findViewById(R.id.autoCompleteStore);
        TextInputEditText categoryInput = dialogView.findViewById(R.id.editTextCategory);
        TextInputEditText priceInput = dialogView.findViewById(R.id.editTextPrice);
        TextInputEditText quantityInput = dialogView.findViewById(R.id.editTextQuantity);
        MaterialButton scanBarcodeButton = dialogView.findViewById(R.id.buttonScanBarcode);

        // Observe stores and populate dropdown
        GroceryDatabase.getInstance(this).storeLocationDao().getAllStores().observe(this, stores -> {
            List<String> storeNames = new ArrayList<>();
            for (StoreLocation store : stores) {
                storeNames.add(store.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, storeNames);
            storeInput.setAdapter(adapter);
        });

        // Set up barcode scanning
        scanBarcodeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, BarcodeScannerActivity.class);
            startActivityForResult(intent, BARCODE_SCAN_REQUEST_CODE);
        });

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String storeName = storeInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();
            String priceStr = priceInput.getText().toString().trim();
            String quantityStr = quantityInput.getText().toString().trim();

            if (name.isEmpty() || storeName.isEmpty()) {
                Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);
                int quantity = quantityStr.isEmpty() ? 1 : Integer.parseInt(quantityStr);
                
                GroceryItem item = new GroceryItem(name, storeName, category, price, quantity);
                groceryViewModel.insert(item);
                showSnackbar("Item added");

                // After successful item addition:
                itemAddCount++;
                if (itemAddCount >= ITEMS_BEFORE_INTERSTITIAL) {
                    adManager.showInterstitialAd(this);
                    itemAddCount = 0;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price or quantity", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showEditItemDialog(GroceryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);
        builder.setTitle("Edit Item");

        TextInputEditText nameInput = dialogView.findViewById(R.id.editTextName);
        AutoCompleteTextView storeInput = dialogView.findViewById(R.id.autoCompleteStore);
        TextInputEditText categoryInput = dialogView.findViewById(R.id.editTextCategory);
        TextInputEditText priceInput = dialogView.findViewById(R.id.editTextPrice);
        TextInputEditText quantityInput = dialogView.findViewById(R.id.editTextQuantity);

        // Pre-fill the fields with the item's current values
        nameInput.setText(item.getName());
        storeInput.setText(item.getLocation());
        categoryInput.setText(item.getCategory());
        priceInput.setText(String.valueOf(item.getPrice()));
        quantityInput.setText(String.valueOf(item.getQuantity()));

        // Observe stores and populate dropdown
        GroceryDatabase.getInstance(this).storeLocationDao().getAllStores().observe(this, stores -> {
            List<String> storeNames = new ArrayList<>();
            for (StoreLocation store : stores) {
                storeNames.add(store.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, storeNames);
            storeInput.setAdapter(adapter);
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String storeName = storeInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();
            String priceStr = priceInput.getText().toString().trim();
            String quantityStr = quantityInput.getText().toString().trim();

            if (name.isEmpty() || storeName.isEmpty()) {
                Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);
                int quantity = quantityStr.isEmpty() ? 1 : Integer.parseInt(quantityStr);

                // Update the item
                item.setName(name);
                item.setLocation(storeName);
                item.setCategory(category);
                item.setPrice(price);
                item.setQuantity(quantity);

                // Save the updated item
                groceryViewModel.update(item);
                showSnackbar("Item updated");
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price or quantity", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_reminder, null);
        builder.setView(dialogView);
        builder.setTitle("Add Reminder");

        AutoCompleteTextView itemInput = dialogView.findViewById(R.id.autoCompleteItem);
        TextInputEditText messageInput = dialogView.findViewById(R.id.editTextMessage);
        MaterialButton buttonDate = dialogView.findViewById(R.id.buttonDate);
        MaterialButton buttonTime = dialogView.findViewById(R.id.buttonTime);
        SwitchMaterial switchRepeating = dialogView.findViewById(R.id.switchRepeating);
        TextInputLayout layoutRepeatInterval = dialogView.findViewById(R.id.layoutRepeatInterval);
        TextInputEditText editTextRepeatInterval = dialogView.findViewById(R.id.editTextRepeatInterval);

        // Observe grocery items and populate dropdown
        groceryViewModel.getAllItems().observe(this, items -> {
            List<String> itemNames = new ArrayList<>();
            for (GroceryItem item : items) {
                itemNames.add(item.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, itemNames);
            itemInput.setAdapter(adapter);
        });

        // Set up date picker
        buttonDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    buttonDate.setText(String.format("%d/%d/%d", month + 1, dayOfMonth, year));
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Set up time picker
        buttonTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    buttonTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true
            );
            timePickerDialog.show();
        });

        // Set up repeating switch
        switchRepeating.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutRepeatInterval.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        builder.setPositiveButton("Add", (dialog, which) -> {
            String itemName = itemInput.getText().toString().trim();
            String message = messageInput.getText().toString().trim();
            boolean isRepeating = switchRepeating.isChecked();
            String repeatIntervalStr = editTextRepeatInterval.getText().toString().trim();
            int repeatInterval = repeatIntervalStr.isEmpty() ? 0 : Integer.parseInt(repeatIntervalStr);

            if (itemName.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Reminder reminder = new Reminder(
                itemName,
                message,
                selectedDateTime.getTimeInMillis(),
                isRepeating,
                repeatInterval
            );
            reminder.setId(UUID.randomUUID().toString());
            reminderViewModel.addReminder(reminder);
            showSnackbar("Reminder added");
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showEditReminderDialog(Reminder reminder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_reminder, null);
        builder.setView(dialogView);
        builder.setTitle("Edit Reminder");

        AutoCompleteTextView itemInput = dialogView.findViewById(R.id.autoCompleteItem);
        TextInputEditText messageInput = dialogView.findViewById(R.id.editTextMessage);
        MaterialButton buttonDate = dialogView.findViewById(R.id.buttonDate);
        MaterialButton buttonTime = dialogView.findViewById(R.id.buttonTime);
        SwitchMaterial switchRepeating = dialogView.findViewById(R.id.switchRepeating);
        TextInputLayout layoutRepeatInterval = dialogView.findViewById(R.id.layoutRepeatInterval);
        TextInputEditText editTextRepeatInterval = dialogView.findViewById(R.id.editTextRepeatInterval);

        // Pre-fill the fields with the reminder's current values
        itemInput.setText(reminder.getItemName());
        messageInput.setText(reminder.getMessage());
        selectedDateTime.setTimeInMillis(reminder.getTimeInMillis());
        buttonDate.setText(dateFormat.format(reminder.getTimeInMillis()));
        buttonTime.setText(timeFormat.format(reminder.getTimeInMillis()));
        switchRepeating.setChecked(reminder.isRepeating());
        layoutRepeatInterval.setVisibility(reminder.isRepeating() ? View.VISIBLE : View.GONE);
        editTextRepeatInterval.setText(String.valueOf(reminder.getRepeatInterval()));

        // Observe grocery items and populate dropdown
        groceryViewModel.getAllItems().observe(this, items -> {
            List<String> itemNames = new ArrayList<>();
            for (GroceryItem item : items) {
                itemNames.add(item.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, itemNames);
            itemInput.setAdapter(adapter);
        });

        // Set up date picker
        buttonDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    buttonDate.setText(String.format("%d/%d/%d", month + 1, dayOfMonth, year));
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Set up time picker
        buttonTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    buttonTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true
            );
            timePickerDialog.show();
        });

        // Set up repeating switch
        switchRepeating.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutRepeatInterval.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            String itemName = itemInput.getText().toString().trim();
            String message = messageInput.getText().toString().trim();
            boolean isRepeating = switchRepeating.isChecked();
            String repeatIntervalStr = editTextRepeatInterval.getText().toString().trim();
            int repeatInterval = repeatIntervalStr.isEmpty() ? 0 : Integer.parseInt(repeatIntervalStr);

            if (itemName.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            reminder.setItemName(itemName);
            reminder.setMessage(message);
            reminder.setTimeInMillis(selectedDateTime.getTimeInMillis());
            reminder.setRepeating(isRepeating);
            reminder.setRepeatInterval(repeatInterval);

            reminderViewModel.updateReminder(reminder);
            showSnackbar("Reminder updated");
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void checkLocationPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        startLocationService();
    }

    private void startLocationService() {
        Log.d("MainActivity", "Starting location service");
        Intent serviceIntent = new Intent(this, LocationService.class);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        // Check battery optimization
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    private void stopLocationService() {
        Log.d("MainActivity", "Stopping location service");
        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);
    }

    private void showAddStoreDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_store, null);
        builder.setView(dialogView);
        builder.setTitle("Add Store Location");

        TextInputEditText etStoreName = dialogView.findViewById(R.id.etStoreName);
        TextInputEditText etAddress = dialogView.findViewById(R.id.etAddress);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = etStoreName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (name.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create store with default location values
            StoreLocation store = new StoreLocation(name, address, 0.0f, 0.0f, 100.0f);
            executorService.execute(() -> {
                GroceryDatabase.getInstance(this).storeLocationDao().insert(store);
                runOnUiThread(() -> showSnackbar("Store added"));
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Make the dialog reusable for other activities
    public static void showAddStoreDialog(AppCompatActivity activity, OnStoreAddedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_add_store, null);
        builder.setView(dialogView);
        builder.setTitle("Add Store Location");

        TextInputEditText etStoreName = dialogView.findViewById(R.id.etStoreName);
        TextInputEditText etAddress = dialogView.findViewById(R.id.etAddress);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = etStoreName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (name.isEmpty() || address.isEmpty()) {
                Toast.makeText(activity, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create store with default location values
            StoreLocation store = new StoreLocation(name, address, 0.0f, 0.0f, 100.0f);
            listener.onStoreAdded(store);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public interface OnStoreAddedListener {
        void onStoreAdded(StoreLocation store);
    }

    private void showSnackbar(String message) {
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(GroceryItem item) {
        showEditItemDialog(item);
    }

    @Override
    public void onDeleteClick(GroceryItem item) {
        groceryViewModel.delete(item);
        showSnackbar("Item deleted");
    }

    @Override
    public void onCheckBoxClick(GroceryItem item, boolean isChecked) {
        groceryViewModel.toggleItemCompletion(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startLocationService();
            } else {
                switchLocationService.setChecked(false);
                Toast.makeText(this, 
                    "Location permission is required for store notifications", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addTestStore() {
        // Dieppe Walmart coordinates: 46.0986° N, 64.7244° W
        StoreLocation dieppeWalmart = new StoreLocation(
            "Walmart Dieppe",
            "477 Paul St, Dieppe, NB E1A 4X5",
            46.0986f,
            -64.7244f,
            100.0f // 100 meters radius
        );
        dieppeWalmart.setActive(true);
        
        executorService.execute(() -> {
            GroceryDatabase.getInstance(this).storeLocationDao().insert(dieppeWalmart);
            runOnUiThread(() -> {
                Toast.makeText(this, "Test store added: Dieppe Walmart", Toast.LENGTH_LONG).show();
                Log.d("MainActivity", "Test store added with ID: " + dieppeWalmart.getId());
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BARCODE_SCAN_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String barcode = data.getStringExtra("barcode");
            if (barcode != null) {
                // TODO: Implement barcode lookup to get item details
                // For now, just show the barcode
                Toast.makeText(this, "Scanned barcode: " + barcode, Toast.LENGTH_LONG).show();
            }
        }
    }
}