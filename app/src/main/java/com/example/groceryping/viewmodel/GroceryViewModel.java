package com.example.groceryping.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.groceryping.data.GroceryDatabase;
import com.example.groceryping.data.GroceryItem;
import com.example.groceryping.data.GroceryItemDao;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroceryViewModel extends AndroidViewModel {
    private final GroceryItemDao groceryItemDao;
    private final ExecutorService executorService;
    private final LiveData<List<GroceryItem>> allItems;

    public GroceryViewModel(Application application) {
        super(application);
        groceryItemDao = GroceryDatabase.getInstance(application).groceryItemDao();
        executorService = Executors.newSingleThreadExecutor();
        allItems = groceryItemDao.getAllItems();
    }

    public LiveData<List<GroceryItem>> getAllItems() {
        return allItems;
    }

    public void insert(GroceryItem item) {
        executorService.execute(() -> groceryItemDao.insert(item));
    }

    public void update(GroceryItem item) {
        executorService.execute(() -> groceryItemDao.update(item));
    }

    public void delete(GroceryItem item) {
        executorService.execute(() -> groceryItemDao.delete(item));
    }

    public void deleteAll() {
        executorService.execute(groceryItemDao::deleteAll);
    }

    public void toggleItemCompletion(GroceryItem item) {
        GroceryItem updatedItem = new GroceryItem(
            item.getId(),
            item.getName(),
            item.getLocation(),
            !item.isCompleted(),
            item.getCategory(),
            item.getPrice(),
            item.getQuantity()
        );
        update(updatedItem);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
} 