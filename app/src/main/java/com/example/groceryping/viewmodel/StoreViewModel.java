package com.example.groceryping.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.groceryping.data.GroceryDatabase;
import com.example.groceryping.data.StoreLocation;
import com.example.groceryping.data.StoreLocationDao;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoreViewModel extends AndroidViewModel {
    private final StoreLocationDao storeLocationDao;
    private final ExecutorService executorService;

    public StoreViewModel(Application application) {
        super(application);
        storeLocationDao = GroceryDatabase.getInstance(application).storeLocationDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<StoreLocation>> getAllStores() {
        return storeLocationDao.getAllStores();
    }

    public LiveData<List<StoreLocation>> getActiveStores() {
        return storeLocationDao.getActiveStores();
    }

    public void insertStore(StoreLocation store) {
        executorService.execute(() -> storeLocationDao.insert(store));
    }

    public void updateStore(StoreLocation store) {
        executorService.execute(() -> storeLocationDao.update(store));
    }

    public void deleteStore(StoreLocation store) {
        executorService.execute(() -> storeLocationDao.delete(store));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
} 