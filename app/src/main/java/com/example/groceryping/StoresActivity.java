package com.example.groceryping;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.groceryping.adapter.StoreAdapter;
import com.example.groceryping.data.StoreLocation;
import com.example.groceryping.viewmodel.StoreViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoresActivity extends AppCompatActivity implements StoreAdapter.OnStoreClickListener {
    private StoreViewModel viewModel;
    private StoreAdapter adapter;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stores);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(StoreViewModel.class);

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new StoreAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup FAB
        FloatingActionButton fabAddStore = findViewById(R.id.fabAddStore);
        fabAddStore.setOnClickListener(v -> showAddStoreDialog());

        // Observe stores
        viewModel.getAllStores().observe(this, stores -> {
            adapter.setStores(stores);
        });

        // Initialize executor service
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddStoreDialog() {
        // Reuse the existing dialog from MainActivity
        MainActivity.showAddStoreDialog(this, store -> {
            executorService.execute(() -> {
                viewModel.insertStore(store);
                runOnUiThread(() -> Toast.makeText(this, "Store added", Toast.LENGTH_SHORT).show());
            });
        });
    }

    @Override
    public void onStoreClick(StoreLocation store) {
        showEditStoreDialog(store);
    }

    private void showEditStoreDialog(StoreLocation store) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_store, null);
        builder.setView(dialogView);
        builder.setTitle("Edit Store");

        TextInputEditText etStoreName = dialogView.findViewById(R.id.etStoreName);
        TextInputEditText etAddress = dialogView.findViewById(R.id.etAddress);
        TextInputEditText etRadius = dialogView.findViewById(R.id.etRadius);

        // Pre-fill the fields with current values
        etStoreName.setText(store.getName());
        etAddress.setText(store.getAddress());
        etRadius.setText(String.valueOf(store.getRadius()));

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etStoreName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String radiusStr = etRadius.getText().toString().trim();

            if (name.isEmpty() || address.isEmpty() || radiusStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                float radius = Float.parseFloat(radiusStr);
                store.setName(name);
                store.setAddress(address);
                store.setRadius(radius);

                executorService.execute(() -> {
                    viewModel.updateStore(store);
                    runOnUiThread(() -> Toast.makeText(this, "Store updated", Toast.LENGTH_SHORT).show());
                });
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid radius value", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onStoreToggle(StoreLocation store, boolean isActive) {
        store.setActive(isActive);
        executorService.execute(() -> {
            viewModel.updateStore(store);
        });
    }

    @Override
    public void onStoreDelete(StoreLocation store) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Store")
            .setMessage("Are you sure you want to delete " + store.getName() + "?")
            .setPositiveButton("Delete", (dialog, which) -> {
                executorService.execute(() -> {
                    viewModel.deleteStore(store);
                    runOnUiThread(() -> Toast.makeText(this, "Store deleted", Toast.LENGTH_SHORT).show());
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
} 